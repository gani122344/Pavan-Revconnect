package org.revature.revconnect.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempAuthResponse {

    private String tempToken;
    private String email;
    private String name;
    private String provider;
    private boolean requiresPhone;
}
