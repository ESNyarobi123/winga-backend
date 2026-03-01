package com.winga.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Submit a review for the other party after contract is completed.
 */
public record ReviewRequest(
        @NotNull
        @Min(1) @Max(5)
        Integer rating,
        String comment
) {}
