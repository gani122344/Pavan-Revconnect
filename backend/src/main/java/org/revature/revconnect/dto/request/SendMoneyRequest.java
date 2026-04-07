package org.revature.revconnect.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendMoneyRequest {
    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    private String recipientUsername;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum amount is ₹1")
    private BigDecimal amount;

    private String note;

    private String paymentMethod;
}
