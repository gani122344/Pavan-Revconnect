package org.revature.revconnect.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.response.ApiResponse;
import org.revature.revconnect.model.PushSubscription;
import org.revature.revconnect.model.User;
import org.revature.revconnect.repository.PushSubscriptionRepository;
import org.revature.revconnect.service.AuthService;
import org.revature.revconnect.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
@Slf4j
public class PushSubscriptionController {

    private final AuthService authService;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushNotificationService pushNotificationService;

    @GetMapping("/vapid-key")
    public ResponseEntity<ApiResponse<Map<String, String>>> getVapidKey() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("publicKey", pushNotificationService.getVapidPublicKey())
        ));
    }

    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, String>>> subscribe(@RequestBody Map<String, Object> body) {
        User user = authService.getCurrentUser();

        String endpoint = (String) body.get("endpoint");
        @SuppressWarnings("unchecked")
        Map<String, String> keys = (Map<String, String>) body.get("keys");
        String p256dh = keys.get("p256dh");
        String auth = keys.get("auth");

        // Upsert: if endpoint exists, update user; otherwise create new
        Optional<PushSubscription> existing = pushSubscriptionRepository.findByEndpoint(endpoint);
        if (existing.isPresent()) {
            PushSubscription sub = existing.get();
            sub.setUser(user);
            sub.setP256dh(p256dh);
            sub.setAuth(auth);
            pushSubscriptionRepository.save(sub);
        } else {
            PushSubscription sub = PushSubscription.builder()
                    .user(user)
                    .endpoint(endpoint)
                    .p256dh(p256dh)
                    .auth(auth)
                    .build();
            pushSubscriptionRepository.save(sub);
        }

        log.info("Push subscription saved for user {} (endpoint: {}...)", user.getId(), endpoint.substring(0, Math.min(50, endpoint.length())));
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "subscribed")));
    }

    @PostMapping("/unsubscribe")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, String>>> unsubscribe(@RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        if (endpoint != null) {
            pushSubscriptionRepository.deleteByEndpoint(endpoint);
            log.info("Push subscription removed for endpoint: {}...", endpoint.substring(0, Math.min(50, endpoint.length())));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "unsubscribed")));
    }
}
