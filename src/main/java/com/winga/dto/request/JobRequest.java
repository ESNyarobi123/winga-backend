package com.winga.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record JobRequest(
        @NotBlank @Size(min = 10, max = 200) String title,

        @NotBlank @Size(min = 30) String description,

        @NotNull @DecimalMin("1000.00") BigDecimal budget,

        @Future LocalDate deadline,

        List<String> tags,
        String category,
        String experienceLevel, // JUNIOR, MID, SENIOR
        String city,
        String region,
        BigDecimal latitude,
        BigDecimal longitude,
        List<String> attachmentUrls
) {
}
