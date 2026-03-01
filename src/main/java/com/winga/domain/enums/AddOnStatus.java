package com.winga.domain.enums;

public enum AddOnStatus {
    PROPOSED,   // Freelancer proposed; client sees as "invoice"
    ACCEPTED,   // Client accepted & deposited; freelancer can deliver
    REJECTED,   // Client declined
    COMPLETED   // Client approved; payment released to freelancer
}
