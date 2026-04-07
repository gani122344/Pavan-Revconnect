package org.revature.revconnect.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.response.ApiResponse;
import org.revature.revconnect.service.AuthService;
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
        Map<String, Object> rejectSignal = new HashMap<>();
        rejectSignal.put("type", "call-rejected");
        rejectSignal.put("callId", callId);
        rejectSignal.put("senderId", authService.getCurrentUser().getId());
        PENDING_SIGNALS.computeIfAbsent(callerId, k -> Collections.synchronizedList(new ArrayList<>())).add(rejectSignal);

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

        Map<String, Object> endSignal = new HashMap<>();
        endSignal.put("type", "call-ended");
        endSignal.put("callId", callId);
        endSignal.put("senderId", userId);
        PENDING_SIGNALS.computeIfAbsent(otherUserId, k -> Collections.synchronizedList(new ArrayList<>())).add(endSignal);

        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "ended")));
    }
}
