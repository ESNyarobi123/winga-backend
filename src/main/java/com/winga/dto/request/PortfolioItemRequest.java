package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortfolioItemRequest(
        @NotBlank @Size(max = 20) String type, // IMAGE, VIDEO, PROJECT
        @NotBlank @Size(max = 500) String url,
        @Size(max = 200) String title,
        String description,
        Integer sortOrder) {}
