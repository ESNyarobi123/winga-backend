package com.winga.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long contractId,
        UserResponse reviewer,
        UserResponse reviewee,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}
