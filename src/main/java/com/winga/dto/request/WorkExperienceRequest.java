package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkExperienceRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,
        @Size(max = 200)
        String company,
        @Size(max = 20)
        String startDate,
        @Size(max = 20)
        String endDate,
        @Size(max = 2000)
        String description,
        /** Skills learned tags e.g. ["General - Intermediate", "Sales - Intermediate"] */
        java.util.List<String> skillsLearned
) {}
