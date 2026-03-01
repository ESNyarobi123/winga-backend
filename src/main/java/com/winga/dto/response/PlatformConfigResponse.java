package com.winga.dto.response;

import java.math.BigDecimal;

public record PlatformConfigResponse(
        BigDecimal commissionRatePercent,
        String currency,
        boolean subscriptionRequiredForBid,
        BigDecimal mpesaMarginPercent,
        BigDecimal tigoMarginPercent,
        BigDecimal airtelMarginPercent) {}
