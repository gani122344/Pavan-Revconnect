package org.revature.revconnect.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.revature.revconnect.dto.response.RazorpayOrderResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class RazorpayService {

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    private RazorpayClient client;

    @PostConstruct
    public void init() {
        try {
            if (keyId != null && !keyId.isEmpty() && keySecret != null && !keySecret.isEmpty()) {
                client = new RazorpayClient(keyId, keySecret);
                log.info("Razorpay client initialized successfully");
            } else {
                log.warn("Razorpay API keys not configured. Payment gateway features will be disabled.");
            }
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client", e);
        }
    }

    public RazorpayOrderResponse createOrder(BigDecimal amount, String currency, String receipt) throws RazorpayException {
        if (client == null) {
            throw new IllegalStateException("Razorpay is not configured. Please add razorpay.key.id and razorpay.key.secret to application.properties");
        }

        JSONObject options = new JSONObject();
        options.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // Razorpay expects paise
        options.put("currency", currency);
        options.put("receipt", receipt);

        Order order = client.orders.create(options);

        return RazorpayOrderResponse.builder()
                .orderId(order.get("id"))
                .amount(amount)
                .currency(currency)
                .transactionRef(receipt)
                .keyId(keyId)
                .build();
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(attributes, keySecret);
        } catch (RazorpayException e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    public String getKeyId() {
        return keyId;
    }

    public boolean isConfigured() {
        return client != null;
    }
}
