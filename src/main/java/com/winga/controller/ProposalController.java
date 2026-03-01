package com.winga.controller;

import com.winga.entity.User;
import com.winga.domain.enums.ProposalStatus;
import com.winga.dto.request.ProposalRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.ProposalResponse;
import com.winga.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Proposals", description = "Submit and manage job proposals")
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('FREELANCER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a proposal for a job (FREELANCER only)")
    public ResponseEntity<ApiResponse<ProposalResponse>> submit(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User freelancer,
            @Valid @RequestBody ProposalRequest request) {

        ProposalResponse proposal = proposalService.submitProposal(jobId, freelancer, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Proposal submitted successfully!", proposal));
    }

    @GetMapping("/my-proposals")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "View my submitted proposals (FREELANCER)")
    public ResponseEntity<ApiResponse<Page<ProposalResponse>>> myProposals(
            @AuthenticationPrincipal User freelancer,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                proposalService.getMyProposals(freelancer.getId(), pageable)));
    }

    @GetMapping("/jobs/{jobId}/applicants")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "View all applicants for my job (CLIENT only)")
    public ResponseEntity<ApiResponse<Page<ProposalResponse>>> getApplicants(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User client,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                proposalService.getJobProposals(jobId, client, pageable)));
    }

    @PatchMapping("/{proposalId}/status")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Update proposal status — shortlist or reject (CLIENT only)")
    public ResponseEntity<ApiResponse<ProposalResponse>> updateStatus(
            @PathVariable Long proposalId,
            @RequestParam ProposalStatus status,
            @AuthenticationPrincipal User client) {

        return ResponseEntity.ok(ApiResponse.success(
                proposalService.updateProposalStatus(proposalId, client, status)));
    }
}
