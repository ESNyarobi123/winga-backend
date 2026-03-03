package com.winga.controller;

import com.winga.entity.User;
import com.winga.dto.request.JobRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.FilterOptionsPublicResponse;
import com.winga.dto.response.JobResponse;
import com.winga.service.JobService;
import com.winga.service.SavedJobService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job board — browse, post, manage jobs")
public class JobController {

    private final JobService jobService;
    private final SavedJobService savedJobService;

    // ─── Public ──────────────────────────────────────────────────────────────────

    @GetMapping("/categories")
    @Operation(summary = "List job categories for find-jobs filters (from DB, admin-managed)")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(jobService.getCategoriesForPublic()));
    }

    @GetMapping("/filter-options")
    @Operation(summary = "Filter options for find-jobs: Employment Type, Social Media, Software, Languages (from DB, admin-managed)")
    public ResponseEntity<ApiResponse<FilterOptionsPublicResponse>> getFilterOptions() {
        return ResponseEntity.ok(ApiResponse.success(jobService.getFilterOptionsForPublic()));
    }

    @GetMapping
    @Operation(summary = "Browse open jobs (search, filters, location, sort: createdAt|budget|deadline|title)")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> browseJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String socialMedia,
            @RequestParam(required = false) String software,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<JobResponse> jobs = jobService.searchJobs(keyword, category,
                employmentType, socialMedia, software, language,
                city, region, featured,
                minBudget, maxBudget, pageable);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "View job details (increments view count)")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getJobById(id)));
    }

    // ─── Saved jobs (bookmarks) — any authenticated user ─────────────────────────

    @GetMapping("/saved")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "List my saved jobs (bookmarks)")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getMySavedJobs(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(savedJobService.getMySavedJobs(user.getId(), pageable)));
    }

    @PostMapping("/{id}/save")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Save (bookmark) a job")
    public ResponseEntity<ApiResponse<Void>> saveJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        savedJobService.saveJob(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Job saved."));
    }

    @DeleteMapping("/{id}/save")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove job from saved (unbookmark)")
    public ResponseEntity<ApiResponse<Void>> unsaveJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        savedJobService.unsaveJob(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Job removed from saved."));
    }

    // ─── Client ──────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Post a new job (CLIENT only)")
    public ResponseEntity<ApiResponse<JobResponse>> postJob(
            @AuthenticationPrincipal User client,
            @Valid @RequestBody JobRequest request) {

        JobResponse job = jobService.createJob(client, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posted successfully!", job));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update an open job (CLIENT owner only)")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User client,
            @Valid @RequestBody JobRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Job updated.", jobService.updateJob(id, client, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cancel a job (CLIENT owner only)")
    public ResponseEntity<ApiResponse<Void>> cancelJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User client) {

        jobService.cancelJob(id, client);
        return ResponseEntity.ok(ApiResponse.ok("Job cancelled."));
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all my posted jobs (CLIENT)")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getMyJobs(
            @AuthenticationPrincipal User client,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(jobService.getClientJobs(client.getId(), pageable)));
    }
}
