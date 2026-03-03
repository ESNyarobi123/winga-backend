package com.winga.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SubscriptionPlanRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String slug,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0") BigDecimal price,
        @Size(max = 10) String currency,
        @NotNull Integer durationDays,
        Boolean isActive,
        Integer sortOrder
) {
    public String currency() {
        return currency != null && !currency.isBlank() ? currency : "TZS";
    }

    public Boolean isActive() {
        return isActive != null ? isActive : true;
    }

    public Integer sortOrder() {
        return sortOrder != null ? sortOrder : 0;
    }
}
