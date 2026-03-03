package com.winga.service;

import com.winga.domain.enums.ContractStatus;
import com.winga.domain.enums.Currency;
import com.winga.domain.enums.FilterOptionType;
import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.ModerationStatus;
import com.winga.domain.enums.ProposalStatus;
import com.winga.domain.enums.Role;
import com.winga.domain.enums.VerificationStatus;
import com.winga.dto.request.AdminBulkProposalStatusRequest;
import com.winga.dto.request.AdminCreateJobRequest;
import com.winga.dto.request.AdminCreateUserRequest;
import com.winga.dto.request.AdminUpdateJobRequest;
import com.winga.dto.request.AdminUpdateUserRequest;
import com.winga.dto.request.AdminLanguageRequest;
import com.winga.dto.request.FilterOptionRequest;
import com.winga.dto.request.JobCategoryRequest;
import com.winga.dto.request.JobRequest;
import com.winga.dto.request.ModerateJobRequest;
import com.winga.dto.request.PaymentOptionRequest;
import com.winga.dto.request.PaymentGatewayConfigRequest;
import com.winga.dto.response.*;
import com.winga.entity.Contract;
import com.winga.entity.Job;
import com.winga.entity.FilterOption;
import com.winga.entity.JobCategory;
import com.winga.entity.PaymentGatewayConfig;
import com.winga.entity.PaymentOption;
import com.winga.entity.Proposal;
import com.winga.entity.User;
import com.winga.entity.Wallet;
import com.winga.exception.BusinessException;
import com.winga.exception.ResourceNotFoundException;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.ContractRepository;
import com.winga.repository.FilterOptionRepository;
import com.winga.repository.JobCategoryRepository;
import com.winga.repository.JobRepository;
import com.winga.repository.PaymentGatewayConfigRepository;
import com.winga.repository.PaymentOptionRepository;
import com.winga.repository.ProposalRepository;
import com.winga.repository.UserRepository;
import com.winga.repository.WalletRepository;
import com.winga.service.CertificationService;
import com.winga.service.NotificationService;
import com.winga.service.PortfolioItemService;
import com.winga.service.WorkExperienceService;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JobRepository jobRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final FilterOptionRepository filterOptionRepository;
    private final PaymentOptionRepository paymentOptionRepository;
    private final PaymentGatewayConfigRepository paymentGatewayConfigRepository;
    private final ObjectMapper objectMapper;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;
    private final JobService jobService;
    private final ProposalService proposalService;
    private final ChatService chatService;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;
    private final PortfolioItemService portfolioItemService;
    private final CertificationService certificationService;
    private final NotificationService notificationService;
    private final WorkExperienceService workExperienceService;

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userService::toUserResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id, User admin) {
        ensureAdmin(admin);
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userService.toUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request, User admin) {
        ensureAdmin(admin);
        if (request.role() == Role.SUPER_ADMIN && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Only Super Admin can create another Super Admin.");
        }
        if (userRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new BusinessException("Email already registered: " + request.email());
        }
        User user = User.builder()
                .email(request.email().trim().toLowerCase())
                .fullName(request.fullName().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .phoneNumber(request.phoneNumber() != null && !request.phoneNumber().isBlank() ? request.phoneNumber().trim() : null)
                .isVerified(false)
                .verificationStatus(VerificationStatus.UNVERIFIED)
                .isActive(true)
                .build();
        user = userRepository.save(user);
        Wallet wallet = Wallet.builder().user(user).currency(Currency.TZS).build();
        walletRepository.save(wallet);
        log.info("Admin {} created user {} ({})", admin.getId(), user.getEmail(), user.getRole());
        return userService.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, AdminUpdateUserRequest request, User admin) {
        ensureAdmin(admin);
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        if ((user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN) && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Only Super Admin can edit an admin user.");
        }
        if (request.fullName() != null && !request.fullName().isBlank()) user.setFullName(request.fullName().trim());
        if (request.email() != null && !request.email().isBlank()) {
            String email = request.email().trim().toLowerCase();
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new BusinessException("Email already in use: " + email);
            }
            user.setEmail(email);
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null) {
            if (request.role() == Role.SUPER_ADMIN && admin.getRole() != Role.SUPER_ADMIN) {
                throw new BusinessException("Only Super Admin can assign Super Admin role.");
            }
            user.setRole(request.role());
        }
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber().isBlank() ? null : request.phoneNumber().trim());
        if (request.isVerified() != null) {
            user.setIsVerified(request.isVerified());
            user.setVerificationStatus(request.isVerified() ? VerificationStatus.VERIFIED : VerificationStatus.UNVERIFIED);
        }
        if (request.verificationStatus() != null) {
            user.setVerificationStatus(request.verificationStatus());
            user.setIsVerified(VerificationStatus.VERIFIED == request.verificationStatus());
        }
        if (request.isActive() != null) user.setIsActive(request.isActive());
        userRepository.save(user);
        log.info("Admin {} updated user {}", admin.getId(), id);
        return userService.toUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long id, User admin) {
        ensureAdmin(admin);
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN) {
            if (admin.getRole() != Role.SUPER_ADMIN) throw new BusinessException("Only Super Admin can delete an admin.");
            if (user.getId().equals(admin.getId())) throw new BusinessException("You cannot delete your own account.");
        }
        user.setIsActive(false);
        userRepository.save(user);
        log.info("Admin {} deactivated user {}", admin.getId(), id);
    }

    @Transactional
    public UserResponse verifyUser(Long userId, boolean verify, User admin) {
        ensureAdmin(admin);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if ((user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN) && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Only Super Admin can change verification of an admin.");
        }
        user.setIsVerified(verify);
        user.setVerificationStatus(verify ? VerificationStatus.VERIFIED : VerificationStatus.UNVERIFIED);
        userRepository.save(user);
        log.info("Admin {} set user {} verification to {}", admin.getId(), userId, verify);
        return userService.toUserResponse(user);
    }

    /** Set worker profile as verified (badge). Only for FREELANCER. */
    @Transactional
    public UserResponse verifyProfile(Long userId, boolean verified, User admin) {
        ensureAdmin(admin);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() != Role.FREELANCER) {
            throw new BusinessException("Profile verification applies only to workers (FREELANCER).");
        }
        user.setProfileVerified(verified);
        user.setProfileVerifiedAt(verified ? LocalDateTime.now() : null);
        userRepository.save(user);
        log.info("Admin {} set user {} profile verified to {}", admin.getId(), userId, verified);
        return userService.toUserResponse(user);
    }

    /** Bulk verify or unverify worker profiles. Skips non-FREELANCER IDs. */
    @Transactional
    public int bulkVerifyProfile(com.winga.dto.request.AdminBulkVerifyProfileRequest request, User admin) {
        ensureAdmin(admin);
        if (request.userIds() == null || request.userIds().isEmpty()) {
            return 0;
        }
        int updated = 0;
        for (Long userId : request.userIds()) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getRole() == Role.FREELANCER) {
                user.setProfileVerified(request.verified());
                user.setProfileVerifiedAt(request.verified() ? LocalDateTime.now() : null);
                userRepository.save(user);
                updated++;
            }
        }
        log.info("Admin {} bulk set profile verified to {} for {} users", admin.getId(), request.verified(), updated);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<com.winga.dto.response.WorkExperienceResponse> getUserExperiences(Long userId, User admin) {
        ensureAdmin(admin);
        userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return workExperienceService.getMyExperiences(userId);
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats(User admin) {
        ensureAdmin(admin);
        long totalUsers = userRepository.count();
        long totalClients = userRepository.countByRole(Role.CLIENT);
        long totalFreelancers = userRepository.countByRole(Role.FREELANCER);
        long openJobs = jobRepository.countByStatus(com.winga.domain.enums.JobStatus.OPEN);
        long totalJobs = jobRepository.count();
        long totalProposals = proposalRepository.count();
        long activeContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);
        long completedContracts = contractRepository.countByStatus(ContractStatus.COMPLETED);
        long disputedContracts = contractRepository.countByStatus(ContractStatus.DISPUTED);
        BigDecimal revenue = contractRepository.totalPlatformRevenue();
        if (revenue == null) revenue = BigDecimal.ZERO;
        return new AdminStatsResponse(
                totalUsers, totalClients, totalFreelancers,
                openJobs, totalJobs, totalProposals,
                activeContracts, completedContracts, disputedContracts,
                revenue);
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsResponse getAnalytics(User admin, LocalDateTime from, LocalDateTime to) {
        ensureAdmin(admin);
        List<Object[]> categoryRows = jobRepository.countByCategory();
        List<Map<String, Object>> jobsPerCategory = categoryRows.stream()
                .map(row -> Map.<String, Object>of("category", row[0] != null ? row[0] : "", "count", ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
        List<Object[]> proposalRows = proposalRepository.countProposalsByJobId(
                org.springframework.data.domain.PageRequest.of(0, 50));
        List<Map<String, Object>> proposalsPerJob = proposalRows.stream()
                .map(row -> Map.<String, Object>of("jobId", ((Number) row[0]).longValue(), "proposalCount", ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
        LocalDateTime fromDate = from != null ? from : LocalDateTime.now().minusDays(30);
        LocalDateTime toDate = to != null ? to : LocalDateTime.now();
        BigDecimal revenue = contractRepository.revenueBetween(fromDate, toDate);
        if (revenue == null) revenue = BigDecimal.ZERO;
        return new AdminAnalyticsResponse(
                jobsPerCategory,
                proposalsPerJob,
                revenue,
                fromDate.toString(),
                toDate.toString());
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> listDisputes(Pageable pageable, User admin) {
        ensureAdmin(admin);
        return contractRepository.findByStatus(ContractStatus.DISPUTED, pageable)
                .map(contractService::toContractResponse);
    }

    @Transactional
    public ContractResponse resolveDispute(Long contractId, String releaseTo, User admin) {
        ensureAdmin(admin);
        return contractService.resolveDispute(contractId, admin, releaseTo);
    }

    /** Admin: full dispute detail — original scope (job + milestones) + chat logs. */
    @Transactional(readOnly = true)
    public DisputeDetailResponse getDisputeDetail(Long contractId, User admin) {
        ensureAdmin(admin);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));
        if (contract.getStatus() != ContractStatus.DISPUTED) {
            throw new BusinessException("Contract is not in disputed status.");
        }
        String jobDescription = contract.getJob() != null ? contract.getJob().getDescription() : null;
        var messages = chatService.getContractMessagesForAdmin(contractId);
        return new DisputeDetailResponse(
                contractService.toContractResponse(contract),
                jobDescription,
                messages);
    }

    private void ensureAdmin(User user) {
        if (user == null || (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN)) {
            throw new UnauthorizedAccessException("Admin or Super Admin only.");
        }
    }

    // ─── Dashboard overview (Super Admin) ───────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminDashboardOverviewResponse getDashboardOverview(User admin) {
        ensureAdmin(admin);
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long activeJobs = jobRepository.countByStatus(com.winga.domain.enums.JobStatus.OPEN);
        long applicationsToday = proposalRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long applicationsThisMonth = proposalRepository.countByCreatedAtBetween(startOfMonth, LocalDateTime.now());
        long hiresMade = contractRepository.countByStatus(ContractStatus.COMPLETED) + contractRepository.countByStatus(ContractStatus.ACTIVE);
        long totalProposals = proposalRepository.count();
        long respondedProposals = totalProposals == 0 ? 0 : totalProposals - proposalRepository.countByStatus(ProposalStatus.PENDING);
        double responseRatePercent = totalProposals == 0 ? 0.0 : (100.0 * respondedProposals / totalProposals);
        BigDecimal revenue = contractRepository.totalPlatformRevenue();
        if (revenue == null) revenue = BigDecimal.ZERO;
        long pendingModerationCount = jobRepository.countByModerationStatus(ModerationStatus.PENDING_APPROVAL);

        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> dateRows = proposalRepository.countGroupByDateSince(since);
        List<AdminDashboardOverviewResponse.ChartPoint> applicationsOverTime = dateRows.stream()
                .map(row -> new AdminDashboardOverviewResponse.ChartPoint(
                        row[0] != null ? row[0].toString() : "",
                        row[1] != null ? ((Number) row[1]).longValue() : 0L))
                .collect(Collectors.toList());

        List<Object[]> categoryRows = proposalRepository.countByCategorySince(since);
        List<AdminDashboardOverviewResponse.TopCategoryDto> topCategories = categoryRows.stream()
                .map(row -> new AdminDashboardOverviewResponse.TopCategoryDto(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).longValue() : 0L))
                .collect(Collectors.toList());

        return new AdminDashboardOverviewResponse(
                activeJobs,
                applicationsToday,
                applicationsThisMonth,
                hiresMade,
                responseRatePercent,
                revenue,
                pendingModerationCount,
                applicationsOverTime,
                topCategories);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> listJobsForModeration(Pageable pageable, User admin) {
        ensureAdmin(admin);
        return jobRepository.findByModerationStatus(ModerationStatus.PENDING_APPROVAL, pageable)
                .map(jobService::toJobResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> listAllJobs(JobStatus status, Pageable pageable, User admin) {
        ensureAdmin(admin);
        if (status != null) {
            return jobRepository.findByStatus(status, pageable).map(jobService::toJobResponse);
        }
        return jobRepository.findAll(pageable).map(jobService::toJobResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobByIdForAdmin(Long jobId, User admin) {
        ensureAdmin(admin);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        return jobService.toJobResponse(job);
    }

    @Transactional
    public JobResponse updateJobAdmin(Long jobId, AdminUpdateJobRequest request, User admin) {
        ensureAdmin(admin);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (request.title() != null) job.setTitle(request.title());
        if (request.description() != null) job.setDescription(request.description());
        if (request.budget() != null) job.setBudget(request.budget());
        if (request.deadline() != null) job.setDeadline(request.deadline());
        if (request.tags() != null) job.setTags(request.tags().isEmpty() ? null : String.join(",", request.tags()));
        if (request.category() != null) job.setCategory(request.category());
        if (request.experienceLevel() != null) job.setExperienceLevel(request.experienceLevel());
        if (request.employmentType() != null) job.setEmploymentType(request.employmentType());
        if (request.socialMedia() != null) job.setSocialMedia(request.socialMedia());
        if (request.software() != null) job.setSoftware(request.software());
        if (request.language() != null) job.setLanguage(request.language());
        if (request.status() != null) job.setStatus(request.status());
        if (request.moderationStatus() != null) job.setModerationStatus(request.moderationStatus());
        if (request.isFeatured() != null) job.setIsFeatured(request.isFeatured());
        if (request.isBoostedTelegram() != null) job.setIsBoostedTelegram(request.isBoostedTelegram());
        jobRepository.save(job);
        log.info("Admin {} updated job {}", admin.getId(), jobId);
        return jobService.toJobResponse(job);
    }

    @Transactional
    public void deleteJobAdmin(Long jobId, User admin) {
        ensureAdmin(admin);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        long proposals = proposalRepository.countByJobId(job.getId());
        if (proposals > 0) {
            job.setStatus(JobStatus.CANCELLED);
            jobRepository.save(job);
            log.info("Admin {} cancelled job {} (had {} proposals)", admin.getId(), jobId, proposals);
        } else {
            jobRepository.delete(job);
            log.info("Admin {} deleted job {}", admin.getId(), jobId);
        }
    }

    @Transactional
    public JobResponse moderateJob(Long jobId, ModerateJobRequest request, User admin) {
        ensureAdmin(admin);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        job.setModerationStatus(request.status());
        jobRepository.save(job);
        log.info("Admin {} moderated job {} to {}", admin.getId(), jobId, request.status());
        User client = job.getClient();
        if (client != null) {
            if (request.status() == ModerationStatus.APPROVED) {
                notificationService.notifyJobApproved(client, job.getTitle(), jobId);
            } else if (request.status() == ModerationStatus.REJECTED) {
                notificationService.notifyJobRejected(client, job.getTitle(), jobId);
            }
        }
        return jobService.toJobResponse(job);
    }

    @Transactional
    public JobResponse createJobAdmin(AdminCreateJobRequest request, User admin) {
        ensureAdmin(admin);
        User client = userRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.clientId()));
        if (client.getRole() != Role.CLIENT && client.getRole() != Role.EMPLOYER_ADMIN && client.getRole() != Role.ADMIN && client.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("Selected user must be a client/employer role to post jobs.");
        }
        JobRequest jr = new JobRequest(
                request.title(),
                request.description(),
                request.budget(),
                request.deadline(),
                request.tags(),
                request.category(),
                request.experienceLevel(),
                request.employmentType(),
                request.socialMedia(),
                request.software(),
                request.language(),
                null, null, null, null, null);
        return jobService.createJob(client, jr);
    }

    // ─── Applications / Proposals ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProposalResponse> listProposals(Long jobId, ProposalStatus status, ModerationStatus moderationStatus, Pageable pageable, User admin) {
        ensureAdmin(admin);
        if (moderationStatus != null) {
            return proposalRepository.findByModerationStatus(moderationStatus, pageable).map(proposalService::toProposalResponse);
        }
        if (jobId != null && status != null) {
            return proposalRepository.findByJobIdAndStatus(jobId, status, pageable).map(proposalService::toProposalResponse);
        }
        if (jobId != null) {
            return proposalRepository.findByJobId(jobId, pageable).map(proposalService::toProposalResponse);
        }
        if (status != null) {
            return proposalRepository.findByStatus(status, pageable).map(proposalService::toProposalResponse);
        }
        return proposalRepository.findAll(pageable).map(proposalService::toProposalResponse);
    }

    @Transactional(readOnly = true)
    public ProposalResponse getProposalById(Long proposalId, User admin) {
        ensureAdmin(admin);
        Proposal p = proposalRepository.findById(proposalId).orElseThrow(() -> new ResourceNotFoundException("Proposal", proposalId));
        return proposalService.toProposalResponse(p);
    }

    @Transactional
    public ProposalResponse updateProposalStatusAdmin(Long proposalId, ProposalStatus status, User admin) {
        ensureAdmin(admin);
        return proposalService.updateProposalStatusAdmin(proposalId, status, admin);
    }

    @Transactional
    public List<ProposalResponse> bulkUpdateProposalStatus(AdminBulkProposalStatusRequest request, User admin) {
        ensureAdmin(admin);
        return request.proposalIds().stream()
                .map(id -> proposalService.updateProposalStatusAdmin(id, request.status(), admin))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProposalResponse moderateProposal(Long proposalId, ModerationStatus moderationStatus, User admin) {
        ensureAdmin(admin);
        return proposalService.moderateProposal(proposalId, moderationStatus, admin);
    }

    // ─── Contracts / Hires ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ContractResponse> listContracts(ContractStatus status, Pageable pageable, User admin) {
        ensureAdmin(admin);
        if (status != null) {
            return contractRepository.findByStatus(status, pageable).map(contractService::toContractResponse);
        }
        return contractRepository.findAll(pageable).map(contractService::toContractResponse);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractById(Long contractId, User admin) {
        ensureAdmin(admin);
        return contractService.getContract(contractId, admin);
    }

    @Transactional
    public ContractResponse terminateContractAdmin(Long contractId, User admin) {
        ensureAdmin(admin);
        return contractService.terminateContractAdmin(contractId, admin);
    }

    // ─── Job categories (Super Admin) ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<JobCategoryResponse> listCategories(User admin) {
        ensureAdmin(admin);
        return jobCategoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobCategoryResponse createCategory(JobCategoryRequest request, User admin) {
        ensureAdmin(admin);
        if (jobCategoryRepository.existsBySlug(request.slug())) {
            throw new BusinessException("Category slug already exists: " + request.slug());
        }
        JobCategory cat = JobCategory.builder()
                .name(request.name())
                .slug(request.slug())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .build();
        cat = jobCategoryRepository.save(cat);
        return toCategoryResponse(cat);
    }

    @Transactional
    public JobCategoryResponse updateCategory(Long id, JobCategoryRequest request, User admin) {
        ensureAdmin(admin);
        JobCategory cat = jobCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("JobCategory", id));
        cat.setName(request.name());
        cat.setSlug(request.slug());
        if (request.sortOrder() != null) cat.setSortOrder(request.sortOrder());
        jobCategoryRepository.save(cat);
        return toCategoryResponse(cat);
    }

    @Transactional
    public void deleteCategory(Long id, User admin) {
        ensureAdmin(admin);
        if (!jobCategoryRepository.existsById(id)) throw new ResourceNotFoundException("JobCategory", id);
        jobCategoryRepository.deleteById(id);
    }

    private JobCategoryResponse toCategoryResponse(JobCategory c) {
        return new JobCategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getSortOrder(), c.getCreatedAt());
    }

    // ─── Filter options (Employment Type, Social Media, Software, Languages) ───────

    @Transactional(readOnly = true)
    public List<FilterOptionResponse> listFilterOptions(FilterOptionType type, User admin) {
        ensureAdmin(admin);
        return filterOptionRepository.findByTypeOrderBySortOrderAsc(type).stream()
                .map(this::toFilterOptionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FilterOptionResponse createLanguage(AdminLanguageRequest request, User admin) {
        return createFilterOption(new FilterOptionRequest(
                FilterOptionType.LANGUAGE, request.name(), request.slug(), request.sortOrder()), admin);
    }

    @Transactional
    public FilterOptionResponse updateLanguage(Long id, AdminLanguageRequest request, User admin) {
        return updateFilterOption(id, new FilterOptionRequest(
                FilterOptionType.LANGUAGE, request.name(), request.slug(), request.sortOrder()), admin);
    }

    @Transactional
    public FilterOptionResponse createFilterOption(FilterOptionRequest request, User admin) {
        ensureAdmin(admin);
        if (filterOptionRepository.existsByTypeAndSlug(request.type(), request.slug())) {
            throw new BusinessException("Filter option slug already exists for this type: " + request.slug());
        }
        FilterOption opt = FilterOption.builder()
                .type(request.type())
                .name(request.name())
                .slug(request.slug())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .build();
        opt = filterOptionRepository.save(opt);
        return toFilterOptionResponse(opt);
    }

    @Transactional
    public FilterOptionResponse updateFilterOption(Long id, FilterOptionRequest request, User admin) {
        ensureAdmin(admin);
        FilterOption opt = filterOptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FilterOption", id));
        opt.setType(request.type());
        opt.setName(request.name());
        opt.setSlug(request.slug());
        if (request.sortOrder() != null) opt.setSortOrder(request.sortOrder());
        filterOptionRepository.save(opt);
        return toFilterOptionResponse(opt);
    }

    @Transactional
    public void deleteFilterOption(Long id, User admin) {
        ensureAdmin(admin);
        if (!filterOptionRepository.existsById(id)) throw new ResourceNotFoundException("FilterOption", id);
        filterOptionRepository.deleteById(id);
    }

    private FilterOptionResponse toFilterOptionResponse(FilterOption o) {
        return new FilterOptionResponse(o.getId(), o.getType(), o.getName(), o.getSlug(), o.getSortOrder(), o.getCreatedAt());
    }

    // ─── Payment options ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PaymentOptionResponse> listPaymentOptions(User admin) {
        ensureAdmin(admin);
        return paymentOptionRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toPaymentOptionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentOptionResponse createPaymentOption(PaymentOptionRequest request, User admin) {
        ensureAdmin(admin);
        if (paymentOptionRepository.existsBySlug(request.slug())) {
            throw new BusinessException("Payment option slug already exists: " + request.slug());
        }
        PaymentOption opt = PaymentOption.builder()
                .name(request.name().trim())
                .slug(request.slug().trim().toLowerCase().replaceAll("\\s+", "-"))
                .description(request.description() != null ? request.description().trim() : null)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .build();
        opt = paymentOptionRepository.save(opt);
        return toPaymentOptionResponse(opt);
    }

    @Transactional
    public PaymentOptionResponse updatePaymentOption(Long id, PaymentOptionRequest request, User admin) {
        ensureAdmin(admin);
        PaymentOption opt = paymentOptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PaymentOption", id));
        if (request.name() != null) opt.setName(request.name().trim());
        if (request.slug() != null) {
            String slug = request.slug().trim().toLowerCase().replaceAll("\\s+", "-");
            if (!slug.equals(opt.getSlug()) && paymentOptionRepository.existsBySlug(slug)) {
                throw new BusinessException("Payment option slug already exists: " + slug);
            }
            opt.setSlug(slug);
        }
        if (request.description() != null) opt.setDescription(request.description().trim());
        if (request.isActive() != null) opt.setIsActive(request.isActive());
        if (request.sortOrder() != null) opt.setSortOrder(request.sortOrder());
        paymentOptionRepository.save(opt);
        return toPaymentOptionResponse(opt);
    }

    @Transactional
    public void deletePaymentOption(Long id, User admin) {
        ensureAdmin(admin);
        if (!paymentOptionRepository.existsById(id)) throw new ResourceNotFoundException("PaymentOption", id);
        paymentOptionRepository.deleteById(id);
    }

    private PaymentOptionResponse toPaymentOptionResponse(PaymentOption o) {
        return new PaymentOptionResponse(o.getId(), o.getName(), o.getSlug(), o.getDescription(), o.getIsActive(), o.getSortOrder(), o.getCreatedAt());
    }

    // ─── Payment gateway config (API keys, settings) ─────────────────────────────

    @Transactional(readOnly = true)
    public List<PaymentGatewayConfigResponse> listPaymentGatewayConfigs(User admin) {
        ensureAdmin(admin);
        return paymentGatewayConfigRepository.findAllByOrderByDisplayNameAsc().stream()
                .map(this::toPaymentGatewayConfigResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentGatewayConfigResponse getPaymentGatewayConfigBySlug(String slug, User admin) {
        ensureAdmin(admin);
        PaymentGatewayConfig cfg = paymentGatewayConfigRepository.findByGatewaySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Payment gateway not found: " + slug));
        return toPaymentGatewayConfigResponse(cfg);
    }

    @Transactional
    public PaymentGatewayConfigResponse createPaymentGatewayConfig(PaymentGatewayConfigRequest request, User admin) {
        ensureAdmin(admin);
        if (paymentGatewayConfigRepository.existsByGatewaySlug(request.gatewaySlug())) {
            throw new BusinessException("Gateway already exists: " + request.gatewaySlug());
        }
        String configJson = configMapToJson(request.config());
        PaymentGatewayConfig cfg = PaymentGatewayConfig.builder()
                .gatewaySlug(request.gatewaySlug().trim().toLowerCase())
                .displayName(request.displayName().trim())
                .configJson(configJson)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();
        cfg = paymentGatewayConfigRepository.save(cfg);
        log.info("Admin {} created payment gateway config: {}", admin.getId(), cfg.getGatewaySlug());
        return toPaymentGatewayConfigResponse(cfg);
    }

    @Transactional
    public PaymentGatewayConfigResponse updatePaymentGatewayConfig(Long id, PaymentGatewayConfigRequest request, User admin) {
        ensureAdmin(admin);
        PaymentGatewayConfig cfg = paymentGatewayConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentGatewayConfig", id));
        cfg.setDisplayName(request.displayName().trim());
        if (request.config() != null && !request.config().isEmpty()) {
            cfg.setConfigJson(configMapToJson(request.config()));
        }
        if (request.isActive() != null) cfg.setIsActive(request.isActive());
        paymentGatewayConfigRepository.save(cfg);
        return toPaymentGatewayConfigResponse(cfg);
    }

    @Transactional
    public void deletePaymentGatewayConfig(Long id, User admin) {
        ensureAdmin(admin);
        if (!paymentGatewayConfigRepository.existsById(id)) throw new ResourceNotFoundException("PaymentGatewayConfig", id);
        paymentGatewayConfigRepository.deleteById(id);
    }

    private PaymentGatewayConfigResponse toPaymentGatewayConfigResponse(PaymentGatewayConfig c) {
        Map<String, String> masked = maskConfigJson(c.getConfigJson());
        return new PaymentGatewayConfigResponse(
                c.getId(), c.getGatewaySlug(), c.getDisplayName(),
                masked, c.getIsActive(), c.getCreatedAt(), c.getUpdatedAt());
    }

    private String configMapToJson(Map<String, String> config) {
        if (config == null || config.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new BusinessException("Invalid config: " + e.getMessage());
        }
    }

    private Map<String, String> maskConfigJson(String configJson) {
        Map<String, String> out = new LinkedHashMap<>();
        if (configJson == null || configJson.isBlank()) return out;
        try {
            Map<String, String> map = objectMapper.readValue(configJson, new TypeReference<Map<String, String>>() {});
            for (Map.Entry<String, String> e : map.entrySet()) {
                String v = e.getValue();
                out.put(e.getKey(), v == null || v.length() <= 3 ? "***" : "***" + v.substring(v.length() - 3));
            }
            return out;
        } catch (Exception e) {
            return out;
        }
    }

    // ─── Portfolio & Certification moderation ───────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PortfolioItemResponse> listPortfolioItems(ModerationStatus moderationStatus, Pageable pageable, User admin) {
        ensureAdmin(admin);
        return portfolioItemService.listByModerationStatus(moderationStatus, pageable);
    }

    @Transactional
    public PortfolioItemResponse moderatePortfolioItem(Long id, ModerationStatus moderationStatus, User admin) {
        ensureAdmin(admin);
        return portfolioItemService.setModerationStatus(id, moderationStatus);
    }

    @Transactional(readOnly = true)
    public Page<CertificationResponse> listCertifications(ModerationStatus moderationStatus, Pageable pageable, User admin) {
        ensureAdmin(admin);
        return certificationService.listByModerationStatus(moderationStatus, pageable);
    }

    @Transactional
    public CertificationResponse moderateCertification(Long id, ModerationStatus moderationStatus, User admin) {
        ensureAdmin(admin);
        return certificationService.setModerationStatus(id, moderationStatus);
    }

    // ─── Export CSV ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] exportJobsCsv(User admin) {
        ensureAdmin(admin);
        List<Job> jobs = jobRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,title,clientEmail,budget,status,category,city,region,createdAt\n");
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (Job j : jobs) {
            sb.append(escapeCsv(j.getId())).append(",");
            sb.append(escapeCsv(j.getTitle())).append(",");
            sb.append(escapeCsv(j.getClient() != null ? j.getClient().getEmail() : "")).append(",");
            sb.append(j.getBudget() != null ? j.getBudget().toPlainString() : "").append(",");
            sb.append(escapeCsv(j.getStatus() != null ? j.getStatus().name() : "")).append(",");
            sb.append(escapeCsv(j.getCategory())).append(",");
            sb.append(escapeCsv(j.getCity())).append(",");
            sb.append(escapeCsv(j.getRegion())).append(",");
            sb.append(j.getCreatedAt() != null ? escapeCsv(j.getCreatedAt().format(dtf)) : "").append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportUsersCsv(User admin) {
        ensureAdmin(admin);
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,email,fullName,role,isVerified,createdAt\n");
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (User u : users) {
            sb.append(escapeCsv(u.getId())).append(",");
            sb.append(escapeCsv(u.getEmail())).append(",");
            sb.append(escapeCsv(u.getFullName())).append(",");
            sb.append(escapeCsv(u.getRole() != null ? u.getRole().name() : "")).append(",");
            sb.append(u.getIsVerified() != null && u.getIsVerified() ? "1" : "0").append(",");
            sb.append(u.getCreatedAt() != null ? escapeCsv(u.getCreatedAt().format(dtf)) : "").append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportWorkersCsv(User admin, boolean incompleteOnly, boolean withCvOnly) {
        return exportWorkersCsv(admin, incompleteOnly, withCvOnly, null);
    }

    /** Export workers CSV. If userIds is non-null and non-empty, only those FREELANCERs are exported. */
    @Transactional(readOnly = true)
    public byte[] exportWorkersCsv(User admin, boolean incompleteOnly, boolean withCvOnly, List<Long> userIds) {
        ensureAdmin(admin);
        List<User> workers = userIds != null && !userIds.isEmpty()
                ? userRepository.findAllById(userIds).stream().filter(u -> u.getRole() == Role.FREELANCER).collect(Collectors.toList())
                : userRepository.findByRole(Role.FREELANCER);
        if (incompleteOnly) {
            workers = workers.stream()
                    .filter(u -> u.getProfileCompleteness() == null || u.getProfileCompleteness() < 100)
                    .collect(Collectors.toList());
        }
        if (withCvOnly) {
            workers = workers.stream()
                    .filter(u -> u.getCvUrl() != null && !u.getCvUrl().isBlank())
                    .collect(Collectors.toList());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("id,email,fullName,headline,country,profileCompleteness,profileVerified,cvUrl,createdAt\n");
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (User u : workers) {
            sb.append(escapeCsv(u.getId())).append(",");
            sb.append(escapeCsv(u.getEmail())).append(",");
            sb.append(escapeCsv(u.getFullName())).append(",");
            sb.append(escapeCsv(u.getHeadline())).append(",");
            sb.append(escapeCsv(u.getCountry())).append(",");
            sb.append(u.getProfileCompleteness() != null ? u.getProfileCompleteness() : "").append(",");
            sb.append(u.getProfileVerified() != null && u.getProfileVerified() ? "1" : "0").append(",");
            sb.append(escapeCsv(u.getCvUrl())).append(",");
            sb.append(u.getCreatedAt() != null ? escapeCsv(u.getCreatedAt().format(dtf)) : "").append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportContractsCsv(User admin) {
        ensureAdmin(admin);
        List<Contract> contracts = contractRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,jobId,jobTitle,clientEmail,freelancerEmail,status,totalAmount,escrowAmount,releasedAmount,platformFee,completedAt,createdAt\n");
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (Contract c : contracts) {
            sb.append(escapeCsv(c.getId())).append(",");
            sb.append(c.getJob() != null ? c.getJob().getId() : "").append(",");
            sb.append(escapeCsv(c.getJob() != null ? c.getJob().getTitle() : "")).append(",");
            sb.append(escapeCsv(c.getClient() != null ? c.getClient().getEmail() : "")).append(",");
            sb.append(escapeCsv(c.getFreelancer() != null ? c.getFreelancer().getEmail() : "")).append(",");
            sb.append(escapeCsv(c.getStatus() != null ? c.getStatus().name() : "")).append(",");
            sb.append(c.getTotalAmount() != null ? c.getTotalAmount().toPlainString() : "").append(",");
            sb.append(c.getEscrowAmount() != null ? c.getEscrowAmount().toPlainString() : "").append(",");
            sb.append(c.getReleasedAmount() != null ? c.getReleasedAmount().toPlainString() : "").append(",");
            sb.append(c.getPlatformFeeCollected() != null ? c.getPlatformFeeCollected().toPlainString() : "").append(",");
            sb.append(c.getCompletedAt() != null ? escapeCsv(c.getCompletedAt().format(dtf)) : "").append(",");
            sb.append(c.getCreatedAt() != null ? escapeCsv(c.getCreatedAt().format(dtf)) : "").append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeCsv(Object o) {
        if (o == null) return "";
        String s = o.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
