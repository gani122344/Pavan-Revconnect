package org.revature.revconnect.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RazorpayOrderResponse {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String transactionRef;
    private String keyId;
}
