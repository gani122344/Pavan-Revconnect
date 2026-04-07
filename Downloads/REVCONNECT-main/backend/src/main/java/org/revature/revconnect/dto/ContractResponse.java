package org.revature.revconnect.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ContractResponse {
    private Long id;
    private String promotionRules;
    private String contentGuidelines;

    // Structured payment
    private Double fixedFee;
    private Double ratePerView;
    private Integer viewMilestone;
    private String paymentSchedule;

    private String brandName;
    private String brandWebsite;
    private Integer durationDays;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean acceptedByCreator;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
}
