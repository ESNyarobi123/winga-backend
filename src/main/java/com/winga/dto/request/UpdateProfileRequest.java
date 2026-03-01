package com.winga.dto.request;

/**
 * Update my profile. All fields optional — only provided ones are updated.
 * Job seeker: telegram, country, languages, cvUrl, workType, timezone, paymentPreferences.
 * Employer: companyName (agency name).
 */
public record UpdateProfileRequest(
        String fullName,
        String phoneNumber,
        String bio,
        String skills,
        String profileImageUrl,
        String companyName,
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
        Long defaultCategoryId
) {}
