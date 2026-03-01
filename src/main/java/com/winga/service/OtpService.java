package com.winga.service;

import com.winga.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OTP store for email verification (register & login).
 * Sends OTP via EmailService (winga@ericksky.online SMTP). Use Redis in production for store.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailService emailService;

    @Value("${app.otp.expiry-minutes:10}")
    private int expiryMinutes;

    /** Key: email (normalized), Value: { code, expiresAt } */
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    public void sendOtp(String email) {
        String normalized = email.toLowerCase().trim();
        String code = generateCode();
        Instant expiresAt = Instant.now().plusSeconds(expiryMinutes * 60L);
        store.put(normalized, new OtpEntry(code, expiresAt));
        emailService.sendOtpEmail(normalized, code, expiryMinutes);
    }

    public boolean verifyOtp(String email, String otp) {
        String normalized = email.toLowerCase().trim();
        OtpEntry entry = store.get(normalized);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            store.remove(normalized);
            return false;
        }
        if (!entry.code.equals(otp.trim())) {
            return false;
        }
        store.remove(normalized);
        return true;
    }

    public void consumeOtp(String email, String otp) {
        if (!verifyOtp(email, otp)) {
            throw new BusinessException("Invalid or expired OTP. Request a new code.");
        }
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int value = RANDOM.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", value);
    }

    private record OtpEntry(String code, Instant expiresAt) {}
}
