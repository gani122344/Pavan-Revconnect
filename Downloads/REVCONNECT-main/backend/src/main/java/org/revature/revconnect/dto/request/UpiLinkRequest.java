package org.revature.revconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpiLinkRequest {
    @NotBlank(message = "UPI ID is required")
    @Pattern(regexp = "^[\\w.-]+@[\\w]+$", message = "Invalid UPI ID format (e.g., username@upi)")
    private String upiId;

    private String provider;

    private Boolean isPrimary = false;
}
