package com.winga.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Payment gateway config for admin UI. Config values are masked (***) for security.
 */
public record PaymentGatewayConfigResponse(
        Long id,
        String gatewaySlug,
        String displayName,
        Map<String, String> configMasked,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
