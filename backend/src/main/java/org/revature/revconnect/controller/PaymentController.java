package org.revature.revconnect.controller;

import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.request.AddMoneyRequest;
import org.revature.revconnect.dto.request.VerifyPaymentRequest;
import org.revature.revconnect.dto.response.ApiResponse;
import org.revature.revconnect.dto.response.RazorpayOrderResponse;
import org.revature.revconnect.dto.response.TransactionResponse;
import org.revature.revconnect.model.Transaction;
import org.revature.revconnect.service.RazorpayService;
import org.revature.revconnect.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final RazorpayService razorpayService;
    private final WalletService walletService;

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfig() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "keyId", razorpayService.getKeyId() != null ? razorpayService.getKeyId() : "",
                "configured", razorpayService.isConfigured()
        )));
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(@Valid @RequestBody AddMoneyRequest request) {
        try {
            String receipt = "rcpt_" + System.currentTimeMillis();
            RazorpayOrderResponse order = razorpayService.createOrder(
                    request.getAmount(), "INR", receipt);

            // Create pending transaction
            walletService.createAddMoneyTransaction(request.getAmount(), order.getOrderId());

            log.info("Razorpay order created: {}", order.getOrderId());
            return ResponseEntity.ok(ApiResponse.success("Order created", order));
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create payment order: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<TransactionResponse>> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        boolean valid = razorpayService.verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        if (!valid) {
            walletService.failAddMoney(request.getRazorpayOrderId(), "Invalid payment signature");
            log.warn("Payment signature verification failed for order: {}", request.getRazorpayOrderId());
            return ResponseEntity.badRequest().body(ApiResponse.error("Payment verification failed"));
        }

        TransactionResponse txn = walletService.completeAddMoney(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        log.info("Payment verified and wallet credited: orderId={}", request.getRazorpayOrderId());
        return ResponseEntity.ok(ApiResponse.success("Payment successful", txn));
    }
}
