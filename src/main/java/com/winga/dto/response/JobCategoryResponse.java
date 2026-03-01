package com.winga.dto.response;

import java.time.LocalDateTime;

public record JobCategoryResponse(
        Long id,
        String name,
        String slug,
        int sortOrder,
        LocalDateTime createdAt
) {}
