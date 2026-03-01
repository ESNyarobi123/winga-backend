package com.winga.dto.response;

import java.math.BigDecimal;

/**
 * Price breakdown for a bid: service fee (what provider gets), platform commission (15%), total client pays.
 */
public record ProposalPriceBreakdown(
        BigDecimal providerFee,
        BigDecimal platformCommission,
        BigDecimal totalToClient,
        BigDecimal commissionRatePercent) {}
