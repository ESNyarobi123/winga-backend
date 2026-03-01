package com.winga.dto.response;

import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.ModerationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JobResponse(
        Long id,
        String title,
        String description,
        BigDecimal budget,
        LocalDate deadline,
        JobStatus status,
        List<String> tags,
        String category,
        String experienceLevel,
        Long viewCount,
        Long proposalCount,
        UserResponse client,
        LocalDateTime createdAt,
        ModerationStatus moderationStatus,
        Boolean isFeatured,
        Boolean isBoostedTelegram,
        String city,
        String region,
        BigDecimal latitude,
        BigDecimal longitude,
        List<String> attachmentUrls
) {
    public JobResponse(Long id, String title, String description, BigDecimal budget, LocalDate deadline,
                       JobStatus status, List<String> tags, String category, String experienceLevel,
                       Long viewCount, Long proposalCount, UserResponse client, LocalDateTime createdAt) {
        this(id, title, description, budget, deadline, status, tags, category, experienceLevel,
                viewCount, proposalCount, client, createdAt, null, null, null, null, null, null, null, null);
    }
}
