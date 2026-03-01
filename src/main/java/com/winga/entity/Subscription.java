package com.winga.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Service Provider (FREELANCER) monthly subscription.
 * Only users with active subscription can submit proposals (bid).
 */
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscription_user", columnList = "user_id"),
        @Index(name = "idx_subscription_status", columnList = "status"),
        @Index(name = "idx_subscription_ends", columnList = "ends_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Plan name e.g. MONTHLY_PROVIDER */
    @Column(nullable = false, length = 50)
    private String planId;

    @Column(nullable = false, length = 50)
    private String status; // ACTIVE, EXPIRED, CANCELLED

    @Column(nullable = false, updatable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
