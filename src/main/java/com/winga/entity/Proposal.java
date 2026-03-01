package com.winga.entity;

import com.winga.domain.enums.ModerationStatus;
import com.winga.domain.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposals",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_proposal_job_freelancer",
                columnNames = {"job_id", "freelancer_id"}  // One proposal per job per freelancer
        ),
        indexes = {
                @Index(name = "idx_proposal_job", columnList = "job_id"),
                @Index(name = "idx_proposal_freelancer", columnList = "freelancer_id"),
                @Index(name = "idx_proposal_status", columnList = "status")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String coverLetter;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal bidAmount;

    @Column(length = 100)
    private String estimatedDuration;           // e.g. "3 days", "2 weeks"

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private ProposalStatus status = ProposalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "moderation_status", length = 20)
    private ModerationStatus moderationStatus = ModerationStatus.PENDING_APPROVAL;

    @Column(length = 1000)
    private String clientNote;                  // Client's internal notes

    @Builder.Default
    @Column(nullable = false)
    private Integer revisionLimit = 3;          // Max free "Request Changes" (e.g. 3)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
