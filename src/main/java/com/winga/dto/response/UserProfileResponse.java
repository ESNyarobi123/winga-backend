package com.winga.dto.response;

import com.winga.domain.enums.Role;
import java.time.LocalDateTime;

/** Profile view — same shape as UserResponse (structure: response/UserProfileResponse). */
public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        Role role,
        String profileImageUrl,
        String bio,
        Boolean isVerified,
        LocalDateTime createdAt) {
}
