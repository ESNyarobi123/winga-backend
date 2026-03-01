package com.winga.dto.response;

import com.winga.domain.enums.MilestoneStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MilestoneResponse(
        Long id,
        String title,
        String description,
        BigDecimal amount,
        LocalDate dueDate,
        Integer orderIndex,
        MilestoneStatus status,
        LocalDateTime fundedAt,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt) {
}
