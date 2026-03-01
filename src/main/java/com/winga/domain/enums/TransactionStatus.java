package com.winga.domain.enums;

/**
 * Wallet transaction lifecycle (e.g. M-Pesa pending → completed/failed).
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}
