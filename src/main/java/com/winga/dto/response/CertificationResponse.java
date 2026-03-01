package com.winga.dto.response;

import com.winga.domain.enums.ModerationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CertificationResponse(
        Long id,
        String name,
        String issuer,
        String fileUrl,
        LocalDate issuedAt,
        ModerationStatus moderationStatus,
        LocalDateTime createdAt) {}
