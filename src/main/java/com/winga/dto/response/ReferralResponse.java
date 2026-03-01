package com.winga.dto.response;

import java.math.BigDecimal;

public record ReferralResponse(
        String code,
        String referralLink,
        int signupCount,
        int hireCount,
        BigDecimal commissionBalance) {}
