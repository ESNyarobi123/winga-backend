package com.winga.dto.request;

import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.ModerationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AdminUpdateJobRequest(
        @Size(min = 10, max = 200) String title,
        @Size(min = 30) String description,
        @DecimalMin("0") BigDecimal budget,
        LocalDate deadline,
        List<String> tags,
        String category,
        String experienceLevel,
        String employmentType,
        String socialMedia,
        String software,
        String language,
        JobStatus status,
        ModerationStatus moderationStatus,
        Boolean isFeatured,
        Boolean isBoostedTelegram
) {}
