package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.SubscriptionPlanResponse;
import com.winga.dto.response.SubscriptionResponse;
import com.winga.entity.User;
import com.winga.service.SubscriptionPlanService;
import com.winga.service.SubscriptionService;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Service provider subscription status and plans")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanService subscriptionPlanService;

    /** Public: list active subscription plans (for freelancer pricing page). */
    @GetMapping("/plans")
    @Operation(summary = "List active subscription plans (public)")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> listPlans() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionPlanService.listActive()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Get my current subscription (active or null)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(@AuthenticationPrincipal User user) {
        return subscriptionService.getActiveSubscription(user.getId())
                .map(sub -> {
                    boolean active = sub.getEndsAt().isAfter(LocalDateTime.now()) && "ACTIVE".equals(sub.getStatus());
                    return ResponseEntity.ok(ApiResponse.success(new SubscriptionResponse(
                            sub.getId(), sub.getPlanId(), sub.getStatus(),
                            sub.getStartsAt(), sub.getEndsAt(), active)));
                })
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success((SubscriptionResponse) null)));
    }
}
