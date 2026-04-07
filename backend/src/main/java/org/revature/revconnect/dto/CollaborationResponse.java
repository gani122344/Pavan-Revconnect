package org.revature.revconnect.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollaborationResponse {
    private Long id;
    private String status;

    private Long businessId;
    private String businessName;
    private String businessUsername;
    private String businessPic;

    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private String creatorPic;

    private String message;
    private ContractResponse contract;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
