package com.winga.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionPlanResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String currency,
        Integer durationDays,
        Boolean isActive,
        Integer sortOrder,
        LocalDateTime createdAt
) {}
