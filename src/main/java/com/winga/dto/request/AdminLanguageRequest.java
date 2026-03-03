package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin: add or edit a language (used in find-jobs / find-workers filters).
 */
public record AdminLanguageRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String slug,
        Integer sortOrder
) {}
