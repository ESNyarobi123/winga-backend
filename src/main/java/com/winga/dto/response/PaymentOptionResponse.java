package com.winga.dto.response;

import java.time.LocalDateTime;

public record PaymentOptionResponse(
        Long id,
        String name,
        String slug,
        String description,
        Boolean isActive,
        Integer sortOrder,
        LocalDateTime createdAt
) {}
