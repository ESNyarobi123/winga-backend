package com.winga.service;

import com.winga.entity.User;
import com.winga.entity.Wallet;
import com.winga.entity.WalletTransaction;
import com.winga.domain.enums.TransactionType;
import com.winga.dto.request.DepositRequest;
import com.winga.dto.request.WithdrawRequest;
import com.winga.dto.response.TransactionResponse;
import com.winga.dto.response.WalletResponse;
import com.winga.exception.BusinessException;
import com.winga.exception.InsufficientFundsException;
import com.winga.exception.ResourceNotFoundException;
import com.winga.repository.WalletRepository;
import com.winga.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    // ─── Balance ─────────────────────────────────────────────────────────────────

    public WalletResponse getBalance(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        return toWalletResponse(wallet);
    }

    // ─── Algorithm 3: Mobile Money Deposit Simulation ────────────────────────────
    /**
     * Simulates an M-Pesa / Tigo Pesa callback.
     * In production, this would be triggered by the payment gateway webhook.
     */
    @Transactional
    public WalletResponse simulateDeposit(User user, DepositRequest request) {
        // Pessimistic lock to prevent concurrent deposit race conditions
        Wallet wallet = walletRepository.findByUserIdWithLock(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + user.getId()));

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.credit(request.amount());
        wallet.setTotalSpent(wallet.getTotalSpent()); // no change
        walletRepository.save(wallet);

        // Log the transaction
        recordTransaction(wallet, TransactionType.DEPOSIT, request.amount(),
                balanceBefore, wallet.getBalance(),
                "Mobile money deposit via " + request.provider().name(),
                request.provider().name(),
                "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        log.info("Deposit simulated: user={} amount={} provider={}",
                user.getId(), request.amount(), request.provider());

        return toWalletResponse(wallet);
    }

    // ─── Withdrawal ──────────────────────────────────────────────────────────────

    @Transactional
    public WalletResponse withdraw(User user, WithdrawRequest request) {
        Wallet wallet = walletRepository.findByUserIdWithLock(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (!wallet.hasSufficientFunds(request.amount())) {
            throw new InsufficientFundsException(
                    "Insufficient balance. Available: TZS " + wallet.getBalance());
        }

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.debit(request.amount());
        walletRepository.save(wallet);

        recordTransaction(wallet, TransactionType.WITHDRAWAL, request.amount(),
                balanceBefore, wallet.getBalance(),
                "Withdrawal to " + request.phoneNumber(),
                null, "WD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        log.info("Withdrawal: user={} amount={}", user.getId(), request.amount());
        return toWalletResponse(wallet);
    }

    // ─── Internal Credit/Debit (used by EscrowService) ──────────────────────────

    @Transactional
    public void creditWallet(Long userId, BigDecimal amount, String description, String referenceId) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));

        BigDecimal before = wallet.getBalance();
        wallet.credit(amount);
        wallet.setTotalEarned(wallet.getTotalEarned().add(amount));
        walletRepository.save(wallet);

        recordTransaction(wallet, TransactionType.ESCROW_RELEASE, amount, before, wallet.getBalance(),
                description, null, referenceId);
    }

    @Transactional
    public void debitWallet(Long userId, BigDecimal amount, String description, String referenceId) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));

        if (!wallet.hasSufficientFunds(amount)) {
            throw new InsufficientFundsException();
        }

        BigDecimal before = wallet.getBalance();
        wallet.debit(amount);
        wallet.setTotalSpent(wallet.getTotalSpent().add(amount));
        walletRepository.save(wallet);

        recordTransaction(wallet, TransactionType.ESCROW_LOCK, amount, before, wallet.getBalance(),
                description, null, referenceId);
    }

    public boolean hasSufficientFunds(Long userId, BigDecimal amount) {
        return getWalletByUserId(userId).hasSufficientFunds(amount);
    }

    // ─── Transaction History ─────────────────────────────────────────────────────

    public Page<TransactionResponse> getHistory(Long userId, Pageable pageable) {
        Wallet wallet = getWalletByUserId(userId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                .map(this::toTransactionResponse);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }

    private void recordTransaction(Wallet wallet, TransactionType type, BigDecimal amount,
            BigDecimal before, BigDecimal after,
            String description, String provider, String extRef) {
        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet)
                .transactionType(type)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .description(description)
                .provider(provider)
                .externalTransactionId(extRef)
                .build();
        transactionRepository.save(tx);
    }

    private WalletResponse toWalletResponse(Wallet w) {
        return new WalletResponse(w.getId(), w.getBalance(), w.getCurrency(),
                w.getTotalEarned(), w.getTotalSpent(), w.getLastUpdatedAt());
    }

    private TransactionResponse toTransactionResponse(WalletTransaction tx) {
        return new TransactionResponse(tx.getId(), tx.getTransactionType(), tx.getAmount(),
                tx.getBalanceBefore(), tx.getBalanceAfter(), tx.getDescription(),
                tx.getProvider(), tx.getCreatedAt());
    }
}
