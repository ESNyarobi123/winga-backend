package com.winga.dto.request;

import com.winga.domain.enums.ModerationStatus;
import jakarta.validation.constraints.NotNull;

public record ModerateJobRequest(
        @NotNull ModerationStatus status,
        String rejectReason
) {}
