package com.winga.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MilestoneRequest(
        @NotBlank @Size(max = 200) String title,

        String description,

        @NotNull @DecimalMin("500.00") BigDecimal amount,

        String dueDate,

        @NotNull @Min(0) Integer orderIndex) {
}
