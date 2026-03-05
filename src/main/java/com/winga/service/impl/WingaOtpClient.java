package com.winga.service.impl;

import com.winga.service.WhatsappOtpSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Sends OTP to WhatsApp by calling the Winga-otp service (Node.js + Baileys).
 * No-op when app.winga-otp.base-url is empty (OTP only via email).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WingaOtpClient implements WhatsappOtpSender {

    private final RestTemplate restTemplate;

    @Value("${app.winga-otp.base-url:}")
    private String baseUrl;

    @Value("${app.platform.name:Winga}")
    private String appName;

    @Override
    public void sendOtpToWhatsApp(String phoneNumber, String otpCode, int expiryMinutes) {
        if (baseUrl == null || baseUrl.isBlank()) return;
        if (phoneNumber == null || phoneNumber.isBlank()) return;
        String normalized = normalizePhoneForWhatsApp(phoneNumber);
        if (normalized.isBlank()) {
            log.warn("Invalid phone for WhatsApp OTP: {}", phoneNumber);
            return;
        }
        String url = baseUrl.endsWith("/") ? baseUrl + "send-otp" : baseUrl + "/send-otp";
        try {
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            var body = new java.util.LinkedHashMap<String, Object>();
            body.put("phone", normalized);
            body.put("code", otpCode);
            body.put("expiryMinutes", expiryMinutes);
            body.put("appName", appName);
            restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            log.info("WhatsApp OTP sent to {}", normalized);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp OTP to {}: {}", normalized, e.getMessage());
            // Do not throw — user can still use email OTP
        }
    }

    /** Normalize to WhatsApp format: 255712345678 (no +, with country code). */
    private String normalizePhoneForWhatsApp(String phone) {
        if (phone == null) return "";
        String p = phone.replaceAll("\\s", "").replace("+", "").trim();
        if (p.startsWith("0") && p.length() >= 9) {
            p = "255" + p.substring(1);
        } else if (!p.startsWith("255") && p.length() == 9 && p.matches("[67]\\d{8}")) {
            p = "255" + p;
        }
        return p.matches("255[67]\\d{8}") ? p : "";
    }
}
