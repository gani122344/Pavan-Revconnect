package org.revature.revconnect.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.revature.revconnect.enums.PromotionStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_promotions", indexes = {
    @Index(name = "idx_promo_post", columnList = "post_id"),
    @Index(name = "idx_promo_creator", columnList = "creator_id"),
    @Index(name = "idx_promo_business", columnList = "business_id"),
    @Index(name = "idx_promo_collab", columnList = "collaboration_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_promo_post_creator", columnNames = {"post_id", "creator_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private User business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id", nullable = false)
    private Collaboration collaboration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PromotionStatus status = PromotionStatus.PENDING;

    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "organic_clicks")
    @Builder.Default
    private Integer organicClicks = 0;

    @Column(name = "creator_clicks")
    @Builder.Default
    private Integer creatorClicks = 0;

    @Column(length = 100)
    private String ctaLabel;

    @Column(length = 500)
    private String ctaUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
