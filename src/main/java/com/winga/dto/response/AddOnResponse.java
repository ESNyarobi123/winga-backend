package com.winga.dto.response;

import com.winga.domain.enums.AddOnStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AddOnResponse(
        Long id,
        Long contractId,
        String title,
        String description,
        BigDecimal amount,
        AddOnStatus status,
        LocalDateTime acceptedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {}
