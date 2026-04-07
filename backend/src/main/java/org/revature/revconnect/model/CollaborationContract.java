package org.revature.revconnect.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "collaboration_contracts", indexes = {
    @Index(name = "idx_contract_collab", columnList = "collaboration_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollaborationContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id", nullable = false)
    private Collaboration collaboration;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String promotionRules;

    @Column(columnDefinition = "TEXT")
    private String paymentTerms;

    @Column(columnDefinition = "TEXT")
    private String contentGuidelines;

    // ═══ Structured Payment Model ═══
    @Column(name = "fixed_fee")
    @Builder.Default
    private Double fixedFee = 0.0;

    @Column(name = "rate_per_view")
    @Builder.Default
    private Double ratePerView = 0.0;

    @Column(name = "view_milestone")
    @Builder.Default
    private Integer viewMilestone = 10; // e.g. ₹X per 10 views

    @Column(name = "payment_schedule")
    @Builder.Default
    private String paymentSchedule = "MONTHLY"; // WEEKLY, MONTHLY, ON_COMPLETION

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "brand_website")
    private String brandWebsite;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "accepted_by_creator")
    @Builder.Default
    private Boolean acceptedByCreator = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
