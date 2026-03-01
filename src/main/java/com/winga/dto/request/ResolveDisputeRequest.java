package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Admin decision for a disputed contract: release escrow to client (refund) or freelancer (payout).
 */
public record ResolveDisputeRequest(
        @NotBlank
        @Pattern(regexp = "CLIENT|FREELANCER", message = "Must be CLIENT or FREELANCER")
        String releaseTo
) {}
