package com.winga.entity;

import com.winga.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency = Currency.TZS;

    // Total lifetime earnings (for freelancer analytics)
    @Builder.Default
    @Column(precision = 18, scale = 2)
    private BigDecimal totalEarned = BigDecimal.ZERO;

    // Total lifetime spent (for client analytics)
    @Builder.Default
    @Column(precision = 18, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;

    // ─── Helpers ────────────────────────────────────────────────────────────────

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient wallet balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public boolean hasSufficientFunds(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
