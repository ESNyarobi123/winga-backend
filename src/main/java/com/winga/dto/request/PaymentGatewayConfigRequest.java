package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Admin: create or update payment gateway config (API keys, settings as key-value).
 */
public record PaymentGatewayConfigRequest(
        @NotBlank @Size(max = 50) String gatewaySlug,
        @NotBlank @Size(max = 100) String displayName,
        Map<String, String> config,
        Boolean isActive
) {}
