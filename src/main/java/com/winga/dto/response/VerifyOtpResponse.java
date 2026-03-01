package com.winga.dto.response;

/**
 * After verifying OTP: either login (existing user) or pending registration (new user).
 */
public record VerifyOtpResponse(
        boolean requiresRegistration,
        String registrationToken,
        AuthResponse auth) {

    /** Existing user: login with tokens and user. */
    public static VerifyOtpResponse login(AuthResponse auth) {
        return new VerifyOtpResponse(false, null, auth);
    }

    /** New user: must call POST /api/auth/register/complete with registrationToken. */
    public static VerifyOtpResponse pendingRegistration(String registrationToken) {
        return new VerifyOtpResponse(true, registrationToken, null);
    }
}
