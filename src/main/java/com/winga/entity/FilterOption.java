package com.winga.entity;

import com.winga.domain.enums.FilterOptionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Filter options for find-jobs / find-workers: Employment Type, Social Media, Software, Languages.
 * Admin-managed; public API returns lists by type.
 */
@Entity
@Table(name = "filter_options", indexes = {
        @Index(name = "idx_filter_option_type", columnList = "type"),
        @Index(name = "uq_filter_option_type_slug", columnList = "type, slug", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FilterOptionType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
