package org.revature.revconnect.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentRequestResponse {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private String requesterUsername;
    private String requesterPic;
    private Long payerId;
    private String payerName;
    private String payerUsername;
    private String payerPic;
    private BigDecimal amount;
    private String currency;
    private String note;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
