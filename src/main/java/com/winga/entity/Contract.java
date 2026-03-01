package com.winga.entity;

import com.winga.domain.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts", indexes = {
        @Index(name = "idx_contract_client", columnList = "client_id"),
        @Index(name = "idx_contract_freelancer", columnList = "freelancer_id"),
        @Index(name = "idx_contract_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    // ─── Financial Fields ────────────────────────────────────────────────────────

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;             // Original agreed amount

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal escrowAmount = BigDecimal.ZERO;  // Funds locked in escrow

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal releasedAmount = BigDecimal.ZERO;// Funds already released

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal platformFeeCollected = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal addonEscrowAmount = BigDecimal.ZERO;  // Funds from accepted add-ons (held until completed)

    // ─── Status ──────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private ContractStatus status = ContractStatus.ACTIVE;

    // ─── Revisions (Request Changes limit) ──────────────────────────────────────

    @Builder.Default
    @Column(nullable = false)
    private Integer revisionLimit = 3;

    @Builder.Default
    @Column(nullable = false)
    private Integer revisionsUsed = 0;

    // ─── Audit ──────────────────────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String terminationReason;

    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ─── Milestones ──────────────────────────────────────────────────────────────

    @Builder.Default
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Milestone> milestones = new ArrayList<>();
}
