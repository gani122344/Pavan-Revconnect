package org.revature.revconnect.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private String transactionRef;
    private Long senderId;
    private String senderName;
    private String senderUsername;
    private String senderPic;
    private Long receiverId;
    private String receiverName;
    private String receiverUsername;
    private String receiverPic;
    private BigDecimal amount;
    private String currency;
    private String type;
    private String status;
    private String paymentMethod;
    private String note;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
