package com.winga.service;

import com.winga.entity.*;
import com.winga.domain.enums.ContractStatus;
import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.MilestoneStatus;
import com.winga.domain.enums.ProposalStatus;
import com.winga.dto.request.MilestoneRequest;
import com.winga.dto.response.*;
import com.winga.exception.BusinessException;
import com.winga.exception.InsufficientFundsException;
import com.winga.exception.ResourceNotFoundException;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.ContractRepository;
import com.winga.repository.JobRepository;
import com.winga.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final MilestoneRepository milestoneRepository;
    private final JobRepository jobRepository;
    private final WalletService walletService;
    private final ProposalService proposalService;
    private final JobService jobService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Value("${app.platform.commission-rate:0.15}")
    private BigDecimal commissionRate;

    // ═══════════════════════════════════════════════════════════════════════════
    // ALGORITHM 1: Hiring & Escrow Locking
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Client hires a freelancer by accepting their proposal.
     *
     * Flow:
     * 1. Validate client wallet balance >= bid amount
     * 2. Deduct bid amount from client wallet (escrow lock)
     * 3. Create Contract with escrowAmount = bidAmount
     * 4. Set Job → IN_PROGRESS, Proposal → HIRED
     * 5. Notify freelancer
     */
    @Transactional
    public ContractResponse hireFreelancer(Long proposalId, User client) {
        Proposal proposal = proposalService.getProposalOrThrow(proposalId);
        Job job = proposal.getJob();

        // ── Validations ──────────────────────────────────────────────────────────
        if (!job.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("You can only hire for your own jobs.");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new BusinessException("Job is no longer open for hiring.");
        }
        if (proposal.getStatus() != ProposalStatus.PENDING &&
                proposal.getStatus() != ProposalStatus.SHORTLISTED) {
            throw new BusinessException("This proposal cannot be hired in its current state.");
        }
        if (contractRepository.existsByJobIdAndStatus(job.getId(), ContractStatus.ACTIVE)) {
            throw new BusinessException("This job already has an active contract.");
        }

        BigDecimal bidAmount = proposal.getBidAmount();

        // ── Step 1: Check wallet balance ──────────────────────────────────────────
        if (!walletService.hasSufficientFunds(client.getId(), bidAmount)) {
            throw new InsufficientFundsException(
                    "Insufficient balance. You need TZS " + bidAmount +
                            " to hire this freelancer. Please deposit funds first.");
        }

        // ── Step 2: Lock funds in escrow ──────────────────────────────────────────
        walletService.debitWallet(
                client.getId(),
                bidAmount,
                "Escrow lock for job: " + job.getTitle(),
                "P-" + proposalId);

        // ── Step 3: Create Contract ───────────────────────────────────────────────
        Contract contract = Contract.builder()
                .job(job)
                .client(client)
                .freelancer(proposal.getFreelancer())
                .proposal(proposal)
                .totalAmount(bidAmount)
                .escrowAmount(bidAmount)
                .releasedAmount(BigDecimal.ZERO)
                .revisionLimit(proposal.getRevisionLimit() != null ? proposal.getRevisionLimit() : 3)
                .revisionsUsed(0)
                .status(ContractStatus.ACTIVE)
                .build();

        Contract savedContract = contractRepository.save(contract);

        // ── Step 4: Update Job and Proposal statuses ──────────────────────────────
        job.setStatus(JobStatus.IN_PROGRESS);
        jobService.getJobOrThrow(job.getId()); // touch to trigger save via cascade
        proposal.setStatus(ProposalStatus.HIRED);
        proposalService.getProposalOrThrow(proposalId); // same

        // Use repositories directly since we have the managed entities
        log.info("Contract created: id={} job={} freelancer={}",
                savedContract.getId(), job.getId(), proposal.getFreelancer().getId());

        // ── Step 5: Notify freelancer ─────────────────────────────────────────────
        notificationService.notifyHired(
                proposal.getFreelancer(),
                job.getTitle(),
                savedContract.getId());

        return toContractResponse(savedContract);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ALGORITHM 2a: Freelancer Submits Work
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public ContractResponse submitWork(Long contractId, User freelancer, String submissionNote) {
        Contract contract = getContractOrThrow(contractId);

        if (!contract.getFreelancer().getId().equals(freelancer.getId())) {
            throw new UnauthorizedAccessException("Only the hired freelancer can submit work.");
        }
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException("Cannot submit work. Contract status: " + contract.getStatus());
        }

        contract.setStatus(ContractStatus.REVIEW_PENDING);
        contractRepository.save(contract);

        // Notify client
        notificationService.notifyWorkSubmitted(
                contract.getClient(),
                freelancer.getFullName(),
                contractId);

        log.info("Work submitted for contract: {}", contractId);
        return toContractResponse(contract);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ALGORITHM 2b: Client Approves Work → Escrow Release + Commission
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Full payout algorithm:
     * 1. platformFee = escrowAmount × 10%
     * 2. freelancerPay = escrowAmount - platformFee
     * 3. Transfer freelancerPay → Freelancer wallet
     * 4. Record platformFee as platform revenue
     * 5. Set contract COMPLETED, escrowAmount = 0
     */
    @Transactional
    public ContractResponse approveWork(Long contractId, User client) {
        Contract contract = getContractOrThrow(contractId);

        if (!contract.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("Only the client can approve work.");
        }
        if (contract.getStatus() != ContractStatus.REVIEW_PENDING &&
                contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException("Work cannot be approved. Current status: " + contract.getStatus());
        }

        BigDecimal escrowAmount = contract.getEscrowAmount();

        if (escrowAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("No funds in escrow to release.");
        }

        // ── Commission Calculation ────────────────────────────────────────────────
        BigDecimal platformFee = escrowAmount
                .multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal freelancerPay = escrowAmount.subtract(platformFee);

        // ── Transfer to Freelancer ────────────────────────────────────────────────
        walletService.creditWallet(
                contract.getFreelancer().getId(),
                freelancerPay,
                "Payment for job: " + contract.getJob().getTitle(),
                "C-" + contractId);

        // ── Update Contract ───────────────────────────────────────────────────────
        contract.setReleasedAmount(contract.getReleasedAmount().add(freelancerPay));
        contract.setPlatformFeeCollected(contract.getPlatformFeeCollected().add(platformFee));
        contract.setEscrowAmount(BigDecimal.ZERO);
        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletedAt(LocalDateTime.now());

        // ── Mark Job COMPLETED ────────────────────────────────────────────────────
        Job job = contract.getJob();
        job.setStatus(JobStatus.COMPLETED);

        contractRepository.save(contract);

        log.info("Contract {} completed. FreelancerPay={} PlatformFee={}",
                contractId, freelancerPay, platformFee);

        // ── Notify freelancer ─────────────────────────────────────────────────────
        notificationService.notifyPaymentReleased(
                contract.getFreelancer(),
                freelancerPay.toPlainString(),
                contractId);

        return toContractResponse(contract);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Milestone Management
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public MilestoneResponse approveMilestone(Long milestoneId, User client) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", milestoneId));

        Contract contract = milestone.getContract();

        if (!contract.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException();
        }
        if (milestone.getStatus() != MilestoneStatus.IN_REVIEW) {
            throw new BusinessException("Milestone is not in review state.");
        }

        BigDecimal milestoneAmount = milestone.getAmount();
        BigDecimal platformFee = milestoneAmount
                .multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal freelancerPay = milestoneAmount.subtract(platformFee);

        walletService.creditWallet(
                contract.getFreelancer().getId(),
                freelancerPay,
                "Milestone payment: " + milestone.getTitle(),
                "M-" + milestoneId);

        milestone.setStatus(MilestoneStatus.APPROVED);
        milestone.setApprovedAt(LocalDateTime.now());

        // Update contract: released, platform fee, and reduce escrow
        contract.setReleasedAmount(contract.getReleasedAmount().add(freelancerPay));
        contract.setPlatformFeeCollected(contract.getPlatformFeeCollected().add(platformFee));
        contract.setEscrowAmount(contract.getEscrowAmount().subtract(milestoneAmount));
        contractRepository.save(contract);

        // If no escrow left, treat contract as completed (all milestones paid)
        if (contract.getEscrowAmount().compareTo(BigDecimal.ZERO) <= 0) {
            contract.setStatus(ContractStatus.COMPLETED);
            contract.setCompletedAt(LocalDateTime.now());
            contractRepository.save(contract);
            Job job = contract.getJob();
            job.setStatus(JobStatus.COMPLETED);
            jobRepository.save(job);
            notificationService.notifyPaymentReleased(
                    contract.getFreelancer(),
                    freelancerPay.toPlainString(),
                    contract.getId());
        }

        return toMilestoneResponse(milestoneRepository.save(milestone));
    }

    /**
     * CLIENT adds a milestone to an active contract. Sum of milestone amounts must not exceed contract totalAmount.
     */
    @Transactional
    public MilestoneResponse addMilestone(Long contractId, User client, MilestoneRequest request) {
        Contract contract = getContractOrThrow(contractId);
        if (!contract.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("Only the client can add milestones.");
        }
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException("Can only add milestones to an active contract.");
        }
        BigDecimal existingTotal = contract.getMilestones().stream()
                .map(Milestone::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newTotal = existingTotal.add(request.amount());
        if (newTotal.compareTo(contract.getTotalAmount()) > 0) {
            throw new BusinessException("Sum of milestone amounts cannot exceed contract total (TZS " + contract.getTotalAmount() + ").");
        }
        LocalDate dueDate = null;
        if (request.dueDate() != null && !request.dueDate().isBlank()) {
            try {
                dueDate = LocalDate.parse(request.dueDate());
            } catch (Exception ignored) {}
        }
        Milestone milestone = Milestone.builder()
                .contract(contract)
                .title(request.title())
                .description(request.description())
                .amount(request.amount())
                .dueDate(dueDate)
                .orderIndex(request.orderIndex() != null ? request.orderIndex() : contract.getMilestones().size())
                .status(MilestoneStatus.PENDING)
                .build();
        milestone = milestoneRepository.save(milestone);
        log.info("Milestone added: contract={} title={} amount={}", contractId, request.title(), request.amount());
        return toMilestoneResponse(milestone);
    }

    /**
     * CLIENT requests changes (instead of approve). Counts against revision limit.
     * If limit reached: throw with message to add new milestone for more revisions.
     */
    @Transactional
    public ContractResponse requestChanges(Long contractId, User client, String note) {
        Contract contract = getContractOrThrow(contractId);
        if (!contract.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("Only the client can request changes.");
        }
        int limit = contract.getRevisionLimit() != null ? contract.getRevisionLimit() : 3;
        int used = contract.getRevisionsUsed() != null ? contract.getRevisionsUsed() : 0;
        if (used >= limit) {
            throw new BusinessException(
                    "Umefikisha kikomo cha marekebisho ya bure (" + limit + "/" + limit + "). " +
                            "Tafadhali ongeza Milestone mpya (malipo ya ziada) ili uendelee kufanya mabadiliko.");
        }
        contract.setRevisionsUsed(used + 1);
        if (contract.getStatus() == ContractStatus.REVIEW_PENDING) {
            contract.setStatus(ContractStatus.ACTIVE);
            contractRepository.save(contract);
            notificationService.notify(contract.getFreelancer(),
                    com.winga.domain.enums.NotificationType.WORK_SUBMITTED,
                    "Marekebisho yanahitajika",
                    "Mteja ameomba mabadiliko. Tazama maelezo kwenye contract.",
                    String.valueOf(contractId), "CONTRACT");
            log.info("Request changes (contract): contract={} revisionsUsed={}/{}", contractId, used + 1, limit);
            return toContractResponse(contract);
        }
        Milestone inReview = contract.getMilestones().stream()
                .filter(m -> m.getStatus() == MilestoneStatus.IN_REVIEW)
                .findFirst()
                .orElse(null);
        if (inReview != null) {
            inReview.setStatus(MilestoneStatus.PENDING);
            inReview.setSubmissionNote(null);
            inReview.setSubmittedAt(null);
            milestoneRepository.save(inReview);
            contractRepository.save(contract);
            notificationService.notify(contract.getFreelancer(),
                    com.winga.domain.enums.NotificationType.WORK_SUBMITTED,
                    "Marekebisho ya Milestone yanahitajika",
                    "Mteja ameomba mabadiliko kwa milestone: " + inReview.getTitle(),
                    String.valueOf(contractId), "CONTRACT");
            log.info("Request changes (milestone): contract={} milestone={} revisionsUsed={}/{}",
                    contractId, inReview.getId(), used + 1, limit);
            return toContractResponse(contract);
        }
        throw new BusinessException("Hakuna kazi iliyotumwa kwa sasa ya kuomba mabadiliko.");
    }

    /**
     * FREELANCER submits work for a milestone; milestone goes to IN_REVIEW for client approval.
     */
    @Transactional
    public MilestoneResponse submitMilestone(Long contractId, Long milestoneId, User freelancer, String note) {
        Contract contract = getContractOrThrow(contractId);
        if (!contract.getFreelancer().getId().equals(freelancer.getId())) {
            throw new UnauthorizedAccessException("Only the freelancer on this contract can submit milestone work.");
        }
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", milestoneId));
        if (!milestone.getContract().getId().equals(contractId)) {
            throw new BusinessException("Milestone does not belong to this contract.");
        }
        if (milestone.getStatus() != MilestoneStatus.PENDING) {
            throw new BusinessException("Milestone is not in PENDING state.");
        }
        milestone.setStatus(MilestoneStatus.IN_REVIEW);
        milestone.setSubmissionNote(note);
        milestone.setSubmittedAt(LocalDateTime.now());
        milestone = milestoneRepository.save(milestone);
        notificationService.notifyWorkSubmitted(contract.getClient(), freelancer.getFullName(), contractId);
        log.info("Milestone submitted: contract={} milestone={}", contractId, milestoneId);
        return toMilestoneResponse(milestone);
    }

    /**
     * CLIENT ends the contract: refunds remaining escrow (and add-on escrow) to client.
     * Job goes back to OPEN so client can hire again or close. Use when client no longer wants to continue.
     */
    @Transactional
    public ContractResponse terminateContract(Long contractId, User client) {
        Contract contract = getContractOrThrow(contractId);
        if (!contract.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("Only the client can end this contract.");
        }
        if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.REVIEW_PENDING) {
            throw new BusinessException("Only active or in-review contracts can be ended by the client.");
        }
        BigDecimal escrow = contract.getEscrowAmount() != null ? contract.getEscrowAmount() : BigDecimal.ZERO;
        BigDecimal addonEscrow = contract.getAddonEscrowAmount() != null ? contract.getAddonEscrowAmount() : BigDecimal.ZERO;
        BigDecimal totalRefund = escrow.add(addonEscrow);
        if (totalRefund.compareTo(BigDecimal.ZERO) > 0) {
            walletService.creditWallet(
                    client.getId(),
                    totalRefund,
                    "Contract ended: refund for job " + contract.getJob().getTitle(),
                    "TERM-C-" + contractId);
        }
        contract.setEscrowAmount(BigDecimal.ZERO);
        contract.setAddonEscrowAmount(BigDecimal.ZERO);
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setCompletedAt(LocalDateTime.now());
        contract.setTerminationReason("Ended by client.");
        contractRepository.save(contract);
        Job job = contract.getJob();
        job.setStatus(JobStatus.OPEN);
        jobRepository.save(job);
        notificationService.notify(contract.getFreelancer(),
                com.winga.domain.enums.NotificationType.SYSTEM,
                "Contract imekomeshwa",
                "Mteja amekomesha contract. Pesa zilizobaki zimerudishwa kwa mteja.",
                String.valueOf(contractId), "CONTRACT");
        log.info("Contract {} terminated by client. Refund={}", contractId, totalRefund);
        return toContractResponse(contract);
    }

    /**
     * Admin ends a contract: same refund logic as client terminate. Job reopens.
     */
    @Transactional
    public ContractResponse terminateContractAdmin(Long contractId, User admin) {
        if (admin.getRole() != com.winga.domain.enums.Role.ADMIN && admin.getRole() != com.winga.domain.enums.Role.SUPER_ADMIN) {
            throw new UnauthorizedAccessException("Only admin can end a contract on behalf of the platform.");
        }
        Contract contract = getContractOrThrow(contractId);
        if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.REVIEW_PENDING && contract.getStatus() != ContractStatus.PAUSED) {
            throw new BusinessException("Only active, in-review or paused contracts can be ended by admin.");
        }
        BigDecimal escrow = contract.getEscrowAmount() != null ? contract.getEscrowAmount() : BigDecimal.ZERO;
        BigDecimal addonEscrow = contract.getAddonEscrowAmount() != null ? contract.getAddonEscrowAmount() : BigDecimal.ZERO;
        BigDecimal totalRefund = escrow.add(addonEscrow);
        if (totalRefund.compareTo(BigDecimal.ZERO) > 0) {
            walletService.creditWallet(
                    contract.getClient().getId(),
                    totalRefund,
                    "Contract ended by admin: refund for job " + contract.getJob().getTitle(),
                    "TERM-C-" + contractId);
        }
        contract.setEscrowAmount(BigDecimal.ZERO);
        contract.setAddonEscrowAmount(BigDecimal.ZERO);
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setCompletedAt(LocalDateTime.now());
        contract.setTerminationReason("Ended by admin.");
        contractRepository.save(contract);
        Job job = contract.getJob();
        job.setStatus(JobStatus.OPEN);
        jobRepository.save(job);
        log.info("Contract {} terminated by admin {}. Refund={}", contractId, admin.getId(), totalRefund);
        return toContractResponse(contract);
    }

    // ─── Dispute ─────────────────────────────────────────────────────────────────

    @Transactional
    public ContractResponse raiseDispute(Long contractId, User requestingParty, String reason) {
        Contract contract = getContractOrThrow(contractId);

        boolean isParty = contract.getClient().getId().equals(requestingParty.getId())
                || contract.getFreelancer().getId().equals(requestingParty.getId());

        if (!isParty) {
            throw new UnauthorizedAccessException();
        }
        if (contract.getStatus() == ContractStatus.COMPLETED ||
                contract.getStatus() == ContractStatus.TERMINATED) {
            throw new BusinessException("Cannot dispute a closed contract.");
        }

        contract.setStatus(ContractStatus.DISPUTED);
        contract.setTerminationReason(reason);
        contractRepository.save(contract);

        log.warn("Dispute raised on contract {} by user {}: {}", contractId, requestingParty.getId(), reason);
        return toContractResponse(contract);
    }

    /**
     * Admin resolves a disputed contract: release escrow to CLIENT (refund) or FREELANCER (payout).
     */
    @Transactional
    public ContractResponse resolveDispute(Long contractId, User admin, String releaseTo) {
        if (admin.getRole() != com.winga.domain.enums.Role.ADMIN && admin.getRole() != com.winga.domain.enums.Role.SUPER_ADMIN) {
            throw new UnauthorizedAccessException("Only admin can resolve disputes.");
        }
        Contract contract = getContractOrThrow(contractId);
        if (contract.getStatus() != ContractStatus.DISPUTED) {
            throw new BusinessException("Contract is not in disputed status.");
        }
        BigDecimal escrowAmount = contract.getEscrowAmount();
        if (escrowAmount == null || escrowAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("No funds in escrow to release.");
        }

        if ("FREELANCER".equals(releaseTo)) {
            BigDecimal platformFee = escrowAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal freelancerPay = escrowAmount.subtract(platformFee);
            walletService.creditWallet(
                    contract.getFreelancer().getId(),
                    freelancerPay,
                    "Dispute resolution: payment released for job " + contract.getJob().getTitle(),
                    "C-" + contractId);
            contract.setReleasedAmount(contract.getReleasedAmount().add(freelancerPay));
            contract.setPlatformFeeCollected(contract.getPlatformFeeCollected().add(platformFee));
            contract.setStatus(ContractStatus.COMPLETED);
            contract.setCompletedAt(LocalDateTime.now());
            contract.setEscrowAmount(BigDecimal.ZERO);
            contractRepository.save(contract);
            notificationService.notifyPaymentReleased(
                    contract.getFreelancer(),
                    freelancerPay.toPlainString(),
                    contractId);
            log.info("Dispute {} resolved: released to freelancer. Amount={}", contractId, freelancerPay);
        } else {
            // CLIENT refund
            walletService.creditWallet(
                    contract.getClient().getId(),
                    escrowAmount,
                    "Dispute resolution: refund for job " + contract.getJob().getTitle(),
                    "REFUND-C-" + contractId);
            contract.setEscrowAmount(BigDecimal.ZERO);
            contract.setStatus(ContractStatus.TERMINATED);
            contract.setCompletedAt(LocalDateTime.now());
            contractRepository.save(contract);
            log.info("Dispute {} resolved: refund to client. Amount={}", contractId, escrowAmount);
        }
        return toContractResponse(contract);
    }

    // ─── Query ───────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ContractResponse getContract(Long contractId, User requestingUser) {
        Contract contract = getContractOrThrow(contractId);
        boolean isParty = contract.getClient().getId().equals(requestingUser.getId())
                || contract.getFreelancer().getId().equals(requestingUser.getId());
        boolean isAdmin = requestingUser.getRole() == com.winga.domain.enums.Role.ADMIN || requestingUser.getRole() == com.winga.domain.enums.Role.SUPER_ADMIN;
        if (!isParty && !isAdmin) {
            throw new UnauthorizedAccessException();
        }
        return toContractResponse(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getFreelancerContracts(Long freelancerId, Pageable pageable) {
        return contractRepository.findByFreelancerId(freelancerId, pageable)
                .map(this::toContractResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getClientContracts(Long clientId, Pageable pageable) {
        return contractRepository.findByClientId(clientId, pageable)
                .map(this::toContractResponse);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    public Contract getContractOrThrow(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────────

    public ContractResponse toContractResponse(Contract c) {
        List<MilestoneResponse> milestones = c.getMilestones().stream()
                .map(this::toMilestoneResponse)
                .toList();

        return new ContractResponse(
                c.getId(),
                c.getJob().getId(),
                c.getJob().getTitle(),
                userService.toUserResponse(c.getClient()),
                userService.toUserResponse(c.getFreelancer()),
                c.getTotalAmount(),
                c.getEscrowAmount(),
                c.getAddonEscrowAmount() != null ? c.getAddonEscrowAmount() : BigDecimal.ZERO,
                c.getReleasedAmount(),
                c.getPlatformFeeCollected(),
                c.getStatus(),
                c.getRevisionLimit(),
                c.getRevisionsUsed(),
                c.getTerminationReason(),
                milestones,
                c.getCreatedAt(),
                c.getCompletedAt());
    }

    public MilestoneResponse toMilestoneResponse(Milestone m) {
        return new MilestoneResponse(
                m.getId(), m.getTitle(), m.getDescription(),
                m.getAmount(), m.getDueDate(), m.getOrderIndex(),
                m.getStatus(), m.getFundedAt(), m.getSubmittedAt(), m.getApprovedAt());
    }
}
