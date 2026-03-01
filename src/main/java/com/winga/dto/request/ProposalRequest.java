package com.winga.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** revisionLimit: idadi ya marekebisho ya bure (default 3). */
public record ProposalRequest(
        @NotBlank @Size(min = 50, message = "Cover letter must be at least 50 characters") String coverLetter,

        @NotNull @DecimalMin("500.00") BigDecimal bidAmount,

        @NotBlank @Size(max = 100) String estimatedDuration,

        @Min(0) @Max(10) Integer revisionLimit) {
}
