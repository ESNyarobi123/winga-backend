package com.winga.controller;

import com.winga.entity.User;
import com.winga.dto.request.LoginRequest;
import com.winga.dto.request.RefreshTokenRequest;
import com.winga.dto.request.RegisterCompleteRequest;
import com.winga.dto.request.RegisterRequest;
import com.winga.dto.request.ResetPasswordRequest;
import com.winga.dto.request.SendOtpRequest;
import com.winga.dto.request.VerifyOtpRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.AuthResponse;
import com.winga.dto.response.UserResponse;
import com.winga.dto.response.VerifyOtpResponse;
import com.winga.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register & Login endpoints")
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current user (alias for /api/users/me)")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(userService.toUserResponse(user)));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user (Client or Freelancer)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse auth = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful! Welcome to Winga 🇹🇿", auth));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email + password (legacy)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse auth = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful!", auth));
    }

    @PostMapping("/admin/login")
    @Operation(summary = "Admin dashboard login (ADMIN/SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse auth = userService.loginAsAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Welcome to Winga Admin", auth));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens: exchange refresh token for new access + new refresh (rotation)")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse auth = userService.refreshTokens(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Tokens refreshed", auth));
    }

    // ─── OTP flow: one screen for register & login (email → OTP → dashboard or choose role) ───

    @PostMapping("/send-otp")
    @Operation(summary = "Request OTP to email (for register or login)")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        userService.sendOtp(request.email());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to your email. Check your inbox."));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP: existing user → login (auth); new user → registrationToken (choose role then complete)")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse result = userService.verifyOtp(request.email(), request.otp());
        String message = result.requiresRegistration()
                ? "Email verified. Choose your role and complete registration."
                : "Login successful!";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request OTP to reset password (sends same OTP as send-otp)")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody SendOtpRequest request) {
        userService.sendOtp(request.email());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent. Use it in reset-password within the validity period."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Set new password using OTP (after forgot-password)")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPasswordWithOtp(request.email(), request.otp(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password updated. You can now login with your new password."));
    }

    @PostMapping("/register/complete")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Complete registration after OTP (Authorization: Bearer <registrationToken>)")
    public ResponseEntity<ApiResponse<AuthResponse>> completeRegistration(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RegisterCompleteRequest request) {
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : null;
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Missing or invalid registration token. Verify OTP first."));
        }
        AuthResponse auth = userService.completeRegistration(token, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration complete! Welcome to Winga 🇹🇿", auth));
    }
}
