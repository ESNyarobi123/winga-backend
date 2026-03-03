package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String email,
        @NotBlank String otp,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String newPassword
) {}
