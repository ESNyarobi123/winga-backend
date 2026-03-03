package com.winga.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Restricts sort to allowed fields for jobs and workers (avoids arbitrary column sort).
 */
public final class SortUtils {

    private static final Set<String> JOB_ALLOWED = Set.of("createdAt", "budget", "deadline", "title", "viewCount");
    private static final Set<String> WORKER_ALLOWED = Set.of("createdAt", "fullName");

    private SortUtils() {}

    /** Default: createdAt, desc. Only allows JOB_ALLOWED fields. */
    public static Pageable jobSort(Pageable pageable) {
        Sort sort = sanitizeSort(pageable.getSort(), JOB_ALLOWED, "createdAt", false);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    /** Default: createdAt, desc. Only allows WORKER_ALLOWED fields. */
    public static Pageable workerSort(Pageable pageable) {
        Sort sort = sanitizeSort(pageable.getSort(), WORKER_ALLOWED, "createdAt", false);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private static Sort sanitizeSort(Sort sort, Set<String> allowed, String defaultProperty, boolean ascending) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, defaultProperty);
        }
        Sort.Order order = sort.iterator().next();
        String property = order.getProperty();
        if (!allowed.contains(property)) {
            property = defaultProperty;
        }
        return Sort.by(order.getDirection(), property);
    }
}
