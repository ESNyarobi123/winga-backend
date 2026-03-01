package com.winga.entity;

import com.winga.domain.enums.TransactionStatus;
import com.winga.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_tx_wallet", columnList = "wallet_id"),
        @Index(name = "idx_tx_type", columnList = "transactionType"),
        @Index(name = "idx_tx_created", columnList = "createdAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceBefore;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String referenceId;                 // Contract ID, Proposal ID, etc.

    @Column(length = 20)
    private String provider;                    // MPESA, TIGOPESA, etc.

    @Column(length = 50)
    private String externalTransactionId;       // Mobile money gateway ref

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20)
    private TransactionStatus status = TransactionStatus.COMPLETED;  // PENDING, COMPLETED, FAILED

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
