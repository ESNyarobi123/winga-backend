package com.winga.dto.response;

import com.winga.domain.enums.ModerationStatus;
import com.winga.domain.enums.ProposalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProposalResponse(
        Long id,
        Long jobId,
        String jobTitle,
        UserResponse freelancer,
        String coverLetter,
        BigDecimal bidAmount,
        String estimatedDuration,
        Integer revisionLimit,
        ProposalStatus status,
        ModerationStatus moderationStatus,
        ProposalPriceBreakdown priceBreakdown,
        LocalDateTime createdAt) {
}
