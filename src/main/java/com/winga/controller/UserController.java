package com.winga.controller;

import com.winga.entity.User;
import com.winga.dto.request.UpdateProfileRequest;
import com.winga.dto.request.WorkExperienceRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.ProfileChecklistResponse;
import com.winga.dto.response.RatingSummaryResponse;
import com.winga.dto.response.ReviewResponse;
import com.winga.dto.response.UserResponse;
import com.winga.dto.response.UserSummaryResponse;
import com.winga.dto.response.WorkerTestResultResponse;
import com.winga.dto.response.WorkExperienceResponse;
import com.winga.service.ReviewService;
import com.winga.service.UserService;
import com.winga.service.WorkExperienceService;
import com.winga.service.WorkerTestService;
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

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Users", description = "User profile endpoints")
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final WorkExperienceService workExperienceService;
    private final WorkerTestService workerTestService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(userService.toUserResponse(user)));
    }

    @GetMapping("/me/profile-checklist")
    @Operation(summary = "Worker onboarding: list of missing required fields and completeness %")
    public ResponseEntity<ApiResponse<ProfileChecklistResponse>> getProfileChecklist(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfileChecklist(user)));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update my profile (includes job seeker: telegram, country, languages, cvUrl, workType, timezone, paymentPreferences)")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(user, request)));
    }

    @GetMapping("/me/experiences")
    @Operation(summary = "Get my work experiences (job seeker)")
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> getMyExperiences(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(workExperienceService.getMyExperiences(user.getId())));
    }

    @PostMapping("/me/experiences")
    @Operation(summary = "Add one work experience (job seeker)")
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> addExperience(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(workExperienceService.addExperience(user, request)));
    }

    @PutMapping("/me/experiences")
    @Operation(summary = "Replace all my work experiences (job seeker)")
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> replaceExperiences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody List<WorkExperienceRequest> request) {
        return ResponseEntity.ok(ApiResponse.success(workExperienceService.replaceMyExperiences(user, request)));
    }

    @DeleteMapping("/me/experiences/{experienceId}")
    @Operation(summary = "Delete one work experience (job seeker)")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @PathVariable Long experienceId,
            @AuthenticationPrincipal User user) {
        workExperienceService.deleteExperience(experienceId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Experience deleted."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user's public profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.toUserResponse(userService.getById(id))));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get user profile + rating summary (one call for worker cards)")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getUserSummary(@PathVariable Long id) {
        UserResponse user = userService.toUserResponse(userService.getById(id));
        double avg = reviewService.getAverageRating(id);
        long count = reviewService.getReviewCount(id);
        return ResponseEntity.ok(ApiResponse.success(new UserSummaryResponse(user, avg, count)));
    }

    @GetMapping("/{id}/experiences")
    @Operation(summary = "Get work experiences for this user (public profile)")
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> getExperiencesForUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(workExperienceService.getMyExperiences(id)));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Get reviews received by this user (for profile)")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsForUser(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsForUser(id, pageable)));
    }

    @GetMapping("/{id}/rating")
    @Operation(summary = "Get rating summary (average + count) for profile")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>> getRatingSummary(@PathVariable Long id) {
        double avg = reviewService.getAverageRating(id);
        long count = reviewService.getReviewCount(id);
        return ResponseEntity.ok(ApiResponse.success(new RatingSummaryResponse(avg, count)));
    }

    @GetMapping("/{id}/completed-tests")
    @Operation(summary = "Get completed qualification tests for this user (for profile display)")
    public ResponseEntity<ApiResponse<List<WorkerTestResultResponse>>> getCompletedTests(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(workerTestService.getMyCompleted(id)));
    }
}
