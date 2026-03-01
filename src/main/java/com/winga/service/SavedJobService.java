package com.winga.service;

import com.winga.entity.Job;
import com.winga.entity.SavedJob;
import com.winga.entity.User;
import com.winga.dto.response.JobResponse;
import com.winga.exception.BusinessException;
import com.winga.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobService jobService;

    @Transactional
    public void saveJob(Long jobId, User user) {
        Job job = jobService.getJobOrThrow(jobId);
        if (savedJobRepository.existsByUserIdAndJobId(user.getId(), jobId)) {
            throw new BusinessException("Job already saved.");
        }
        SavedJob saved = SavedJob.builder()
                .user(user)
                .job(job)
                .build();
        savedJobRepository.save(saved);
        log.debug("User {} saved job {}", user.getId(), jobId);
    }

    @Transactional
    public void unsaveJob(Long jobId, User user) {
        savedJobRepository.deleteByUserIdAndJobId(user.getId(), jobId);
        log.debug("User {} unsaved job {}", user.getId(), jobId);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getMySavedJobs(Long userId, Pageable pageable) {
        return savedJobRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(sj -> jobService.toJobResponse(sj.getJob()));
    }

    @Transactional(readOnly = true)
    public boolean isSavedByUser(Long userId, Long jobId) {
        return savedJobRepository.existsByUserIdAndJobId(userId, jobId);
    }
}
