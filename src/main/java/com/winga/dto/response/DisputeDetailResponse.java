package com.winga.dto.response;

import java.util.List;

/**
 * Admin dispute view: original scope (contract + job + milestones) + recent chat.
 */
public record DisputeDetailResponse(
        ContractResponse contract,
        String jobDescription,
        List<ChatMessageResponse> recentMessages
) {}
