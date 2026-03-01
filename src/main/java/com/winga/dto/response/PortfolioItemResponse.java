package com.winga.dto.response;

import com.winga.domain.enums.ModerationStatus;

import java.time.LocalDateTime;

public record PortfolioItemResponse(
        Long id,
        String type,
        String url,
        String title,
        String description,
        Integer sortOrder,
        ModerationStatus moderationStatus,
        LocalDateTime createdAt) {}
