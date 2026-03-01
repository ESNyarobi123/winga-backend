package com.winga.dto.response;

/**
 * Average rating and review count for a user (profile display).
 */
public record RatingSummaryResponse(
        double averageRating,
        long reviewCount
) {}
