package com.winga.dto.response;

import java.util.List;

/**
 * For worker onboarding UI: which required profile fields are missing.
 * GET /api/users/me/profile-checklist (FREELANCER only).
 */
public record ProfileChecklistResponse(
        boolean complete,
        int profileCompleteness,
        List<String> missingFields
) {}
