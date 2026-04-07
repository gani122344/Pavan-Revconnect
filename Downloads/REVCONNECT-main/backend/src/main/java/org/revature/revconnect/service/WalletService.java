package org.revature.revconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.request.*;
import org.revature.revconnect.dto.response.*;
import org.revature.revconnect.enums.*;
import org.revature.revconnect.exception.ResourceNotFoundException;
import org.revature.revconnect.model.*;
import org.revature.revconnect.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final AuthService authService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UpiLinkRepository upiLinkRepository;
    private final UserRepository userRepository;

    // ─── Wallet ───

    @Transactional
    public WalletResponse getOrCreateWallet() {
        User user = authService.getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wallet w = Wallet.builder().user(user).balance(BigDecimal.ZERO).currency("INR").build();
                    return walletRepository.save(w);
                });
        return toWalletResponse(wallet);
    }

    public WalletResponse getWalletBalance() {
        User user = authService.getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", user.getId()));
        return toWalletResponse(wallet);
    }

    // ─── Send Money (wallet-to-wallet) ───

    @Transactional
    public TransactionResponse sendMoney(SendMoneyRequest request) {
        User sender = authService.getCurrentUser();

        User receiver;
        if (request.getRecipientId() != null) {
            receiver = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getRecipientId()));
        } else if (request.getRecipientUsername() != null) {
            receiver = userRepository.findByUsername(request.getRecipientUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getRecipientUsername()));
        } else {
            throw new IllegalArgumentException("Recipient ID or username is required");
        }

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send money to yourself");
        }

        BigDecimal amount = request.getAmount();

        // Lock sender wallet
        Wallet senderWallet = walletRepository.findByUserIdForUpdate(sender.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", sender.getId()));

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Current: ₹" + senderWallet.getBalance());
        }

        // Lock receiver wallet (or create)
        Wallet receiverWallet = walletRepository.findByUserIdForUpdate(receiver.getId())
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().user(receiver).balance(BigDecimal.ZERO).currency("INR").build()));

        // Debit sender, credit receiver
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // Create transaction
        Transaction txn = Transaction.builder()
                .transactionRef(generateRef())
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .currency("INR")
                .type(TransactionType.SEND)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod(PaymentMethod.WALLET)
                .note(request.getNote())
                .completedAt(LocalDateTime.now())
                .build();
        transactionRepository.save(txn);

        log.info("Money sent: {} -> {}, amount={}", sender.getUsername(), receiver.getUsername(), amount);
        return toTransactionResponse(txn, sender.getId());
    }

    // ─── Request Money ───

    @Transactional
    public PaymentRequestResponse requestMoney(RequestMoneyRequest request) {
        User requester = authService.getCurrentUser();
        User payer = userRepository.findById(request.getPayerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getPayerId()));

        if (requester.getId().equals(payer.getId())) {
            throw new IllegalArgumentException("Cannot request money from yourself");
        }

        PaymentRequest pr = PaymentRequest.builder()
                .requester(requester)
                .payer(payer)
                .amount(request.getAmount())
                .currency("INR")
                .note(request.getNote())
                .status(PaymentRequestStatus.PENDING)
                .build();
        paymentRequestRepository.save(pr);

        log.info("Money request: {} -> {}, amount={}", requester.getUsername(), payer.getUsername(), request.getAmount());
        return toPaymentRequestResponse(pr);
    }

    @Transactional
    public TransactionResponse acceptPaymentRequest(Long requestId) {
        User payer = authService.getCurrentUser();
        PaymentRequest pr = paymentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", requestId));

        if (!pr.getPayer().getId().equals(payer.getId())) {
            throw new IllegalArgumentException("You are not the payer for this request");
        }
        if (pr.getStatus() != PaymentRequestStatus.PENDING) {
            throw new IllegalArgumentException("This request is no longer pending");
        }

        // Use sendMoney logic
        SendMoneyRequest smr = new SendMoneyRequest();
        smr.setRecipientId(pr.getRequester().getId());
        smr.setAmount(pr.getAmount());
        smr.setNote("Payment for request: " + (pr.getNote() != null ? pr.getNote() : ""));

        TransactionResponse txnResp = sendMoney(smr);

        pr.setStatus(PaymentRequestStatus.ACCEPTED);
        pr.setRespondedAt(LocalDateTime.now());
        paymentRequestRepository.save(pr);

        return txnResp;
    }

    @Transactional
    public PaymentRequestResponse rejectPaymentRequest(Long requestId) {
        User payer = authService.getCurrentUser();
        PaymentRequest pr = paymentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", requestId));

        if (!pr.getPayer().getId().equals(payer.getId())) {
            throw new IllegalArgumentException("You are not the payer for this request");
        }
        if (pr.getStatus() != PaymentRequestStatus.PENDING) {
            throw new IllegalArgumentException("This request is no longer pending");
        }

        pr.setStatus(PaymentRequestStatus.REJECTED);
        pr.setRespondedAt(LocalDateTime.now());
        paymentRequestRepository.save(pr);

        return toPaymentRequestResponse(pr);
    }

    // ─── Add Money (via Razorpay → wallet) ───

    @Transactional
    public Transaction createAddMoneyTransaction(BigDecimal amount, String razorpayOrderId) {
        User user = authService.getCurrentUser();
        // Ensure wallet exists
        walletRepository.findByUserId(user.getId())
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().user(user).balance(BigDecimal.ZERO).currency("INR").build()));

        Transaction txn = Transaction.builder()
                .transactionRef(generateRef())
                .sender(null)
                .receiver(user)
                .amount(amount)
                .currency("INR")
                .type(TransactionType.ADD_MONEY)
                .status(TransactionStatus.PENDING)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .razorpayOrderId(razorpayOrderId)
                .build();
        return transactionRepository.save(txn);
    }

    @Transactional
    public TransactionResponse completeAddMoney(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        Transaction txn = transactionRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "razorpayOrderId", razorpayOrderId));

        if (txn.getStatus() == TransactionStatus.COMPLETED) {
            return toTransactionResponse(txn, txn.getReceiver().getId());
        }

        txn.setRazorpayPaymentId(razorpayPaymentId);
        txn.setRazorpaySignature(razorpaySignature);
        txn.setStatus(TransactionStatus.COMPLETED);
        txn.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(txn);

        // Credit wallet
        Wallet wallet = walletRepository.findByUserIdForUpdate(txn.getReceiver().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", txn.getReceiver().getId()));
        wallet.setBalance(wallet.getBalance().add(txn.getAmount()));
        walletRepository.save(wallet);

        log.info("Add money completed: user={}, amount={}", txn.getReceiver().getUsername(), txn.getAmount());
        return toTransactionResponse(txn, txn.getReceiver().getId());
    }

    @Transactional
    public void failAddMoney(String razorpayOrderId, String reason) {
        Transaction txn = transactionRepository.findByRazorpayOrderId(razorpayOrderId).orElse(null);
        if (txn != null && txn.getStatus() == TransactionStatus.PENDING) {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setFailureReason(reason);
            transactionRepository.save(txn);
        }
    }

    // ─── Transactions ───

    public Page<TransactionResponse> getTransactions(int page, int size, String filter) {
        User user = authService.getCurrentUser();
        Page<Transaction> txns;
        PageRequest pr = PageRequest.of(page, size);

        switch (filter != null ? filter.toLowerCase() : "all") {
            case "sent":
                txns = transactionRepository.findSentByUserId(user.getId(), pr);
                break;
            case "received":
                txns = transactionRepository.findReceivedByUserId(user.getId(), pr);
                break;
            case "pending":
                txns = transactionRepository.findByUserIdAndStatus(user.getId(), TransactionStatus.PENDING, pr);
                break;
            case "failed":
                txns = transactionRepository.findByUserIdAndStatus(user.getId(), TransactionStatus.FAILED, pr);
                break;
            default:
                txns = transactionRepository.findByUserId(user.getId(), pr);
        }

        return txns.map(t -> toTransactionResponse(t, user.getId()));
    }

    // ─── Payment Requests ───

    public Page<PaymentRequestResponse> getPaymentRequests(int page, int size) {
        User user = authService.getCurrentUser();
        return paymentRequestRepository.findByUserId(user.getId(), PageRequest.of(page, size))
                .map(this::toPaymentRequestResponse);
    }

    public Page<PaymentRequestResponse> getPendingPaymentRequests(int page, int size) {
        User user = authService.getCurrentUser();
        return paymentRequestRepository.findPendingForUser(user.getId(), PaymentRequestStatus.PENDING, PageRequest.of(page, size))
                .map(this::toPaymentRequestResponse);
    }

    public long getPendingRequestCount() {
        User user = authService.getCurrentUser();
        return paymentRequestRepository.countByPayerIdAndStatus(user.getId(), PaymentRequestStatus.PENDING);
    }

    // ─── Bank Account ───

    @Transactional
    public BankAccount addBankAccount(BankAccountRequest request) {
        User user = authService.getCurrentUser();
        if (bankAccountRepository.existsByUserIdAndAccountNumber(user.getId(), request.getAccountNumber())) {
            throw new IllegalArgumentException("This account number is already linked");
        }
        BankAccount ba = BankAccount.builder()
                .user(user)
                .accountHolderName(request.getAccountHolderName())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .bankName(request.getBankName())
                .isPrimary(request.getIsPrimary())
                .build();
        return bankAccountRepository.save(ba);
    }

    public List<BankAccount> getBankAccounts() {
        return bankAccountRepository.findByUserId(authService.getCurrentUser().getId());
    }

    @Transactional
    public void deleteBankAccount(Long id) {
        User user = authService.getCurrentUser();
        BankAccount ba = bankAccountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", "id", id));
        bankAccountRepository.delete(ba);
    }

    // ─── UPI ───

    @Transactional
    public UpiLink addUpiLink(UpiLinkRequest request) {
        User user = authService.getCurrentUser();
        if (upiLinkRepository.existsByUserIdAndUpiId(user.getId(), request.getUpiId())) {
            throw new IllegalArgumentException("This UPI ID is already linked");
        }
        UpiLink upi = UpiLink.builder()
                .user(user)
                .upiId(request.getUpiId())
                .provider(request.getProvider())
                .isPrimary(request.getIsPrimary())
                .build();
        return upiLinkRepository.save(upi);
    }

    public List<UpiLink> getUpiLinks() {
        return upiLinkRepository.findByUserId(authService.getCurrentUser().getId());
    }

    @Transactional
    public void deleteUpiLink(Long id) {
        User user = authService.getCurrentUser();
        UpiLink upi = upiLinkRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("UpiLink", "id", id));
        upiLinkRepository.delete(upi);
    }

    // ─── Helpers ───

    private String generateRef() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private WalletResponse toWalletResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId())
                .userId(w.getUser().getId())
                .username(w.getUser().getUsername())
                .balance(w.getBalance())
                .currency(w.getCurrency())
                .isActive(w.getIsActive())
                .createdAt(w.getCreatedAt())
                .build();
    }

    public TransactionResponse toTransactionResponse(Transaction t, Long currentUserId) {
        return TransactionResponse.builder()
                .id(t.getId())
                .transactionRef(t.getTransactionRef())
                .senderId(t.getSender() != null ? t.getSender().getId() : null)
                .senderName(t.getSender() != null ? t.getSender().getName() : "Razorpay")
                .senderUsername(t.getSender() != null ? t.getSender().getUsername() : null)
                .senderPic(t.getSender() != null ? t.getSender().getProfilePicture() : null)
                .receiverId(t.getReceiver() != null ? t.getReceiver().getId() : null)
                .receiverName(t.getReceiver() != null ? t.getReceiver().getName() : null)
                .receiverUsername(t.getReceiver() != null ? t.getReceiver().getUsername() : null)
                .receiverPic(t.getReceiver() != null ? t.getReceiver().getProfilePicture() : null)
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .type(t.getType().name())
                .status(t.getStatus().name())
                .paymentMethod(t.getPaymentMethod() != null ? t.getPaymentMethod().name() : null)
                .note(t.getNote())
                .failureReason(t.getFailureReason())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }

    private PaymentRequestResponse toPaymentRequestResponse(PaymentRequest pr) {
        return PaymentRequestResponse.builder()
                .id(pr.getId())
                .requesterId(pr.getRequester().getId())
                .requesterName(pr.getRequester().getName())
                .requesterUsername(pr.getRequester().getUsername())
                .requesterPic(pr.getRequester().getProfilePicture())
                .payerId(pr.getPayer().getId())
                .payerName(pr.getPayer().getName())
                .payerUsername(pr.getPayer().getUsername())
                .payerPic(pr.getPayer().getProfilePicture())
                .amount(pr.getAmount())
                .currency(pr.getCurrency())
                .note(pr.getNote())
                .status(pr.getStatus().name())
                .createdAt(pr.getCreatedAt())
                .respondedAt(pr.getRespondedAt())
                .build();
    }
}
