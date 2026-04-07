package org.revature.revconnect.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostPromotionResponse {
    private Long id;
    private Long postId;
    private String postContent;
    private String postMediaUrl;

    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private String creatorPic;

    private Long businessId;
    private String businessName;
    private String businessUsername;
    private String businessPic;

    private String status;
    private String ctaLabel;
    private String ctaUrl;
    private Integer organicClicks;
    private Integer creatorClicks;
    private LocalDateTime promotedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
