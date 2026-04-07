package org.revature.revconnect.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.revature.revconnect.enums.CollaborationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "collaborations", indexes = {
    @Index(name = "idx_collab_business", columnList = "business_id"),
    @Index(name = "idx_collab_creator", columnList = "creator_id"),
    @Index(name = "idx_collab_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_collab_business_creator", columnNames = {"business_id", "creator_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Collaboration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private User business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CollaborationStatus status = CollaborationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
