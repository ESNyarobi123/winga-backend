package com.winga.dto.response;

import com.winga.domain.enums.ContractStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ContractResponse(
        Long id,
        Long jobId,
        String jobTitle,
        UserResponse client,
        UserResponse freelancer,
        BigDecimal totalAmount,
        BigDecimal escrowAmount,
        BigDecimal addonEscrowAmount,
        BigDecimal releasedAmount,
        BigDecimal platformFeeCollected,
        ContractStatus status,
        Integer revisionLimit,
        Integer revisionsUsed,
        String terminationReason,
        List<MilestoneResponse> milestones,
        LocalDateTime createdAt,
        LocalDateTime completedAt) {
}
