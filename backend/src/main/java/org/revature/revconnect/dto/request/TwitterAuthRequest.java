package org.revature.revconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwitterAuthRequest {

    @NotBlank(message = "Twitter auth code is required")
    private String code;

    @NotBlank(message = "Code verifier is required")
    private String codeVerifier;
}
