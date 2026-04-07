package org.revature.revconnect.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.response.ApiResponse;
import org.revature.revconnect.model.Message;
import org.revature.revconnect.model.User;
import org.revature.revconnect.repository.MessageRepository;
import org.revature.revconnect.repository.UserRepository;
import org.revature.revconnect.service.AuthService;
import org.revature.revconnect.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final AuthService authService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    // In-memory signal store: recipientId -> list of pending signals
    private static final Map<Long, List<Map<String, Object>>> PENDING_SIGNALS = new ConcurrentHashMap<>();
    // Active calls: callId -> call metadata
    private static final Map<String, Map<String, Object>> ACTIVE_CALLS = new ConcurrentHashMap<>();

    @PostMapping("/signal")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendSignal(@RequestBody Map<String, Object> signal) {
        Long senderId = authService.getCurrentUser().getId();
        Long recipientId = Long.valueOf(signal.get("recipientId").toString());
        signal.put("senderId", senderId);
        signal.put("timestamp", System.currentTimeMillis());

        PENDING_SIGNALS.computeIfAbsent(recipientId, k -> Collections.synchronizedList(new ArrayList<>())).add(signal);

        log.info("Call signal from {} to {}: type={}", senderId, recipientId, signal.get("type"));
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "sent")));
    }

    @GetMapping("/signals")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSignals() {
        Long userId = authService.getCurrentUser().getId();
        List<Map<String, Object>> signals = PENDING_SIGNALS.remove(userId);
        if (signals == null) signals = List.of();
        if (!signals.isEmpty()) {
            log.info("Delivering {} signal(s) to user {}: types={}", signals.size(), userId,
                signals.stream().map(s -> String.valueOf(s.get("type"))).toList());
        }
        return ResponseEntity.ok(ApiResponse.success(signals));
    }

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiateCall(
            @RequestParam Long recipientId,
            @RequestParam(defaultValue = "audio") String callType) {
        Long callerId = authService.getCurrentUser().getId();
        String callId = UUID.randomUUID().toString();

        Map<String, Object> callData = new HashMap<>();
        callData.put("callId", callId);
        callData.put("callerId", callerId);
        callData.put("callerName", authService.getCurrentUser().getName());
        callData.put("callerUsername", authService.getCurrentUser().getUsername());
        callData.put("callerPic", authService.getCurrentUser().getProfilePicture());
        callData.put("recipientId", recipientId);
        callData.put("callType", callType);
        callData.put("status", "ringing");
        callData.put("startedAt", System.currentTimeMillis());

        ACTIVE_CALLS.put(callId, callData);

        // Send incoming call signal to recipient
        Map<String, Object> incomingSignal = new HashMap<>(callData);
        incomingSignal.put("type", "incoming-call");
        PENDING_SIGNALS.computeIfAbsent(recipientId, k -> Collections.synchronizedList(new ArrayList<>())).add(incomingSignal);

        log.info("Call initiated: {} -> {}, type={}, callId={}", callerId, recipientId, callType, callId);

        // Send push notification to recipient (for background/closed app)
        try {
            Map<String, Object> pushPayload = new HashMap<>();
            pushPayload.put("type", "incoming-call");
            pushPayload.put("callId", callId);
            pushPayload.put("callType", callType);
            pushPayload.put("callerName", authService.getCurrentUser().getName());
            pushPayload.put("callerPic", authService.getCurrentUser().getProfilePicture());
            pushNotificationService.sendPushToUser(recipientId, pushPayload);
        } catch (Exception e) {
            log.warn("Failed to send push notification for call: {}", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(callData));
    }

    @PostMapping("/{callId}/accept")
    public ResponseEntity<ApiResponse<Map<String, Object>>> acceptCall(@PathVariable String callId) {
        Map<String, Object> call = ACTIVE_CALLS.get(callId);
        if (call == null) return ResponseEntity.ok(ApiResponse.success(Map.of("status", "not-found")));

        call.put("status", "connected");
        Long callerId = Long.valueOf(call.get("callerId").toString());

        Map<String, Object> acceptSignal = new HashMap<>();
        acceptSignal.put("type", "call-accepted");
        acceptSignal.put("callId", callId);
        acceptSignal.put("senderId", authService.getCurrentUser().getId());
        PENDING_SIGNALS.computeIfAbsent(callerId, k -> Collections.synchronizedList(new ArrayList<>())).add(acceptSignal);

        return ResponseEntity.ok(ApiResponse.success(call));
    }

    @PostMapping("/{callId}/reject")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rejectCall(@PathVariable String callId) {
        Map<String, Object> call = ACTIVE_CALLS.remove(callId);
        if (call == null) return ResponseEntity.ok(ApiResponse.success(Map.of("status", "not-found")));

        Long callerId = Long.valueOf(call.get("callerId").toString());
        String callType = String.valueOf(call.getOrDefault("callType", "audio"));
        Map<String, Object> rejectSignal = new HashMap<>();
        rejectSignal.put("type", "call-rejected");
        rejectSignal.put("callId", callId);
        rejectSignal.put("senderId", authService.getCurrentUser().getId());
        PENDING_SIGNALS.computeIfAbsent(callerId, k -> Collections.synchronizedList(new ArrayList<>())).add(rejectSignal);

        // Save missed call message
        Long recipientId = Long.valueOf(call.get("recipientId").toString());
        saveCallMessage(callerId, recipientId, callType, "missed", 0);

        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "rejected")));
    }

    @PostMapping("/{callId}/end")
    public ResponseEntity<ApiResponse<Map<String, Object>>> endCall(@PathVariable String callId) {
        Map<String, Object> call = ACTIVE_CALLS.remove(callId);
        if (call == null) return ResponseEntity.ok(ApiResponse.success(Map.of("status", "already-ended")));

        Long userId = authService.getCurrentUser().getId();
        Long callerId = Long.valueOf(call.get("callerId").toString());
        Long recipientId = Long.valueOf(call.get("recipientId").toString());
        Long otherUserId = userId.equals(callerId) ? recipientId : callerId;
        String callType = String.valueOf(call.getOrDefault("callType", "audio"));
        String status = String.valueOf(call.getOrDefault("status", "ended"));

        Map<String, Object> endSignal = new HashMap<>();
        endSignal.put("type", "call-ended");
        endSignal.put("callId", callId);
        endSignal.put("senderId", userId);
        PENDING_SIGNALS.computeIfAbsent(otherUserId, k -> Collections.synchronizedList(new ArrayList<>())).add(endSignal);

        // Calculate duration if call was connected
        long durationSec = 0;
        if ("connected".equals(status)) {
            Object startedAt = call.get("startedAt");
            if (startedAt != null) {
                durationSec = (System.currentTimeMillis() - Long.parseLong(startedAt.toString())) / 1000;
            }
        }
        String callStatus = "connected".equals(status) ? "ended" : "cancelled";
        saveCallMessage(callerId, recipientId, callType, callStatus, durationSec);

        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "ended")));
    }

    private void saveCallMessage(Long callerId, Long recipientId, String callType, String callStatus, long durationSec) {
        try {
            User caller = userRepository.findById(callerId).orElse(null);
            User recipient = userRepository.findById(recipientId).orElse(null);
            if (caller == null || recipient == null) return;

            String content = "[[CALL|" + callType + "|" + callStatus + "|" + durationSec + "]]";
            Message msg = Message.builder()
                    .sender(caller)
                    .receiver(recipient)
                    .content(content)
                    .build();
            messageRepository.save(msg);
            log.info("Call event message saved: {} -> {}, content={}", callerId, recipientId, content);
        } catch (Exception e) {
            log.error("Failed to save call event message", e);
        }
    }
}
