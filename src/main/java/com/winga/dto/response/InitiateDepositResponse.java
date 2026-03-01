package com.winga.dto.response;

/**
 * After initiating STK push: checkout request ID (and optional message).
 */
public record InitiateDepositResponse(
        String checkoutRequestId,
        String message
) {}
