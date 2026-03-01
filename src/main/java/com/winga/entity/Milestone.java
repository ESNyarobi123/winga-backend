package com.winga.entity;

import com.winga.domain.enums.MilestoneStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "milestones", indexes = {
        @Index(name = "idx_milestone_contract", columnList = "contract_id"),
        @Index(name = "idx_milestone_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    private LocalDate dueDate;

    @Builder.Default
    @Column(nullable = false)
    private Integer orderIndex = 0;             // Display ordering

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String submissionNote;              // Freelancer's note when submitting

    @Column(columnDefinition = "TEXT")
    private String reviewNote;                  // Client's review note

    private LocalDateTime fundedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
