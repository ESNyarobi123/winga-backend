package com.winga.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user) {
    public AuthResponse(String accessToken, String refreshToken, UserResponse user) {
        this(accessToken, refreshToken, "Bearer", 86400L, user);
    }
}
