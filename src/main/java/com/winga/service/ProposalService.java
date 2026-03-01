package com.winga.service;

import com.winga.entity.Job;
import com.winga.entity.Proposal;
import com.winga.entity.User;
import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.ProposalStatus;
import com.winga.domain.enums.Role;
import com.winga.dto.request.ProposalRequest;
import com.winga.dto.response.ProposalPriceBreakdown;
import com.winga.dto.response.ProposalResponse;
import com.winga.exception.BusinessException;
import com.winga.exception.ResourceNotFoundException;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final JobService jobService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    @Value("${app.platform.commission-rate:0.15}")
    private BigDecimal commissionRate;

    // ─── Freelancer: Submit Proposal ─────────────────────────────────────────────

    @Transactional
    public ProposalResponse submitProposal(Long jobId, User freelancer, ProposalRequest request) {
        Job job = jobService.getJobOrThrow(jobId);

        // Business rule: Job must be OPEN
        if (job.getStatus() != JobStatus.OPEN) {
            throw new BusinessException("This job is no longer accepting proposals.");
        }

        // Business rule: One proposal per freelancer per job
        if (proposalRepository.existsByJobIdAndFreelancerId(jobId, freelancer.getId())) {
            throw new BusinessException("You have already submitted a proposal for this job.");
        }

        // Business rule: Freelancer cannot apply to their own jobs (edge case)
        if (job.getClient().getId().equals(freelancer.getId())) {
            throw new BusinessException("You cannot apply to your own job.");
        }

        // Business rule: Freelancer (Service Provider) must have active subscription to bid
        if (freelancer.getRole() == Role.FREELANCER && !subscriptionService.hasActiveSubscription(freelancer.getId())) {
            throw new BusinessException("Active monthly subscription is required to submit proposals. Please subscribe first.");
        }

        Proposal proposal = Proposal.builder()
                .job(job)
                .freelancer(freelancer)
                .coverLetter(request.coverLetter())
                .bidAmount(request.bidAmount())
                .estimatedDuration(request.estimatedDuration())
                .revisionLimit(request.revisionLimit() != null ? request.revisionLimit() : 3)
                .status(ProposalStatus.PENDING)
                .build();

        Proposal saved = proposalRepository.save(proposal);

        // Notify client
        notificationService.notifyProposalReceived(
                job.getClient(), freelancer.getFullName(), jobId);

        log.info("Proposal submitted: proposalId={} job={} freelancer={}",
                saved.getId(), jobId, freelancer.getId());

        return toProposalResponse(saved);
    }

    // ─── Client: Manage Proposals ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProposalResponse> getJobProposals(Long jobId, User client, Pageable pageable) {
        Job job = jobService.getJobOrThrow(jobId);
        if (!job.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("You can only view proposals for your own jobs.");
        }
        return proposalRepository.findByJobId(jobId, pageable)
                .map(this::toProposalResponse);
    }

    @Transactional
    public ProposalResponse updateProposalStatus(Long proposalId, User client, ProposalStatus newStatus) {
        Proposal proposal = getProposalOrThrow(proposalId);

        if (!proposal.getJob().getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException();
        }
        if (proposal.getStatus() == ProposalStatus.HIRED) {
            throw new BusinessException("This proposal is already hired.");
        }

        proposal.setStatus(newStatus);
        return toProposalResponse(proposalRepository.save(proposal));
    }

    /** Admin can update any proposal status (shortlist, reject, etc.). */
    @Transactional
    public ProposalResponse updateProposalStatusAdmin(Long proposalId, ProposalStatus newStatus, User admin) {
        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedAccessException("Admin only.");
        }
        Proposal proposal = getProposalOrThrow(proposalId);
        if (proposal.getStatus() == ProposalStatus.HIRED) {
            throw new BusinessException("This proposal is already hired.");
        }
        proposal.setStatus(newStatus);
        return toProposalResponse(proposalRepository.save(proposal));
    }

    /** Admin: set proposal moderation status (APPROVED / REJECTED). */
    @Transactional
    public ProposalResponse moderateProposal(Long proposalId, com.winga.domain.enums.ModerationStatus moderationStatus, User admin) {
        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedAccessException("Admin only.");
        }
        Proposal proposal = getProposalOrThrow(proposalId);
        proposal.setModerationStatus(moderationStatus);
        return toProposalResponse(proposalRepository.save(proposal));
    }

    // ─── Freelancer: My Proposals ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProposalResponse> getMyProposals(Long freelancerId, Pageable pageable) {
        return proposalRepository.findByFreelancerId(freelancerId, pageable)
                .map(this::toProposalResponse);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    public Proposal getProposalOrThrow(Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal", id));
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────────

    public ProposalResponse toProposalResponse(Proposal p) {
        return new ProposalResponse(
                p.getId(),
                p.getJob().getId(),
                p.getJob().getTitle(),
                userService.toUserResponse(p.getFreelancer()),
                p.getCoverLetter(),
                p.getBidAmount(),
                p.getEstimatedDuration(),
                p.getRevisionLimit(),
                p.getStatus(),
                p.getModerationStatus(),
                buildPriceBreakdown(p.getBidAmount()),
                p.getCreatedAt());
    }

    private ProposalPriceBreakdown buildPriceBreakdown(BigDecimal bidAmount) {
        if (bidAmount == null) return null;
        BigDecimal commission = bidAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalToClient = bidAmount.add(commission);
        BigDecimal ratePercent = commissionRate.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
        return new ProposalPriceBreakdown(bidAmount, commission, totalToClient, ratePercent);
    }
}
