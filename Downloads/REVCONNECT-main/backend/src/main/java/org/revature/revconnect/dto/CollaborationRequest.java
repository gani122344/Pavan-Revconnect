package org.revature.revconnect.dto;

import lombok.Data;

@Data
public class CollaborationRequest {
    private Long creatorId;
    private String message;
    private String promotionRules;
    private String contentGuidelines;

    // Structured payment
    private Double fixedFee;        // one-time fixed fee ₹
    private Double ratePerView;     // ₹X per viewMilestone views
    private Integer viewMilestone;  // e.g. 10 (₹2 per 10 views)
    private String paymentSchedule; // WEEKLY, MONTHLY, ON_COMPLETION

    private String brandName;
    private String brandWebsite;
    private Integer durationDays;
}
