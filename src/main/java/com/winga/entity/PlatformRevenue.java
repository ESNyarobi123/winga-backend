package com.winga.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks total platform revenue. One row per "accounting period" (or just one
 * running-total row updated atomically via the EscrowService).
 */
@Entity
@Table(name = "platform_revenue")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlatformRevenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String period;                      // e.g. "2024-02", "TOTAL"

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalFees = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Long totalTransactions = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addFee(BigDecimal fee) {
        this.totalFees = this.totalFees.add(fee);
        this.totalTransactions++;
    }
}
