package com.winga.controller;

import java.util.List;

import com.winga.entity.User;
import com.winga.dto.request.MilestoneRequest;
import com.winga.dto.request.ReviewRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.request.AddOnRequest;
import com.winga.dto.response.AddOnResponse;
import com.winga.dto.response.ContractResponse;
import com.winga.dto.response.MilestoneResponse;
import com.winga.dto.response.ReviewResponse;
import com.winga.service.ContractAddOnService;
import com.winga.service.ContractService;
import com.winga.service.ReviewService;
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
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Contracts & Escrow", description = "Hire, submit, approve — escrow engine")
public class ContractController {

    private final ContractService contractService;
    private final ReviewService reviewService;
    private final ContractAddOnService contractAddOnService;

    // ─── Algorithm 1: Hire & Lock Escrow ─────────────────────────────────────────

    @PostMapping("/hire/{proposalId}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Hire a freelancer & lock funds in escrow (CLIENT only)")
    public ResponseEntity<ApiResponse<ContractResponse>> hire(
            @PathVariable Long proposalId,
            @AuthenticationPrincipal User client) {

        ContractResponse contract = contractService.hireFreelancer(proposalId, client);
        return ResponseEntity.ok(ApiResponse.success(
                "Freelancer hired! Funds secured in escrow. 🔒", contract));
    }

    // ─── Algorithm 2a: Submit Work
    // ────────────────────────────────────────────────

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Submit work for review (FREELANCER only)")
    public ResponseEntity<ApiResponse<ContractResponse>> submitWork(
            @PathVariable Long id,
            @AuthenticationPrincipal User freelancer,
            @RequestParam(required = false) String note) {

        return ResponseEntity.ok(ApiResponse.success(
                "Work submitted for client review.", contractService.submitWork(id, freelancer, note)));
    }

    // ─── Algorithm 2b: Approve & Release Escrow ──────────────────────────────────

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Approve work & release payment from escrow (CLIENT only)")
    public ResponseEntity<ApiResponse<ContractResponse>> approveWork(
            @PathVariable Long id,
            @AuthenticationPrincipal User client) {

        ContractResponse contract = contractService.approveWork(id, client);
        return ResponseEntity.ok(ApiResponse.success(
                "Work approved! Payment released to freelancer. 💸", contract));
    }

    @PostMapping("/{id}/request-changes")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Request changes (counts against revision limit; client must add milestone when limit reached)")
    public ResponseEntity<ApiResponse<ContractResponse>> requestChanges(
            @PathVariable Long id,
            @AuthenticationPrincipal User client,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(ApiResponse.success(
                contractService.requestChanges(id, client, note)));
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "End contract (CLIENT only): refunds remaining escrow to client, job reopens")
    public ResponseEntity<ApiResponse<ContractResponse>> terminateContract(
            @PathVariable Long id,
            @AuthenticationPrincipal User client) {
        return ResponseEntity.ok(ApiResponse.success(
                "Contract ended. Refund credited to your wallet.", contractService.terminateContract(id, client)));
    }

    // ─── Dispute
    // ──────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/dispute")
    @Operation(summary = "Raise a dispute on a contract")
    public ResponseEntity<ApiResponse<ContractResponse>> raiseDispute(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam String reason) {

        return ResponseEntity.ok(ApiResponse.success(
                "Dispute raised. Admin will review.", contractService.raiseDispute(id, user, reason)));
    }

    @PostMapping("/{id}/reviews")
    @Operation(summary = "Submit a review for the other party (after contract completed)")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.createReview(id, user, request)));
    }

    // ─── Add-ons (Propose Extra Contract from chat) ──────────────────────────────

    @GetMapping("/{id}/add-ons")
    @Operation(summary = "List add-ons for this contract")
    public ResponseEntity<ApiResponse<List<AddOnResponse>>> listAddOns(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(contractAddOnService.listByContract(id, user)));
    }

    @PostMapping("/{id}/add-ons")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Propose add-on (extra work) — client gets notification, must Accept & Deposit")
    public ResponseEntity<ApiResponse<AddOnResponse>> proposeAddOn(
            @PathVariable Long id,
            @AuthenticationPrincipal User freelancer,
            @Valid @RequestBody AddOnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contractAddOnService.propose(id, freelancer, request)));
    }

    @PostMapping("/{id}/add-ons/{addOnId}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Accept add-on & deposit (client pays from wallet into escrow)")
    public ResponseEntity<ApiResponse<AddOnResponse>> acceptAddOn(
            @PathVariable Long id,
            @PathVariable Long addOnId,
            @AuthenticationPrincipal User client) {
        return ResponseEntity.ok(ApiResponse.success(contractAddOnService.accept(addOnId, client)));
    }

    @PostMapping("/{id}/add-ons/{addOnId}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Reject proposed add-on")
    public ResponseEntity<ApiResponse<AddOnResponse>> rejectAddOn(
            @PathVariable Long id,
            @PathVariable Long addOnId,
            @AuthenticationPrincipal User client) {
        return ResponseEntity.ok(ApiResponse.success(contractAddOnService.reject(addOnId, client)));
    }

    @PostMapping("/{id}/add-ons/{addOnId}/complete")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Mark add-on as complete — release payment to freelancer")
    public ResponseEntity<ApiResponse<AddOnResponse>> completeAddOn(
            @PathVariable Long id,
            @PathVariable Long addOnId,
            @AuthenticationPrincipal User client) {
        return ResponseEntity.ok(ApiResponse.success(contractAddOnService.complete(addOnId, client)));
    }

    // ─── Queries ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "View contract details")
    public ResponseEntity<ApiResponse<ContractResponse>> getContract(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success(contractService.getContract(id, user)));
    }

    @GetMapping("/my-contracts")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "View my active contracts (FREELANCER)")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> myContracts(
            @AuthenticationPrincipal User freelancer,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                contractService.getFreelancerContracts(freelancer.getId(), pageable)));
    }

    @GetMapping("/client/my-contracts")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "View contracts I've created (CLIENT)")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> clientContracts(
            @AuthenticationPrincipal User client,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                contractService.getClientContracts(client.getId(), pageable)));
    }

    // ─── Milestones ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/milestones")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Add a milestone to contract (CLIENT only)")
    public ResponseEntity<ApiResponse<MilestoneResponse>> addMilestone(
            @PathVariable Long id,
            @AuthenticationPrincipal User client,
            @Valid @RequestBody MilestoneRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                contractService.addMilestone(id, client, request)));
    }

    @PostMapping("/{id}/milestones/{milestoneId}/submit")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Submit milestone work for review (FREELANCER only)")
    public ResponseEntity<ApiResponse<MilestoneResponse>> submitMilestone(
            @PathVariable Long id,
            @PathVariable Long milestoneId,
            @AuthenticationPrincipal User freelancer,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(ApiResponse.success(
                contractService.submitMilestone(id, milestoneId, freelancer, note)));
    }

    @PostMapping("/milestones/{milestoneId}/approve")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Approve a milestone & release partial payment (CLIENT only)")
    public ResponseEntity<ApiResponse<?>> approveMilestone(
            @PathVariable Long milestoneId,
            @AuthenticationPrincipal User client) {

        return ResponseEntity.ok(ApiResponse.success(
                "Milestone approved! Payment released. ✅",
                contractService.approveMilestone(milestoneId, client)));
    }
}
