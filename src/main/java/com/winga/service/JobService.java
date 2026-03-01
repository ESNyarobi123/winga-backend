package com.winga.service;

import com.winga.entity.Job;
import com.winga.entity.User;
import com.winga.domain.enums.JobStatus;
import com.winga.dto.request.JobRequest;
import com.winga.dto.response.JobResponse;
import com.winga.exception.ResourceNotFoundException;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.JobRepository;
import com.winga.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final UserService userService;

    // ─── Client: Post a Job ──────────────────────────────────────────────────────

    @Transactional
    public JobResponse createJob(User client, JobRequest request) {
        String attachmentUrlsJson = (request.attachmentUrls() != null && !request.attachmentUrls().isEmpty())
                ? String.join(",", request.attachmentUrls())
                : null;
        Job job = Job.builder()
                .client(client)
                .title(request.title())
                .description(request.description())
                .budget(request.budget())
                .deadline(request.deadline())
                .tags(request.tags() != null ? String.join(",", request.tags()) : null)
                .category(request.category())
                .experienceLevel(request.experienceLevel())
                .city(request.city())
                .region(request.region())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .attachmentUrls(attachmentUrlsJson)
                .status(JobStatus.OPEN)
                .viewCount(0L)
                .build();

        Job saved = jobRepository.save(job);
        log.info("Job posted: id={} by client={}", saved.getId(), client.getId());
        return toJobResponse(saved);
    }

    // ─── Public: Browse Jobs ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, String category,
            BigDecimal minBudget, BigDecimal maxBudget,
            Pageable pageable) {
        return jobRepository.searchJobs(JobStatus.OPEN, keyword, category, minBudget, maxBudget, pageable)
                .map(this::toJobResponse);
    }

    @Transactional
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", id));
        jobRepository.incrementViewCount(id);
        return toJobResponse(job);
    }

    // ─── Client: Manage Jobs ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<JobResponse> getClientJobs(Long clientId, Pageable pageable) {
        return jobRepository.findByClientId(clientId, pageable)
                .map(this::toJobResponse);
    }

    @Transactional
    public JobResponse updateJob(Long jobId, User client, JobRequest request) {
        Job job = getJobOrThrow(jobId);

        if (!job.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("You can only edit your own jobs.");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new com.winga.exception.BusinessException("Only OPEN jobs can be edited.");
        }

        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setBudget(request.budget());
        job.setDeadline(request.deadline());
        job.setTags(request.tags() != null ? String.join(",", request.tags()) : null);
        job.setCategory(request.category());
        job.setExperienceLevel(request.experienceLevel());
        job.setCity(request.city());
        job.setRegion(request.region());
        job.setLatitude(request.latitude());
        job.setLongitude(request.longitude());
        if (request.attachmentUrls() != null) {
            job.setAttachmentUrls(request.attachmentUrls().isEmpty() ? null : String.join(",", request.attachmentUrls()));
        }

        return toJobResponse(jobRepository.save(job));
    }

    @Transactional
    public void cancelJob(Long jobId, User client) {
        Job job = getJobOrThrow(jobId);
        if (!job.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException();
        }
        if (job.getStatus() == JobStatus.IN_PROGRESS) {
            throw new com.winga.exception.BusinessException("Cannot cancel a job that is in progress.");
        }
        job.setStatus(JobStatus.CANCELLED);
        jobRepository.save(job);
    }

    // ─── Internal helpers ────────────────────────────────────────────────────────

    public Job getJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", id));
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────────

    public JobResponse toJobResponse(Job job) {
        List<String> tags = (job.getTags() != null && !job.getTags().isBlank())
                ? Arrays.asList(job.getTags().split(","))
                : List.of();
        List<String> attachmentUrls = (job.getAttachmentUrls() != null && !job.getAttachmentUrls().isBlank())
                ? Arrays.asList(job.getAttachmentUrls().split(","))
                : List.of();
        long proposalCount = proposalRepository.countByJobId(job.getId());

        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getBudget(),
                job.getDeadline(),
                job.getStatus(),
                tags,
                job.getCategory(),
                job.getExperienceLevel(),
                job.getViewCount(),
                proposalCount,
                userService.toUserResponse(job.getClient()),
                job.getCreatedAt(),
                job.getModerationStatus(),
                job.getIsFeatured(),
                job.getIsBoostedTelegram(),
                job.getCity(),
                job.getRegion(),
                job.getLatitude(),
                job.getLongitude(),
                attachmentUrls);
    }
}
