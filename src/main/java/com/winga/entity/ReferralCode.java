package com.winga.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Affiliate / referral: user has a unique code; when someone signs up or completes a job via link, referrer gets commission.
 */
@Entity
@Table(name = "referral_codes", indexes = {
        @Index(name = "idx_referral_user", columnList = "user_id"),
        @Index(name = "idx_referral_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Builder.Default
    @Column(nullable = false)
    private Integer signupCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer hireCount = 0;

    /** Commission balance (TZS) — paid out via admin */
    @Column(nullable = false, precision = 18, scale = 2)
    private java.math.BigDecimal commissionBalance = java.math.BigDecimal.ZERO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
