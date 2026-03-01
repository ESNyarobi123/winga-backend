package com.winga.dto.response;

import com.winga.domain.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        Long id,
        BigDecimal balance,
        Currency currency,
        BigDecimal totalEarned,
        BigDecimal totalSpent,
        LocalDateTime lastUpdatedAt) {
}
