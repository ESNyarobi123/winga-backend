package com.winga.repository;

import com.winga.entity.SavedWorker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedWorkerRepository extends JpaRepository<SavedWorker, Long> {

    Page<SavedWorker> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndWorkerId(Long userId, Long workerId);

    void deleteByUserIdAndWorkerId(Long userId, Long workerId);
}
