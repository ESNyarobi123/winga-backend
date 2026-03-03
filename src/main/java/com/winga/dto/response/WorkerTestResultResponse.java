package com.winga.dto.response;

import java.time.LocalDateTime;

public record WorkerTestResultResponse(
        Long testId,
        String testName,
        String testSlug,
        String testType,
        Integer minScore,
        Integer maxScore,
        Integer maxAttempts,
        Integer attemptsCount,
        Integer bestScore,
        String status,
        LocalDateTime completedAt,
        Boolean addedToProfile
) {}
