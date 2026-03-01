package com.winga.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CV / extended profile (One-to-One with User).
 * skills: store as JSON array string e.g. ["Java", "Spring Boot", "MySQL"] for search/display.
 */
@Entity
@Table(name = "profiles", indexes = {
        @Index(name = "idx_profile_user", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 200)
    private String headline;           // e.g. "Senior React Developer"

    @Column(columnDefinition = "TEXT")
    private String bio;               // Long bio

    /** JSON array of skills: ["Java", "Spring Boot", "MySQL"] — stored as TEXT for compatibility */
    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(precision = 18, scale = 2)
    private BigDecimal hourlyRate;

    @Column(length = 500)
    private String portfolioUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
