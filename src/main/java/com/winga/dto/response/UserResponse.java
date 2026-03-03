package com.winga.dto.response;

import com.winga.domain.enums.Role;
import com.winga.domain.enums.VerificationStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        Role role,
        String profileImageUrl,
        String bio,
        String headline,
        String skills,
        String industry,
        String companyName,
        Boolean isVerified,
        VerificationStatus verificationStatus,
        Boolean isActive,
        LocalDateTime createdAt,
        String telegram,
        String country,
        String countryCode,
        String languages,
        String cvUrl,
        String workType,
        String timezone,
        String paymentPreferences,
        String typeSpeed,
        String internetSpeed,
        String computerSpecs,
        Boolean hasWebcam,
        String city,
        String region,
        java.math.BigDecimal latitude,
        java.math.BigDecimal longitude,
        Long defaultCategoryId,
        /** Worker profile completeness 0–100 (only for FREELANCER). */
        Integer profileCompleteness,
        /** True when profileCompleteness >= 100. */
        Boolean isProfileComplete,
        /** Admin-verified profile (worker). */
        Boolean profileVerified,
        /** When admin verified the profile. */
        LocalDateTime profileVerifiedAt) {
}
