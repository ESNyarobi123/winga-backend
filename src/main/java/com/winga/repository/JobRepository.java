package com.winga.repository;

import com.winga.entity.Job;
import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    Page<Job> findByClientId(Long clientId, Pageable pageable);

    Page<Job> findByModerationStatus(ModerationStatus moderationStatus, Pageable pageable);

    long countByModerationStatus(ModerationStatus moderationStatus);

    @Query("""
            SELECT j FROM Job j
            WHERE j.status = :status
              AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR j.category = :category)
              AND (:minBudget IS NULL OR j.budget >= :minBudget)
              AND (:maxBudget IS NULL OR j.budget <= :maxBudget)
            """)
    Page<Job> searchJobs(
            JobStatus status,
            String keyword,
            String category,
            BigDecimal minBudget,
            BigDecimal maxBudget,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Job j SET j.viewCount = j.viewCount + 1 WHERE j.id = :id")
    void incrementViewCount(Long id);

    List<Job> findByClientIdAndStatus(Long clientId, JobStatus status);

    long countByStatus(JobStatus status);
}
