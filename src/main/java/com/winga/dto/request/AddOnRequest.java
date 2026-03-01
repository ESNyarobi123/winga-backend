package com.winga.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Freelancer proposes extra work (add-on) from chat — title + amount.
 */
public record AddOnRequest(
        @NotBlank @NotNull String title,
        String description,
        @NotNull @DecimalMin("1000.00") BigDecimal amount
) {}
