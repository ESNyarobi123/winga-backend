package com.winga.repository;

import com.winga.entity.WorkerTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerTestResultRepository extends JpaRepository<WorkerTestResult, Long> {

    List<WorkerTestResult> findByUserIdOrderByTestId(Long userId);

    Optional<WorkerTestResult> findByUserIdAndTestId(Long userId, Long testId);

    List<WorkerTestResult> findByUserIdAndStatus(Long userId, String status);
}
