package com.winga.dto.response;

import java.math.BigDecimal;

/**
 * Platform stats for admin dashboard (Guru/Upwork-style).
 */
public record AdminStatsResponse(
        long totalUsers,
        long totalClients,
        long totalFreelancers,
        long openJobs,
        long totalJobs,
        long activeContracts,
        long completedContracts,
        long disputedContracts,
        BigDecimal totalPlatformRevenue
) {}
