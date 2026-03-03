package com.winga.dto.response;

import com.winga.domain.enums.FilterOptionType;

import java.time.LocalDateTime;

public record FilterOptionResponse(
        Long id,
        FilterOptionType type,
        String name,
        String slug,
        int sortOrder,
        LocalDateTime createdAt
) {}
