package com.winga.entity;

import com.winga.domain.enums.ModerationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service provider certification: PDF or image, issuer, date.
 */
@Entity
@Table(name = "certifications", indexes = {
        @Index(name = "idx_certification_user", columnList = "user_id"),
        @Index(name = "idx_certification_moderation", columnList = "moderation_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String issuer;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    private LocalDate issuedAt;

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
