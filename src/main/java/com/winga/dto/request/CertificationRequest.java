package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CertificationRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String issuer,
        @NotBlank @Size(max = 500) String fileUrl,
        LocalDate issuedAt) {}
