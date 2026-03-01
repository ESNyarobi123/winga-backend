package com.winga.dto.request;

import com.winga.domain.enums.ProposalStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminBulkProposalStatusRequest(
        @NotEmpty List<Long> proposalIds,
        @NotNull ProposalStatus status
) {}
