package com.winga.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Super Admin / Employer dashboard overview: metrics + chart data.
 */
public record AdminDashboardOverviewResponse(
        long activeJobs,
        long applicationsToday,
        long applicationsThisMonth,
        long hiresMade,
        double responseRatePercent,
        BigDecimal revenue,
        long pendingModerationCount,
        List<ChartPoint> applicationsOverTime,
        List<TopCategoryDto> topCategories
) {
    public record ChartPoint(String date, long count) {}
    public record TopCategoryDto(String categoryName, long count) {}
}
