package org.revature.revconnect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.revature.revconnect.model.PushSubscription;
import org.revature.revconnect.repository.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PushNotificationService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    @Value("${vapid.private.key}")
    private String vapidPrivateKey;

    @Value("${vapid.subject}")
    private String vapidSubject;

    private PushService pushService;

    public PushNotificationService(PushSubscriptionRepository pushSubscriptionRepository, ObjectMapper objectMapper) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }

            // Manually decode VAPID keys and build KeyPair to avoid web-push-java parsing issue
            ECNamedCurveParameterSpec paramSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");

            // Decode private key from URL-safe base64
            byte[] privBytes = Base64.getUrlDecoder().decode(vapidPrivateKey);
            BigInteger privInt = new BigInteger(1, privBytes);
            ECPrivateKeySpec privSpec = new ECPrivateKeySpec(privInt, paramSpec);
            PrivateKey privateKey = keyFactory.generatePrivate(privSpec);

            // Decode public key from URL-safe base64 (uncompressed point)
            byte[] pubBytes = Base64.getUrlDecoder().decode(vapidPublicKey);
            ECPoint pubPoint = paramSpec.getCurve().decodePoint(pubBytes);
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(pubPoint, paramSpec);
            PublicKey publicKey = keyFactory.generatePublic(pubSpec);

            pushService = new PushService(new KeyPair(publicKey, privateKey), vapidSubject);
            log.info("Web Push service initialized successfully");
        } catch (Throwable e) {
            log.error("Failed to initialize Web Push service", e);
        }
    }

    public void sendPushToUser(Long userId, Map<String, Object> payload) {
        if (pushService == null) {
            log.warn("Push service not initialized, skipping push for user {}", userId);
            return;
        }

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserId(userId);
        if (subscriptions.isEmpty()) {
            log.debug("No push subscriptions found for user {}", userId);
            return;
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Failed to serialize push payload", e);
            return;
        }

        for (PushSubscription sub : subscriptions) {
            try {
                Notification notification = new Notification(
                        sub.getEndpoint(),
                        sub.getP256dh(),
                        sub.getAuth(),
                        payloadJson.getBytes()
                );
                pushService.send(notification);
                log.info("Push notification sent to user {} endpoint {}", userId, sub.getEndpoint().substring(0, Math.min(50, sub.getEndpoint().length())));
            } catch (Exception e) {
                log.warn("Failed to send push to endpoint {}: {}", sub.getEndpoint().substring(0, Math.min(50, sub.getEndpoint().length())), e.getMessage());
                if (e.getMessage() != null && (e.getMessage().contains("410") || e.getMessage().contains("404"))) {
                    pushSubscriptionRepository.delete(sub);
                    log.info("Removed expired push subscription for user {}", userId);
                }
            }
        }
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }
}
