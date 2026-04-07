package org.revature.revconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.request.*;
import org.revature.revconnect.dto.response.*;
import org.revature.revconnect.model.BankAccount;
import org.revature.revconnect.model.UpiLink;
import org.revature.revconnect.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet() {
        return ResponseEntity.ok(ApiResponse.success("Wallet ready", walletService.getOrCreateWallet()));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWalletBalance()));
    }

    // ─── Send Money ───
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<TransactionResponse>> sendMoney(@Valid @RequestBody SendMoneyRequest request) {
        log.info("Send money request: amount={}", request.getAmount());
        return ResponseEntity.ok(ApiResponse.success("Money sent successfully", walletService.sendMoney(request)));
    }

    // ─── Request Money ───
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<PaymentRequestResponse>> requestMoney(@Valid @RequestBody RequestMoneyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Money request sent", walletService.requestMoney(request)));
    }

    @PostMapping("/request/{id}/accept")
    public ResponseEntity<ApiResponse<TransactionResponse>> acceptRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment request accepted", walletService.acceptPaymentRequest(id)));
    }

    @PostMapping("/request/{id}/reject")
    public ResponseEntity<ApiResponse<PaymentRequestResponse>> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment request rejected", walletService.rejectPaymentRequest(id)));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<PaymentRequestResponse>>> getRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getPaymentRequests(page, size)));
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<ApiResponse<Page<PaymentRequestResponse>>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getPendingPaymentRequests(page, size)));
    }

    @GetMapping("/requests/pending/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingRequestCount() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", walletService.getPendingRequestCount())));
    }

    // ─── Transactions ───
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filter) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getTransactions(page, size, filter)));
    }

    // ─── Bank Accounts ───
    @PostMapping("/bank")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addBank(@Valid @RequestBody BankAccountRequest request) {
        BankAccount ba = walletService.addBankAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Bank account added", Map.of(
                "id", ba.getId(), "bankName", ba.getBankName(),
                "accountNumber", "****" + ba.getAccountNumber().substring(Math.max(0, ba.getAccountNumber().length() - 4)),
                "ifscCode", ba.getIfscCode(), "accountHolderName", ba.getAccountHolderName()
        )));
    }

    @GetMapping("/bank")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBanks() {
        List<Map<String, Object>> banks = walletService.getBankAccounts().stream()
                .map(ba -> Map.<String, Object>of(
                        "id", ba.getId(), "bankName", ba.getBankName(),
                        "accountNumber", "****" + ba.getAccountNumber().substring(Math.max(0, ba.getAccountNumber().length() - 4)),
                        "ifscCode", ba.getIfscCode(), "accountHolderName", ba.getAccountHolderName(),
                        "isPrimary", ba.getIsPrimary()
                )).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(banks));
    }

    @DeleteMapping("/bank/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBank(@PathVariable Long id) {
        walletService.deleteBankAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Bank account removed", null));
    }

    // ─── UPI ───
    @PostMapping("/upi")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addUpi(@Valid @RequestBody UpiLinkRequest request) {
        UpiLink upi = walletService.addUpiLink(request);
        return ResponseEntity.ok(ApiResponse.success("UPI ID linked", Map.of(
                "id", upi.getId(), "upiId", upi.getUpiId(),
                "provider", upi.getProvider() != null ? upi.getProvider() : "",
                "isPrimary", upi.getIsPrimary()
        )));
    }

    @GetMapping("/upi")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUpis() {
        List<Map<String, Object>> upis = walletService.getUpiLinks().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(), "upiId", u.getUpiId(),
                        "provider", u.getProvider() != null ? u.getProvider() : "",
                        "isPrimary", u.getIsPrimary()
                )).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(upis));
    }

    @DeleteMapping("/upi/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUpi(@PathVariable Long id) {
        walletService.deleteUpiLink(id);
        return ResponseEntity.ok(ApiResponse.success("UPI ID removed", null));
    }
}
