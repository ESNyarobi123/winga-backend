package com.winga.dto.response;

/**
 * Public user profile + rating summary in one call (e.g. for find-workers cards).
 */
public record UserSummaryResponse(
        UserResponse user,
        double averageRating,
        long reviewCount
) {}
