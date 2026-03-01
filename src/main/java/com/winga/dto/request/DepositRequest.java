package com.winga.dto.request;

import com.winga.domain.enums.MobileMoneyProvider;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DepositRequest(
        @NotNull @DecimalMin("500.00") @DecimalMax("5000000.00") BigDecimal amount,

        @NotBlank @Pattern(regexp = "^(\\+255|0)[67]\\d{8}$") String phoneNumber,

        @NotNull MobileMoneyProvider provider) {
}
