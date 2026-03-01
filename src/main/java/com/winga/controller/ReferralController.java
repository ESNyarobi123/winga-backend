package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.ReferralResponse;
import com.winga.entity.User;
import com.winga.service.ReferralCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
@Tag(name = "Referral", description = "Affiliate / referral code and stats")
public class ReferralController {

    private final ReferralCodeService referralCodeService;

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my referral code, link, and stats (signup count, hire count, commission balance)")
    public ResponseEntity<ApiResponse<ReferralResponse>> getMyReferral(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(referralCodeService.getMyReferral(user)));
    }
}
