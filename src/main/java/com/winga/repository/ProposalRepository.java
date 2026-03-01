package com.winga.repository;

import com.winga.entity.Proposal;
import com.winga.domain.enums.ModerationStatus;
import com.winga.domain.enums.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    boolean existsByJobIdAndFreelancerId(Long jobId, Long freelancerId);

    Optional<Proposal> findByJobIdAndFreelancerId(Long jobId, Long freelancerId);

    Page<Proposal> findByJobId(Long jobId, Pageable pageable);

    Page<Proposal> findByFreelancerId(Long freelancerId, Pageable pageable);

    Page<Proposal> findByStatus(ProposalStatus status, Pageable pageable);

    List<Proposal> findByJobIdAndStatus(Long jobId, ProposalStatus status);

    Page<Proposal> findByJobIdAndStatus(Long jobId, ProposalStatus status, Pageable pageable);

    Page<Proposal> findByModerationStatus(ModerationStatus moderationStatus, Pageable pageable);

    long countByModerationStatus(ModerationStatus moderationStatus);

    long countByJobId(Long jobId);

    long countByFreelancerId(Long freelancerId);

    long countByFreelancerIdAndStatus(Long freelancerId, ProposalStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(ProposalStatus status);

    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.job.client.id = :clientId")
    long countByJobClientId(Long clientId);

    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.job.client.id = :clientId AND p.status <> 'PENDING'")
    long countByJobClientIdAndStatusNotPending(Long clientId);

    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.job.client.id = :clientId AND p.createdAt BETWEEN :start AND :end")
    long countByJobClientIdAndCreatedAtBetween(Long clientId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT DATE(p.created_at) as d, COUNT(*) as c FROM proposals p INNER JOIN jobs j ON p.job_id = j.id WHERE j.client_id = :clientId AND p.created_at >= :since GROUP BY DATE(p.created_at) ORDER BY d", nativeQuery = true)
    List<Object[]> countGroupByDateSinceForClient(@Param("clientId") Long clientId, @Param("since") LocalDateTime since);

    @Query("SELECT j.category, COUNT(p) FROM Proposal p JOIN p.job j WHERE j.client.id = :clientId AND p.createdAt >= :since AND j.category IS NOT NULL AND j.category <> '' GROUP BY j.category ORDER BY COUNT(p) DESC")
    List<Object[]> countByCategorySinceForClient(Long clientId, LocalDateTime since);

    @Query(value = "SELECT DATE(created_at) as d, COUNT(*) as c FROM proposals WHERE created_at >= :since GROUP BY DATE(created_at) ORDER BY d", nativeQuery = true)
    List<Object[]> countGroupByDateSince(@Param("since") LocalDateTime since);

    @Query("SELECT j.category, COUNT(p) FROM Proposal p JOIN p.job j WHERE p.createdAt >= :since AND j.category IS NOT NULL AND j.category <> '' GROUP BY j.category ORDER BY COUNT(p) DESC")
    List<Object[]> countByCategorySince(LocalDateTime since);
}
