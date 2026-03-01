package com.winga.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request to initiate M-Pesa STK push (user will complete payment on phone).
 */
public record InitiateDepositRequest(
        @NotNull
        @DecimalMin(value = "1000", message = "Minimum deposit is 1000 TZS")
        BigDecimal amount,
        @NotBlank(message = "M-Pesa phone number required (e.g. 255712345678)")
        String phoneNumber
) {}
