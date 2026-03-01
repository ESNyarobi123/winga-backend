package com.winga.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AdminCreateJobRequest(
        @NotNull Long clientId,
        @NotBlank @Size(min = 10, max = 200) String title,
        @NotBlank @Size(min = 30) String description,
        @NotNull @DecimalMin("0") BigDecimal budget,
        LocalDate deadline,
        List<String> tags,
        String category,
        String experienceLevel
) {}
