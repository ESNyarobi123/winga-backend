package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.EmployerDashboardOverviewResponse;
import com.winga.entity.User;
import com.winga.service.EmployerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT') or hasRole('EMPLOYER_ADMIN') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Employer Dashboard", description = "Employer/Recruiter dashboard: overview, my jobs, applications")
public class EmployerDashboardController {

    private final EmployerDashboardService employerDashboardService;

    @GetMapping("/dashboard/overview")
    @Operation(summary = "Employer dashboard overview: active jobs, applications, hires, response rate, charts")
    public ResponseEntity<ApiResponse<EmployerDashboardOverviewResponse>> getOverview(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(employerDashboardService.getOverview(user)));
    }
}
