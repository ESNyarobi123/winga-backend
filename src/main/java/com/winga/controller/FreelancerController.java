package com.winga.controller;

import com.winga.entity.User;
import com.winga.dto.request.SubmitTestScoreRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.ContractResponse;
import com.winga.dto.response.FreelancerDashboardResponse;
import com.winga.dto.response.ProposalResponse;
import com.winga.dto.response.QualificationTestResponse;
import com.winga.dto.response.WorkerTestResultResponse;
import com.winga.service.ContractService;
import com.winga.service.FreelancerService;
import com.winga.service.ProposalService;
import com.winga.service.QualificationTestService;
import com.winga.service.WorkerTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/freelancer")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('FREELANCER')")
@Tag(name = "Freelancer (Worker)", description = "Worker dashboard, my contracts, my proposals")
public class FreelancerController {

    private final FreelancerService freelancerService;
    private final ContractService contractService;
    private final ProposalService proposalService;
    private final QualificationTestService qualificationTestService;
    private final WorkerTestService workerTestService;

    @GetMapping("/dashboard")
    @Operation(summary = "Worker dashboard: balance, total earned, active contracts & pending proposals count")
    public ResponseEntity<ApiResponse<FreelancerDashboardResponse>> getDashboard(
            @AuthenticationPrincipal User freelancer) {
        return ResponseEntity.ok(ApiResponse.success(
                freelancerService.getDashboard(freelancer.getId())));
    }

    @GetMapping("/my-contracts")
    @Operation(summary = "View my active contracts (FREELANCER)")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> myContracts(
            @AuthenticationPrincipal User freelancer,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                contractService.getFreelancerContracts(freelancer.getId(), pageable)));
    }

    @GetMapping("/my-proposals")
    @Operation(summary = "View my submitted proposals (FREELANCER)")
    public ResponseEntity<ApiResponse<Page<ProposalResponse>>> myProposals(
            @AuthenticationPrincipal User freelancer,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                proposalService.getMyProposals(freelancer.getId(), pageable)));
    }

    @GetMapping("/tests")
    @Operation(summary = "List active qualification tests")
    public ResponseEntity<ApiResponse<List<QualificationTestResponse>>> listTests() {
        return ResponseEntity.ok(ApiResponse.success(qualificationTestService.listActive()));
    }

    @GetMapping("/my-test-results")
    @Operation(summary = "My test results (all active tests with status; completed = added to profile)")
    public ResponseEntity<ApiResponse<List<WorkerTestResultResponse>>> myTestResults(
            @AuthenticationPrincipal User freelancer) {
        return ResponseEntity.ok(ApiResponse.success(workerTestService.getMyResults(freelancer.getId())));
    }

    @PostMapping("/tests/{id}/submit")
    @Operation(summary = "Submit test score; when score >= min_score, test is marked completed (added to profile)")
    public ResponseEntity<ApiResponse<WorkerTestResultResponse>> submitTestScore(
            @PathVariable Long id,
            @Valid @RequestBody SubmitTestScoreRequest request,
            @AuthenticationPrincipal User freelancer) {
        return ResponseEntity.ok(ApiResponse.success(
                workerTestService.submitScore(freelancer, id, request.score())));
    }
}
