package com.winga.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record WithdrawRequest(
        @NotNull @DecimalMin("500.00") BigDecimal amount,

        @NotBlank @Pattern(regexp = "^(\\+255|0)[67]\\d{8}$") String phoneNumber) {
}
