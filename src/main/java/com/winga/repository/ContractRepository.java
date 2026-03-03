package com.winga.repository;

import com.winga.entity.Contract;
import com.winga.domain.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Page<Contract> findByClientId(Long clientId, Pageable pageable);

    Page<Contract> findByFreelancerId(Long freelancerId, Pageable pageable);

    List<Contract> findByFreelancerIdAndStatus(Long freelancerId, ContractStatus status);

    long countByFreelancerIdAndStatusIn(Long freelancerId, Collection<ContractStatus> statuses);

    List<Contract> findByClientIdAndStatus(Long clientId, ContractStatus status);

    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

    boolean existsByJobIdAndStatus(Long jobId, ContractStatus status);

    @Query("SELECT SUM(c.platformFeeCollected) FROM Contract c WHERE c.status = 'COMPLETED'")
    BigDecimal totalPlatformRevenue();

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    long countByStatus(ContractStatus status);

    @Query("SELECT COALESCE(SUM(c.platformFeeCollected), 0) FROM Contract c WHERE c.status = 'COMPLETED' AND c.completedAt BETWEEN :from AND :to")
    BigDecimal revenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
