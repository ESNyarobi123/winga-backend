package com.winga.repository;

import com.winga.entity.WalletTransaction;
import com.winga.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    Page<WalletTransaction> findByWalletIdAndTransactionType(Long walletId, TransactionType type, Pageable pageable);
}
