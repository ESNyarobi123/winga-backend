package com.winga.controller;

import com.winga.domain.enums.ContractStatus;
import com.winga.domain.enums.JobStatus;
import com.winga.domain.enums.ProposalStatus;
import com.winga.dto.request.AdminBulkProposalStatusRequest;
import com.winga.dto.request.AdminCreateJobRequest;
import com.winga.dto.request.AdminCreateUserRequest;
import com.winga.dto.request.AdminUpdateJobRequest;
import com.winga.dto.request.AdminUpdateUserRequest;
import com.winga.dto.request.JobCategoryRequest;
import com.winga.dto.request.ModerateJobRequest;
import com.winga.dto.request.PaymentOptionRequest;
import com.winga.dto.request.ResolveDisputeRequest;
import com.winga.dto.response.*;
import com.winga.entity.User;
import com.winga.domain.enums.ModerationStatus;
import java.util.List;
import com.winga.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listUsers(pageable)));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(id, admin)));
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

    @GetMapping("/stats")
    @Operation(summary = "Platform stats (users, jobs, contracts, revenue)")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats(@AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getStats(admin)));
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
