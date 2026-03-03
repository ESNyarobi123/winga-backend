package com.winga.entity;

import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.JobType;
import com.winga.domain.enums.ModerationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_job_status", columnList = "status"),
        @Index(name = "idx_job_client", columnList = "client_id"),
        @Index(name = "idx_job_created", columnList = "createdAt"),
        @Index(name = "idx_job_category", columnList = "category")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal budget;

    @Column(precision = 18, scale = 2)
    private BigDecimal budgetMin;             // Range like OFM: "$3 - $10 hourly"

    @Column(precision = 18, scale = 2)
    private BigDecimal budgetMax;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private JobType type;                    // FIXED_PRICE, HOURLY

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.OPEN;

    // Tags stored as comma-separated string (denormalized for performance)
    @Column(length = 500)
    private String tags;

    @Column(length = 100)
    private String category;

    /** Filter: slug from filter_options (EMPLOYMENT_TYPE) e.g. full-time, part-time */
    @Column(name = "employment_type", length = 100)
    private String employmentType;

    /** Filter: slug from filter_options (SOCIAL_MEDIA) e.g. instagram, onlyfans */
    @Column(name = "social_media", length = 100)
    private String socialMedia;

    /** Filter: slug from filter_options (SOFTWARE) e.g. excel, photoshop */
    @Column(name = "software", length = 100)
    private String software;

    /** Filter: slug from filter_options (LANGUAGE) e.g. english, swahili */
    @Column(name = "language", length = 100)
    private String language;

    /** Job location: city */
    @Column(length = 100)
    private String city;

    /** Job location: region / mkoa */
    @Column(length = 100)
    private String region;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 50)
    private String experienceLevel;             // JUNIOR, MID, SENIOR

    /** JSON array of attachment URLs (from upload) e.g. ["/uploads/abc.jpg","/uploads/def.pdf"] */
    @Column(name = "attachment_urls", columnDefinition = "TEXT")
    private String attachmentUrls;

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    /** Moderation: PENDING_APPROVAL → APPROVED/REJECTED by Super Admin / Moderator */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "moderation_status", length = 20)
    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;

    @Builder.Default
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Builder.Default
    @Column(name = "is_boosted_telegram", nullable = false)
    private Boolean isBoostedTelegram = false;

    @Builder.Default
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proposal> proposals = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ─── Helper to increment view count ─────────────────────────────────────────
    public void incrementViewCount() {
        this.viewCount++;
    }
}
