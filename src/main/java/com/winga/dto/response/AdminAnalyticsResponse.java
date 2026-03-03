package com.winga.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Admin dashboard analytics: jobs per category, proposals per job, revenue in period.
 */
public record AdminAnalyticsResponse(
        List<Map<String, Object>> jobsPerCategory,
        List<Map<String, Object>> proposalsPerJob,
        BigDecimal revenueInPeriod,
        String periodFrom,
        String periodTo
) {}
