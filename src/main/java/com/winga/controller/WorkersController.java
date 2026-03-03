package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.UserResponse;
import com.winga.entity.User;
import com.winga.service.SavedWorkerService;
import com.winga.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@Tag(name = "Workers", description = "Browse workers (freelancers), save favourites")
public class WorkersController {

    private final UserService userService;
    private final SavedWorkerService savedWorkerService;

    @GetMapping
    @Operation(summary = "List workers (search, filters: profileVerified, profileComplete; sort: createdAt|fullName)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listWorkers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean profileVerified,
            @RequestParam(required = false) Boolean profileComplete,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.findWorkers(
                keyword, employmentType, language, skill, categoryId, profileVerified, profileComplete, pageable)));
    }

    @GetMapping("/saved")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "List my saved workers (bookmarks)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getMySavedWorkers(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(savedWorkerService.getMySavedWorkers(user.getId(), pageable)));
    }

    @PostMapping("/{id}/save")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Save (bookmark) a worker")
    public ResponseEntity<ApiResponse<Void>> saveWorker(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        savedWorkerService.saveWorker(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Worker saved."));
    }

    @DeleteMapping("/{id}/save")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove worker from saved (unbookmark)")
    public ResponseEntity<ApiResponse<Void>> unsaveWorker(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        savedWorkerService.unsaveWorker(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Worker removed from saved."));
    }

    @GetMapping("/{id}/saved")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Check if current user has saved this worker (for bookmark icon)")
    public ResponseEntity<ApiResponse<Boolean>> isWorkerSaved(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(savedWorkerService.isSavedByUser(user.getId(), id)));
    }
}
