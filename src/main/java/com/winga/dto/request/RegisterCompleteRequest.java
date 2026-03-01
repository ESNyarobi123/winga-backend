package com.winga.dto.request;

import com.winga.domain.enums.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Complete registration after OTP.
 * Flow: Select Role (Employer/Seeker) → optional Role-Specific Details (industry, etc.) or Skip → Dashboard.
 * fullName/industry/companyName optional; when skipped, user still reaches dashboard.
 */
public record RegisterCompleteRequest(
        @NotNull(message = "Role is required") Role role,
        @Size(min = 1, max = 100) String fullName,
        @Size(max = 100) String industry,
        @Size(max = 200) String companyName,
        @Size(max = 32) String referralCode) {
}
