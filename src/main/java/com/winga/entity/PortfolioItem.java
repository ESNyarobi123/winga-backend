package com.winga.entity;

import com.winga.domain.enums.ModerationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Service provider portfolio: images, videos, completed projects.
 */
@Entity
@Table(name = "portfolio_items", indexes = {
        @Index(name = "idx_portfolio_user", columnList = "user_id"),
        @Index(name = "idx_portfolio_moderation", columnList = "moderation_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** IMAGE, VIDEO, PROJECT */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "moderation_status", length = 20)
    private ModerationStatus moderationStatus = ModerationStatus.PENDING_APPROVAL;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
