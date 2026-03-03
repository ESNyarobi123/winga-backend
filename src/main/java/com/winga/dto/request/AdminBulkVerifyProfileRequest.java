package com.winga.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Admin bulk verify/unverify worker profiles. */
public record AdminBulkVerifyProfileRequest(
        @NotNull List<Long> userIds,
        boolean verified
) {}
