package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;

public record JobCategoryRequest(
        @NotBlank String name,
        @NotBlank String slug,
        Integer sortOrder
) {}
