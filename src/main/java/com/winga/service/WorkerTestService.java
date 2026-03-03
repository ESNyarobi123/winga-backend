package com.winga.service;

import com.winga.entity.QualificationTest;
import com.winga.entity.User;
import com.winga.entity.WorkerTestResult;
import com.winga.repository.QualificationTestRepository;
import com.winga.repository.WorkerTestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.winga.dto.response.WorkerTestResultResponse;

@Service
@RequiredArgsConstructor
public class WorkerTestService {

    private final QualificationTestRepository qualificationTestRepository;
    private final WorkerTestResultRepository workerTestResultRepository;

    /**
     * For worker my-tests page: all active tests with my result (attempts, best score, status).
     * Completed tests are "added to profile" (status COMPLETED).
     */
    @Transactional(readOnly = true)
    public List<WorkerTestResultResponse> getMyResults(Long userId) {
        List<QualificationTest> tests = qualificationTestRepository.findByIsActiveTrueOrderBySortOrderAsc();
        List<WorkerTestResult> results = workerTestResultRepository.findByUserIdOrderByTestId(userId);
        Map<Long, WorkerTestResult> resultByTestId = results.stream().collect(Collectors.toMap(r -> r.getTest().getId(), r -> r));

        List<WorkerTestResultResponse> list = new ArrayList<>();
        for (QualificationTest t : tests) {
            WorkerTestResult r = resultByTestId.get(t.getId());
            list.add(toResponse(t, r));
        }
        return list;
    }

    /**
     * Submit score for a test. Creates or updates WorkerTestResult. If score >= minScore, marks COMPLETED (added to profile).
     */
    @Transactional
    public WorkerTestResultResponse submitScore(User user, Long testId, int score) {
        QualificationTest test = qualificationTestRepository.findById(testId)
                .orElseThrow(() -> new com.winga.exception.ResourceNotFoundException("QualificationTest", testId));
        if (!Boolean.TRUE.equals(test.getIsActive())) {
            throw new com.winga.exception.BusinessException("This test is not available.");
        }

        WorkerTestResult result = workerTestResultRepository.findByUserIdAndTestId(user.getId(), testId).orElse(null);
        if (result == null) {
            result = WorkerTestResult.builder()
                    .user(user)
                    .test(test)
                    .attemptsCount(0)
                    .bestScore(null)
                    .status("PENDING")
                    .build();
        }

        if (result.getAttemptsCount() >= test.getMaxAttempts()) {
            throw new com.winga.exception.BusinessException("Maximum attempts (" + test.getMaxAttempts() + ") reached for this test.");
        }

        result.setAttemptsCount(result.getAttemptsCount() + 1);
        int prevBest = result.getBestScore() != null ? result.getBestScore() : -1;
        result.setBestScore(Math.max(prevBest, score));

        if ("PENDING".equals(result.getStatus()) && score >= test.getMinScore()) {
            result.setStatus("COMPLETED");
            result.setCompletedAt(LocalDateTime.now());
        }

        result = workerTestResultRepository.save(result);
        return toResponse(test, result);
    }

    /** Completed test results for this user (for profile display). */
    @Transactional(readOnly = true)
    public List<WorkerTestResultResponse> getMyCompleted(Long userId) {
        return workerTestResultRepository.findByUserIdAndStatus(userId, "COMPLETED").stream()
                .map(r -> toResponse(r.getTest(), r))
                .toList();
    }

    private static WorkerTestResultResponse toResponse(QualificationTest t, WorkerTestResult r) {
        if (r == null) {
            return new WorkerTestResultResponse(
                    t.getId(), t.getName(), t.getSlug(), t.getTestType(),
                    t.getMinScore(), t.getMaxScore(), t.getMaxAttempts(),
                    0, null, "PENDING", null, false);
        }
        return new WorkerTestResultResponse(
                t.getId(), t.getName(), t.getSlug(), t.getTestType(),
                t.getMinScore(), t.getMaxScore(), t.getMaxAttempts(),
                r.getAttemptsCount(), r.getBestScore(), r.getStatus(),
                r.getCompletedAt(), "COMPLETED".equals(r.getStatus()));
    }
}
