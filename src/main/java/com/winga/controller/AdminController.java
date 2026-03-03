package com.winga.controller;

import com.winga.domain.enums.ContractStatus;
import com.winga.domain.enums.FilterOptionType;
import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.ProposalStatus;
import com.winga.dto.request.AdminBulkProposalStatusRequest;
import com.winga.dto.request.AdminCreateJobRequest;
import com.winga.dto.request.AdminCreateUserRequest;
import com.winga.dto.request.AdminUpdateJobRequest;
import com.winga.dto.request.AdminUpdateUserRequest;
import com.winga.dto.request.AdminLanguageRequest;
import com.winga.dto.request.FilterOptionRequest;
import com.winga.dto.request.JobCategoryRequest;
import com.winga.dto.request.ModerateJobRequest;
import com.winga.dto.request.PaymentOptionRequest;
import com.winga.dto.request.ResolveDisputeRequest;
import com.winga.dto.request.QualificationTestRequest;
import com.winga.dto.request.SubscriptionPlanRequest;
import com.winga.dto.response.*;
import com.winga.dto.request.PaymentGatewayConfigRequest;
import com.winga.entity.User;
import com.winga.domain.enums.ModerationStatus;
import java.util.List;
import java.util.Map;
import com.winga.service.AdminService;
import com.winga.service.QualificationTestService;
import com.winga.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin", description = "Admin-only: users, stats, disputes")
public class AdminController {

    private final AdminService adminService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final QualificationTestService qualificationTestService;

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listUsers(pageable)));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID (includes profileImageUrl, cvUrl, profileCompleteness, profileVerified)")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(id, admin)));
    }

    @GetMapping("/users/{id}/experiences")
    @Operation(summary = "Get worker's work experiences (admin view)")
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> getUserExperiences(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserExperiences(id, admin)));
    }

    @PostMapping("/users")
    @Operation(summary = "Create user (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody AdminCreateUserRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createUser(request, admin)));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user (admin)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUser(id, request, admin)));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Deactivate user (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        adminService.deleteUser(id, admin);
        return ResponseEntity.ok(ApiResponse.ok("User deactivated"));
    }

    @PostMapping("/users/{id}/verify")
    @Operation(summary = "Verify or unverify a user")
    public ResponseEntity<ApiResponse<UserResponse>> verifyUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean verify,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.verifyUser(id, verify, admin)));
    }

    @PutMapping("/users/{id}/verify-profile")
    @Operation(summary = "Set worker profile as verified (badge). Admin only.")
    public ResponseEntity<ApiResponse<UserResponse>> verifyProfile(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean verified,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.verifyProfile(id, verified, admin)));
    }

    @PostMapping("/users/bulk-verify-profile")
    @Operation(summary = "Bulk verify or unverify worker profiles. Skips non-FREELANCER IDs.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkVerifyProfile(
            @RequestBody com.winga.dto.request.AdminBulkVerifyProfileRequest request,
            @AuthenticationPrincipal User admin) {
        int updated = adminService.bulkVerifyProfile(request, admin);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("updated", updated)));
    }

    @GetMapping("/stats")
    @Operation(summary = "Platform stats (users, jobs, proposals, contracts, revenue)")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats(@AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getStats(admin)));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Analytics: jobs per category, proposals per job (top 50), revenue in period (default last 30 days)")
    public ResponseEntity<ApiResponse<AdminAnalyticsResponse>> getAnalytics(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime to,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAnalytics(admin, from, to)));
    }

    @GetMapping(value = "/export/jobs", produces = "text/csv")
    @Operation(summary = "Export all jobs as CSV")
    public ResponseEntity<byte[]> exportJobsCsv(@AuthenticationPrincipal User admin) {
        byte[] csv = adminService.exportJobsCsv(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "winga-jobs.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping(value = "/export/users", produces = "text/csv")
    @Operation(summary = "Export all users as CSV")
    public ResponseEntity<byte[]> exportUsersCsv(@AuthenticationPrincipal User admin) {
        byte[] csv = adminService.exportUsersCsv(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "winga-users.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping(value = "/export/workers", produces = "text/csv")
    @Operation(summary = "Export workers (FREELANCER) as CSV. Optional: incompleteOnly, withCvOnly")
    public ResponseEntity<byte[]> exportWorkersCsv(
            @RequestParam(required = false) Boolean incompleteOnly,
            @RequestParam(required = false) Boolean withCvOnly,
            @AuthenticationPrincipal User admin) {
        byte[] csv = adminService.exportWorkersCsv(admin, Boolean.TRUE.equals(incompleteOnly), Boolean.TRUE.equals(withCvOnly));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "winga-workers.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @PostMapping(value = "/export/workers", produces = "text/csv")
    @Operation(summary = "Export selected workers (FREELANCER) as CSV. Body: { \"userIds\": [1,2,3] }")
    public ResponseEntity<byte[]> exportWorkersCsvSelected(
            @RequestBody(required = false) com.winga.dto.request.AdminExportWorkersRequest request,
            @AuthenticationPrincipal User admin) {
        List<Long> ids = request != null && request.userIds() != null ? request.userIds() : null;
        byte[] csv = adminService.exportWorkersCsv(admin, false, false, ids);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "winga-workers.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping(value = "/export/contracts", produces = "text/csv")
    @Operation(summary = "Export all contracts as CSV")
    public ResponseEntity<byte[]> exportContractsCsv(@AuthenticationPrincipal User admin) {
        byte[] csv = adminService.exportContractsCsv(admin);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "winga-contracts.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/disputes")
    @Operation(summary = "List disputed contracts")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> listDisputes(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listDisputes(pageable, admin)));
    }

    @GetMapping("/disputes/{id}")
    @Operation(summary = "Get dispute detail: original scope (job + milestones) + chat logs")
    public ResponseEntity<ApiResponse<DisputeDetailResponse>> getDisputeDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDisputeDetail(id, admin)));
    }

    @PatchMapping("/disputes/{id}/resolve")
    @Operation(summary = "Resolve dispute (Force Release): release escrow to CLIENT or FREELANCER")
    public ResponseEntity<ApiResponse<ContractResponse>> resolveDispute(
            @PathVariable Long id,
            @Valid @RequestBody ResolveDisputeRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.resolveDispute(id, request.releaseTo(), admin)));
    }

    // ─── Dashboard overview ────────────────────────────────────────────────────

    @GetMapping("/dashboard/overview")
    @Operation(summary = "Dashboard overview: metrics, applications over time, top categories")
    public ResponseEntity<ApiResponse<AdminDashboardOverviewResponse>> getDashboardOverview(
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardOverview(admin)));
    }

    // ─── Job moderation ─────────────────────────────────────────────────────────

    @GetMapping("/jobs/moderation")
    @Operation(summary = "List jobs pending moderation")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> listJobsForModeration(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listJobsForModeration(pageable, admin)));
    }

    @PatchMapping("/jobs/{id}/moderate")
    @Operation(summary = "Approve or reject a job")
    public ResponseEntity<ApiResponse<JobResponse>> moderateJob(
            @PathVariable Long id,
            @Valid @RequestBody ModerateJobRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.moderateJob(id, request, admin)));
    }

    @GetMapping("/jobs")
    @Operation(summary = "List all jobs (any status, optional filter)")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> listAllJobs(
            @RequestParam(required = false) JobStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listAllJobs(status, pageable, admin)));
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Get job by ID (admin, no view increment)")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getJobByIdForAdmin(id, admin)));
    }

    @PutMapping("/jobs/{id}")
    @Operation(summary = "Update job (admin)")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateJobRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateJobAdmin(id, request, admin)));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Delete or cancel job (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        adminService.deleteJobAdmin(id, admin);
        return ResponseEntity.ok(ApiResponse.ok("Job deleted or cancelled"));
    }

    @PostMapping("/jobs")
    @Operation(summary = "Create job on behalf of a client (admin)")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody AdminCreateJobRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createJobAdmin(request, admin)));
    }

    // ─── Applications / Proposals ─────────────────────────────────────────────────

    @GetMapping("/proposals")
    @Operation(summary = "List all proposals (optional filters: jobId, status, moderationStatus)")
    public ResponseEntity<ApiResponse<Page<ProposalResponse>>> listProposals(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) ProposalStatus status,
            @RequestParam(required = false) com.winga.domain.enums.ModerationStatus moderationStatus,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listProposals(jobId, status, moderationStatus, pageable, admin)));
    }

    @PatchMapping("/proposals/{id}/moderate")
    @Operation(summary = "Approve or reject a bid (moderation)")
    public ResponseEntity<ApiResponse<ProposalResponse>> moderateProposal(
            @PathVariable Long id,
            @RequestParam com.winga.domain.enums.ModerationStatus moderationStatus,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.moderateProposal(id, moderationStatus, admin)));
    }

    @GetMapping("/proposals/{id}")
    @Operation(summary = "Get proposal by ID")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposal(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getProposalById(id, admin)));
    }

    @PatchMapping("/proposals/{id}/status")
    @Operation(summary = "Update proposal status (shortlist, reject, etc.)")
    public ResponseEntity<ApiResponse<ProposalResponse>> updateProposalStatus(
            @PathVariable Long id,
            @RequestParam ProposalStatus status,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateProposalStatusAdmin(id, status, admin)));
    }

    @PostMapping("/proposals/bulk-status")
    @Operation(summary = "Bulk update proposal status")
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> bulkUpdateProposalStatus(
            @Valid @RequestBody AdminBulkProposalStatusRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.bulkUpdateProposalStatus(request, admin)));
    }

    // ─── Contracts / Hires ───────────────────────────────────────────────────────

    @GetMapping("/contracts")
    @Operation(summary = "List all contracts (optional filter: status)")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> listContracts(
            @RequestParam(required = false) ContractStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listContracts(status, pageable, admin)));
    }

    @GetMapping("/contracts/{id}")
    @Operation(summary = "Get contract by ID")
    public ResponseEntity<ApiResponse<ContractResponse>> getContract(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getContractById(id, admin)));
    }

    @PostMapping("/contracts/{id}/terminate")
    @Operation(summary = "End contract (admin): refund to client, job reopens")
    public ResponseEntity<ApiResponse<ContractResponse>> terminateContract(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.terminateContractAdmin(id, admin)));
    }

    // ─── Job categories ──────────────────────────────────────────────────────────

    @GetMapping("/categories")
    @Operation(summary = "List job categories (OFM: Chatter, VA, Editor, etc.)")
    public ResponseEntity<ApiResponse<List<JobCategoryResponse>>> listCategories(
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listCategories(admin)));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create job category")
    public ResponseEntity<ApiResponse<JobCategoryResponse>> createCategory(
            @Valid @RequestBody JobCategoryRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createCategory(request, admin)));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update job category")
    public ResponseEntity<ApiResponse<JobCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody JobCategoryRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateCategory(id, request, admin)));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete job category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        adminService.deleteCategory(id, admin);
        return ResponseEntity.ok(ApiResponse.ok("Deleted"));
    }

    // ─── Filter options (Employment Type, Social Media, Software, Languages) ─────

    @GetMapping("/filter-options")
    @Operation(summary = "List filter options by type (EMPLOYMENT_TYPE, SOCIAL_MEDIA, SOFTWARE, LANGUAGE)")
    public ResponseEntity<ApiResponse<List<FilterOptionResponse>>> listFilterOptions(
            @RequestParam FilterOptionType type,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listFilterOptions(type, admin)));
    }

    @PostMapping("/filter-options")
    @Operation(summary = "Create filter option")
    public ResponseEntity<ApiResponse<FilterOptionResponse>> createFilterOption(
            @Valid @RequestBody FilterOptionRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createFilterOption(request, admin)));
    }

    @PutMapping("/filter-options/{id}")
    @Operation(summary = "Update filter option")
    public ResponseEntity<ApiResponse<FilterOptionResponse>> updateFilterOption(
            @PathVariable Long id,
            @Valid @RequestBody FilterOptionRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateFilterOption(id, request, admin)));
    }

    @DeleteMapping("/filter-options/{id}")
    @Operation(summary = "Delete filter option")
    public ResponseEntity<ApiResponse<Void>> deleteFilterOption(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        adminService.deleteFilterOption(id, admin);
        return ResponseEntity.ok(ApiResponse.ok("Deleted"));
    }

    // ─── Languages (admin section: add/edit languages for filters) ───────────────

    @GetMapping("/languages")
    @Operation(summary = "List all languages (for find-jobs / find-workers)")
    public ResponseEntity<ApiResponse<List<FilterOptionResponse>>> listLanguages(@AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listFilterOptions(FilterOptionType.LANGUAGE, admin)));
    }

    @PostMapping("/languages")
    @Operation(summary = "Add a language")
    public ResponseEntity<ApiResponse<FilterOptionResponse>> createLanguage(
            @Valid @RequestBody AdminLanguageRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createLanguage(request, admin)));
    }

    @PutMapping("/languages/{id}")
    @Operation(summary = "Update a language")
    public ResponseEntity<ApiResponse<FilterOptionResponse>> updateLanguage(
            @PathVariable Long id,
            @Valid @RequestBody AdminLanguageRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateLanguage(id, request, admin)));
    }

    @DeleteMapping("/languages/{id}")
    @Operation(summary = "Delete a language")
    public ResponseEntity<ApiResponse<Void>> deleteLanguage(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        adminService.deleteFilterOption(id, admin);
        return ResponseEntity.ok(ApiResponse.ok("Deleted"));
    }

    // ─── Payment options ─────────────────────────────────────────────────────────

    @GetMapping("/payment-options")
    @Operation(summary = "List payment options")
    public ResponseEntity<ApiResponse<List<PaymentOptionResponse>>> listPaymentOptions(
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listPaymentOptions(admin)));
    }

    @PostMapping("/payment-options")
    @Operation(summary = "Create payment option")
    public ResponseEntity<ApiResponse<PaymentOptionResponse>> createPaymentOption(
            @Valid @RequestBody PaymentOptionRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createPaymentOption(request, admin)));
    }

    @PutMapping("/payment-options/{id}")
    @Operation(summary = "Update payment option")
    public ResponseEntity<ApiResponse<PaymentOptionResponse>> updatePaymentOption(
            @PathVariable Long id,
            @Valid @RequestBody PaymentOptionRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updatePaymentOption(id, request, admin)));
    }

    @DeleteMapping("/payment-options/{id}")
    @Operation(summary = "Delete payment option")
    public ResponseEntity<ApiResponse<Void>> deletePaymentOption(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        adminService.deletePaymentOption(id, admin);
        return ResponseEntity.ok(ApiResponse.ok("Deleted"));
    }

    // ─── Subscription plans (freelancer packages) ────────────────────────────────

    @GetMapping("/subscription-plans")
    @Operation(summary = "List all subscription plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> listSubscriptionPlans(
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.listAll()));
    }

    @GetMapping("/subscription-plans/{id}")
    @Operation(summary = "Get subscription plan by ID")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getSubscriptionPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.getById(id)));
    }

    @PostMapping("/subscription-plans")
    @Operation(summary = "Create subscription plan")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createSubscriptionPlan(
            @Valid @RequestBody SubscriptionPlanRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.create(request)));
    }

    @PutMapping("/subscription-plans/{id}")
    @Operation(summary = "Update subscription plan")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updateSubscriptionPlan(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.update(id, request)));
    }

    @DeleteMapping("/subscription-plans/{id}")
    @Operation(summary = "Delete subscription plan")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriptionPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        subscriptionPlanService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted"));
    }

    // ─── Qualification tests (worker tests: min/max score, status, added to profile when complete) ─

    @GetMapping("/qualification-tests")
    @Operation(summary = "List all qualification tests")
    public ResponseEntity<ApiResponse<List<QualificationTestResponse>>> listQualificationTests(
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(qualificationTestService.listAll()));
    }

    @GetMapping("/qualification-tests/{id}")
    @Operation(summary = "Get qualification test by ID")
    public ResponseEntity<ApiResponse<QualificationTestResponse>> getQualificationTest(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(qualificationTestService.getById(id)));
    }

    @PostMapping("/qualification-tests")
    @Operation(summary = "Create qualification test")
    public ResponseEntity<ApiResponse<QualificationTestResponse>> createQualificationTest(
            @Valid @RequestBody QualificationTestRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(qualificationTestService.create(request)));
    }

    @PutMapping("/qualification-tests/{id}")
    @Operation(summary = "Update qualification test")
    public ResponseEntity<ApiResponse<QualificationTestResponse>> updateQualificationTest(
            @PathVariable Long id,
            @Valid @RequestBody QualificationTestRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(qualificationTestService.update(id, request)));
    }

    @DeleteMapping("/qualification-tests/{id}")
    @Operation(summary = "Delete qualification test")
    public ResponseEntity<ApiResponse<Void>> deleteQualificationTest(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        qualificationTestService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted"));
    }

    // ─── Payment gateway config (API keys, M-Pesa, PayPal, etc.) ─────────────────

    @GetMapping("/payment-gateways")
    @Operation(summary = "List payment gateway configs (API keys / settings)")
    public ResponseEntity<ApiResponse<List<PaymentGatewayConfigResponse>>> listPaymentGateways(
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listPaymentGatewayConfigs(admin)));
    }

    @GetMapping("/payment-gateways/{slug}")
    @Operation(summary = "Get payment gateway config by slug (e.g. mpesa)")
    public ResponseEntity<ApiResponse<PaymentGatewayConfigResponse>> getPaymentGateway(
            @PathVariable String slug,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPaymentGatewayConfigBySlug(slug, admin)));
    }

    @PostMapping("/payment-gateways")
    @Operation(summary = "Add payment gateway config (API keys, shortcode, callback URL, etc.)")
    public ResponseEntity<ApiResponse<PaymentGatewayConfigResponse>> createPaymentGateway(
            @Valid @RequestBody PaymentGatewayConfigRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createPaymentGatewayConfig(request, admin)));
    }

    @PutMapping("/payment-gateways/{id}")
    @Operation(summary = "Update payment gateway config")
    public ResponseEntity<ApiResponse<PaymentGatewayConfigResponse>> updatePaymentGateway(
            @PathVariable Long id,
            @Valid @RequestBody PaymentGatewayConfigRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updatePaymentGatewayConfig(id, request, admin)));
    }

    @DeleteMapping("/payment-gateways/{id}")
    @Operation(summary = "Delete payment gateway config")
    public ResponseEntity<ApiResponse<Void>> deletePaymentGateway(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        adminService.deletePaymentGatewayConfig(id, admin);
        return ResponseEntity.ok(ApiResponse.ok("Deleted"));
    }

    // ─── Portfolio & Certification moderation ───────────────────────────────────

    @GetMapping("/portfolio-items")
    @Operation(summary = "List portfolio items by moderation status")
    public ResponseEntity<ApiResponse<Page<PortfolioItemResponse>>> listPortfolioItems(
            @RequestParam ModerationStatus moderationStatus,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listPortfolioItems(moderationStatus, pageable, admin)));
    }

    @PatchMapping("/portfolio-items/{id}/moderate")
    @Operation(summary = "Approve or reject a portfolio item")
    public ResponseEntity<ApiResponse<PortfolioItemResponse>> moderatePortfolioItem(
            @PathVariable Long id,
            @RequestParam ModerationStatus moderationStatus,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.moderatePortfolioItem(id, moderationStatus, admin)));
    }

    @GetMapping("/certifications")
    @Operation(summary = "List certifications by moderation status")
    public ResponseEntity<ApiResponse<Page<CertificationResponse>>> listCertifications(
            @RequestParam ModerationStatus moderationStatus,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listCertifications(moderationStatus, pageable, admin)));
    }

    @PatchMapping("/certifications/{id}/moderate")
    @Operation(summary = "Approve or reject a certification")
    public ResponseEntity<ApiResponse<CertificationResponse>> moderateCertification(
            @PathVariable Long id,
            @RequestParam ModerationStatus moderationStatus,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.moderateCertification(id, moderationStatus, admin)));
    }
}
