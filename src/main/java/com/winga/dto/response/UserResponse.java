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
        String skills,
        String industry,
        String companyName,
        Boolean isVerified,
        VerificationStatus verificationStatus,
        Boolean isActive,
        LocalDateTime createdAt,
        String telegram,
        String country,
        String languages,
        String cvUrl,
        String workType,
        String timezone,
        String paymentPreferences,
        String city,
        String region,
        java.math.BigDecimal latitude,
        java.math.BigDecimal longitude,
        Long defaultCategoryId) {
}
