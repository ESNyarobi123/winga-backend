package com.winga.dto.request;

/**
 * Update my profile. All fields optional — only provided ones are updated.
 * Worker profile: fullName, country, headline (✅), languages, paymentPreferences, workType, timezone;
 * optional: typeSpeed, internetSpeed, computerSpecs, hasWebcam, countryCode, profileImageUrl.
 */
public record UpdateProfileRequest(
        String fullName,
        String phoneNumber,
        String bio,
        String headline,
        String skills,
        String profileImageUrl,
        String companyName,
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
        Long defaultCategoryId
) {}
