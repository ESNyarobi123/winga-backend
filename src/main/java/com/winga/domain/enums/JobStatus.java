package com.winga.domain.enums;

public enum JobStatus {
    OPEN,
    PAUSED,       // Admin/client can pause job (no new applications)
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
