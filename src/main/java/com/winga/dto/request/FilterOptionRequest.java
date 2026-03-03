package com.winga.dto.request;

import com.winga.domain.enums.FilterOptionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FilterOptionRequest(
        @NotNull FilterOptionType type,
        @NotBlank String name,
        @NotBlank String slug,
        Integer sortOrder
) {}
