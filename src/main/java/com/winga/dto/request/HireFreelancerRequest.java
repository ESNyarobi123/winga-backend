package com.winga.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Optional body when hiring a freelancer (e.g. custom amount or note).
 * Hiring is usually done via POST /api/contracts/hire/{proposalId} with no body.
 */
public record HireFreelancerRequest(
        @NotNull Long proposalId,
        BigDecimal releaseAmount,   // optional override
        String clientNote
) {
}
