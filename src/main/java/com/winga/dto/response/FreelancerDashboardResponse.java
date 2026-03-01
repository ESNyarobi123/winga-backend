package com.winga.dto.response;

import com.winga.domain.enums.Currency;

import java.math.BigDecimal;

/**
 * Worker/freelancer dashboard: balance, earnings, active contracts & proposals counts.
 */
public record FreelancerDashboardResponse(
        BigDecimal balance,
        BigDecimal totalEarned,
        Currency currency,
        long activeContractsCount,
        long pendingProposalsCount) {
}
