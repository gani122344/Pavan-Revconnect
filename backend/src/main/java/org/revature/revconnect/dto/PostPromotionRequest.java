package org.revature.revconnect.dto;

import lombok.Data;

@Data
public class PostPromotionRequest {
    private Long postId;
    private Long creatorId;
    private String ctaLabel;
    private String ctaUrl;
}
