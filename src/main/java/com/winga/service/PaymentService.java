package com.winga.service;

import com.winga.entity.User;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Payment (M-Pesa / Escrow) operations.
 * Logic ya Escrow inakaa hapa; implementation in service/impl.
 */
public interface PaymentService {

    /**
     * Handle M-Pesa (or gateway) callback — validate and credit wallet.
     * Called by PaymentController when gateway sends webhook.
     */
    void handleMpesaCallback(Map<String, Object> callbackPayload);

    /**
     * Initiate STK push or similar for deposit (optional; can be in WalletService).
     */
    String initiateDeposit(User user, BigDecimal amount, String phoneNumber);
}
