package com.winga.dto.request;

import java.util.List;

/** Optional filter for admin export workers: export only selected user IDs (must be FREELANCER). */
public record AdminExportWorkersRequest(
        List<Long> userIds
) {}
