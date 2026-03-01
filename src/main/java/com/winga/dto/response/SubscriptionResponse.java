package com.winga.dto.response;

import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long id,
        String planId,
        String status,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        boolean active) {}
