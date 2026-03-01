package com.winga.dto.request;

import com.winga.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 100) String fullName,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotNull Role role,
        String phoneNumber
) {}
