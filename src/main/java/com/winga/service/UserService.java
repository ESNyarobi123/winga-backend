package com.winga.service;

import com.winga.entity.User;
import com.winga.entity.Wallet;
import com.winga.domain.enums.Currency;
import com.winga.domain.enums.Role;
import com.winga.dto.request.LoginRequest;
import com.winga.dto.request.RegisterCompleteRequest;
import com.winga.dto.request.RegisterRequest;
import com.winga.dto.request.UpdateProfileRequest;
import com.winga.dto.response.AuthResponse;
import com.winga.dto.response.UserResponse;
import com.winga.dto.response.VerifyOtpResponse;
import com.winga.exception.BusinessException;
import com.winga.entity.JobCategory;
import com.winga.repository.JobCategoryRepository;
import com.winga.repository.UserRepository;
import com.winga.repository.WalletRepository;
import com.winga.service.ReferralCodeService;
import com.winga.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final ReferralCodeService referralCodeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN || request.role() == Role.SUPER_ADMIN
                || request.role() == Role.MODERATOR || request.role() == Role.EMPLOYER_ADMIN) {
            throw new BusinessException("This role cannot be registered. It is assigned manually.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered: " + request.email());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank() && userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException("Phone number already registered.");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber() != null && !request.phoneNumber().isBlank() ? request.phoneNumber() : null)
                .role(request.role())
                .isVerified(false)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .currency(Currency.TZS)
                .build();
        walletRepository.save(wallet);

        log.info("New user registered: {} ({})", savedUser.getEmail(), savedUser.getRole());

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        return new AuthResponse(accessToken, refreshToken, toUserResponse(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().toLowerCase().trim(),
                        request.password()));

        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.info("User logged in: {}", user.getEmail());
        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + email));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new com.winga.exception.ResourceNotFoundException("User", id));
    }

    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        User entity = userRepository.findById(user.getId())
                .orElseThrow(() -> new com.winga.exception.ResourceNotFoundException("User", user.getId()));
        if (request.fullName() != null && !request.fullName().isBlank()) {
            entity.setFullName(request.fullName().trim());
        }
        if (request.phoneNumber() != null) {
            String phone = request.phoneNumber().isBlank() ? null : request.phoneNumber().trim();
            if (phone != null && !phone.equals(entity.getPhoneNumber())) {
                if (userRepository.existsByPhoneNumber(phone)) {
                    throw new BusinessException("Phone number already in use.");
                }
                entity.setPhoneNumber(phone);
            } else if (request.phoneNumber().isBlank()) {
                entity.setPhoneNumber(null);
            }
        }
        if (request.bio() != null) {
            entity.setBio(request.bio().length() > 1000 ? request.bio().substring(0, 1000) : request.bio());
        }
        if (request.skills() != null) {
            entity.setSkills(request.skills().length() > 100 ? request.skills().substring(0, 100) : request.skills());
        }
        if (request.profileImageUrl() != null) {
            entity.setProfileImageUrl(request.profileImageUrl().length() > 500 ? request.profileImageUrl().substring(0, 500) : request.profileImageUrl());
        }
        if (request.companyName() != null) {
            entity.setCompanyName(request.companyName().length() > 200 ? request.companyName().substring(0, 200) : request.companyName().trim());
        }
        if (request.telegram() != null) {
            entity.setTelegram(request.telegram().length() > 100 ? request.telegram().substring(0, 100) : request.telegram().trim());
        }
        if (request.country() != null) {
            entity.setCountry(request.country().length() > 100 ? request.country().substring(0, 100) : request.country().trim());
        }
        if (request.languages() != null) {
            entity.setLanguages(request.languages().length() > 2000 ? request.languages().substring(0, 2000) : request.languages());
        }
        if (request.cvUrl() != null) {
            entity.setCvUrl(request.cvUrl().length() > 500 ? request.cvUrl().substring(0, 500) : request.cvUrl());
        }
        if (request.workType() != null) {
            entity.setWorkType(request.workType().length() > 50 ? request.workType().substring(0, 50) : request.workType().trim());
        }
        if (request.timezone() != null) {
            entity.setTimezone(request.timezone().length() > 100 ? request.timezone().substring(0, 100) : request.timezone().trim());
        }
        if (request.paymentPreferences() != null) {
            entity.setPaymentPreferences(request.paymentPreferences().length() > 500 ? request.paymentPreferences().substring(0, 500) : request.paymentPreferences());
        }
        if (request.city() != null) {
            entity.setCity(request.city().length() > 100 ? request.city().substring(0, 100) : request.city().trim());
        }
        if (request.region() != null) {
            entity.setRegion(request.region().length() > 100 ? request.region().substring(0, 100) : request.region().trim());
        }
        if (request.latitude() != null) {
            entity.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            entity.setLongitude(request.longitude());
        }
        if (request.defaultCategoryId() != null) {
            entity.setDefaultCategoryId(request.defaultCategoryId());
        }
        userRepository.save(entity);
        log.debug("Profile updated for user {}", entity.getId());
        return toUserResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findWorkers(String keyword, Pageable pageable) {
        return userRepository.findWorkers(Role.FREELANCER, (keyword != null && !keyword.isBlank()) ? keyword.trim() : null, pageable)
                .map(this::toUserResponse);
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getProfileImageUrl(),
                user.getBio(),
                user.getSkills(),
                user.getIndustry(),
                user.getCompanyName(),
                user.getIsVerified(),
                user.getVerificationStatus(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getTelegram(),
                user.getCountry(),
                user.getLanguages(),
                user.getCvUrl(),
                user.getWorkType(),
                user.getTimezone(),
                user.getPaymentPreferences(),
                user.getCity(),
                user.getRegion(),
                user.getLatitude(),
                user.getLongitude(),
                user.getDefaultCategoryId());
    }

    // ─── OTP-based auth (register & login) ───────────────────────────────────────

    public void sendOtp(String email) {
        String normalized = email.toLowerCase().trim();
        otpService.sendOtp(normalized);
    }

    public VerifyOtpResponse verifyOtp(String email, String otp) {
        String normalized = email.toLowerCase().trim();
        otpService.consumeOtp(normalized, otp);

        var userOpt = userRepository.findByEmail(normalized);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            AuthResponse auth = new AuthResponse(accessToken, refreshToken, toUserResponse(user));
            log.info("User logged in via OTP: {}", user.getEmail());
            return VerifyOtpResponse.login(auth);
        }

        String registrationToken = jwtService.generateRegistrationToken(normalized);
        return VerifyOtpResponse.pendingRegistration(registrationToken);
    }

    @Transactional
    public AuthResponse completeRegistration(String registrationToken, RegisterCompleteRequest request) {
        String email;
        try {
            email = jwtService.extractEmailFromRegistrationToken(registrationToken);
        } catch (JwtException e) {
            throw new BusinessException("Invalid or expired registration token. Please verify your email again.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already registered. Please login.");
        }
        if (request.role() == Role.ADMIN) {
            throw new BusinessException("ADMIN role cannot be registered.");
        }

        String fullName = (request.fullName() != null && !request.fullName().isBlank())
                ? request.fullName().trim()
                : email.substring(0, Math.min(email.indexOf('@'), 50)); // default from email

        String industry = (request.industry() != null && !request.industry().isBlank()) ? request.industry().trim() : null;
        String companyName = (request.companyName() != null && !request.companyName().isBlank()) ? request.companyName().trim() : null;

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString())) // OTP-only; no password login
                .role(request.role())
                .industry(industry)
                .companyName(companyName)
                .isVerified(false)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.FREELANCER && industry != null) {
            java.util.List<JobCategory> categories = jobCategoryRepository.findAllByOrderBySortOrderAsc();
            String industryLower = industry.toLowerCase();
            JobCategory matched = categories.stream()
                    .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(industryLower))
                    .findFirst()
                    .orElse(categories.stream()
                            .filter(c -> c.getSlug() != null && c.getSlug().toLowerCase().contains(industryLower))
                            .findFirst()
                            .orElse(categories.isEmpty() ? null : categories.get(0)));
            if (matched != null) {
                savedUser.setDefaultCategoryId(matched.getId());
                userRepository.save(savedUser);
            }
        }

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .currency(Currency.TZS)
                .build();
        walletRepository.save(wallet);

        if (request.referralCode() != null && !request.referralCode().isBlank()) {
            referralCodeService.recordSignup(request.referralCode().trim());
        }

        log.info("Registration completed via OTP: {} ({})", savedUser.getEmail(), savedUser.getRole());

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        return new AuthResponse(accessToken, refreshToken, toUserResponse(savedUser));
    }
}
