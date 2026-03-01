package com.winga.dto.request;

import com.winga.domain.enums.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Full name is required") @Size(min = 2, max = 100) String fullName,

        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,

        @Pattern(regexp = "^(\\+255|0)[67]\\d{8}$", message = "Invalid Tanzanian phone number") String phoneNumber,

        @NotNull(message = "Role is required") Role role) {
}
