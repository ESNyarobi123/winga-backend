package com.winga.controller;

import com.winga.entity.User;
import com.winga.dto.request.DepositRequest;
import com.winga.dto.request.InitiateDepositRequest;
import com.winga.dto.request.WithdrawRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.InitiateDepositResponse;
import com.winga.dto.response.TransactionResponse;
import com.winga.dto.response.WalletResponse;
import com.winga.service.PaymentService;
import com.winga.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Wallet", description = "Balance, M-Pesa deposit, withdrawal, transaction history")
public class WalletController {

    private final WalletService walletService;
    private final PaymentService paymentService;

    @GetMapping("/balance")
    @Operation(summary = "Check my wallet balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success(walletService.getBalance(user.getId())));
    }

    /**
     * Initiate M-Pesa STK push. When Daraja is configured, user gets prompt on phone;
     * otherwise returns simulated ID. Callback credits wallet on success.
     */
    @PostMapping("/deposit/initiate")
    @Operation(summary = "🇹🇿 Initiate M-Pesa STK push (or simulated deposit)")
    public ResponseEntity<ApiResponse<InitiateDepositResponse>> initiateDeposit(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InitiateDepositRequest request) {
        String checkoutRequestId = paymentService.initiateDeposit(
                user, request.amount(), request.phoneNumber());
        String message = checkoutRequestId.startsWith("SIMULATED")
                ? "Simulated. Use POST /wallet/deposit/simulate to credit wallet."
                : "STK push sent. Complete payment on your phone.";
        return ResponseEntity.ok(ApiResponse.success(
                new InitiateDepositResponse(checkoutRequestId, message)));
    }

    /**
     * Algorithm 3 endpoint — simulates M-Pesa / Tigo Pesa deposit (no real STK push).
     */
    @PostMapping("/deposit/simulate")
    @Operation(summary = "🇹🇿 Simulate M-Pesa / Tigo Pesa deposit")
    public ResponseEntity<ApiResponse<WalletResponse>> simulateDeposit(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DepositRequest request) {

        WalletResponse wallet = walletService.simulateDeposit(user, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Deposit of TZS " + request.amount() + " via " +
                        request.provider() + " successful! 📱",
                wallet));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Request a withdrawal to mobile money")
    public ResponseEntity<ApiResponse<WalletResponse>> withdraw(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody WithdrawRequest request) {

        WalletResponse wallet = walletService.withdraw(user, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Withdrawal of TZS " + request.amount() + " initiated.", wallet));
    }

    @GetMapping("/transactions")
    @Operation(summary = "View wallet transaction history")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getHistory(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                walletService.getHistory(user.getId(), pageable)));
    }
}
