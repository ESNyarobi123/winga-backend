package com.winga.dto.request;

import com.winga.domain.enums.Role;
import com.winga.domain.enums.VerificationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AdminUpdateUserRequest(
        @Size(min = 2, max = 100) String fullName,
        @Email @Size(max = 100) String email,
        @Size(min = 6, max = 100) String password,
        Role role,
        String phoneNumber,
        Boolean isVerified,
        VerificationStatus verificationStatus,
        Boolean isActive
) {}
