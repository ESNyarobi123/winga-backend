package com.winga.dto.response;

import java.time.LocalDateTime;

public record WorkExperienceResponse(
        Long id,
        String title,
        String company,
        String startDate,
        String endDate,
        String description,
        java.util.List<String> skillsLearned,
        LocalDateTime createdAt
) {}
