package com.winga.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Employer (CLIENT) dashboard overview: my jobs, my applications, hires, response rate.
 */
public record EmployerDashboardOverviewResponse(
        long activeJobs,
        long applicationsToday,
        long applicationsThisMonth,
        long hiresMade,
        double responseRatePercent,
        List<ChartPoint> applicationsOverTime,
        List<TopCategoryDto> topCategories
) {
    public record ChartPoint(String date, long count) {}
    public record TopCategoryDto(String categoryName, long count) {}
}
