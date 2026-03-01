package com.winga.dto.response;

import com.winga.domain.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String actionUrl,
        boolean isRead,
        String referenceId,
        String referenceType,
        LocalDateTime createdAt
) {}
