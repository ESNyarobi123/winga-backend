package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QualificationTestRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 100) String slug,
        @NotBlank @Size(max = 50) String testType,
        @NotNull Integer minScore,
        @NotNull Integer maxScore,
        @NotNull Integer maxAttempts,
        Boolean isActive,
        Integer sortOrder
) {
    public Boolean isActive() {
        return isActive != null ? isActive : true;
    }

    public Integer sortOrder() {
        return sortOrder != null ? sortOrder : 0;
    }
}
