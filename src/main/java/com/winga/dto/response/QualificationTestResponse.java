package com.winga.dto.response;

import java.time.LocalDateTime;

public record QualificationTestResponse(
        Long id,
        String name,
        String slug,
        String testType,
        Integer minScore,
        Integer maxScore,
        Integer maxAttempts,
        Boolean isActive,
        Integer sortOrder,
        LocalDateTime createdAt
) {}
