package com.winga.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Uniform paginated envelope: { data, total, page, limit, totalPages }.
 * Use when frontend expects this shape instead of Spring's Page.
 */
public record PaginatedResponse<T>(
        List<T> data,
        long total,
        int page,
        int limit,
        int totalPages
) {
    public static <T> PaginatedResponse<T> of(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
    }
}
