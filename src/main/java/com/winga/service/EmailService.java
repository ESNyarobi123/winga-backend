package com.winga.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends emails via SMTP (winga@ericksky.online / mail.ericksky.online).
 * Used for OTP and other transactional emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:winga@ericksky.online}")
    private String fromEmail;

    @Value("${app.platform.name:Winga}")
    private String appName;

    /**
     * Send OTP code to the given email address.
     */
    public void sendOtpEmail(String toEmail, String otpCode, int expiryMinutes) {
        String subject = appName + " — Your verification code";
        String body = buildOtpEmailBody(otpCode, expiryMinutes);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            try {
                helper.setFrom(fromEmail, appName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(toEmail.trim().toLowerCase());
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new com.winga.exception.BusinessException(
                "Unable to send verification email. Please try again or check your email address.");
        }
    }

    private String buildOtpEmailBody(String code, int expiryMinutes) {
        return """
            <div style="font-family: sans-serif; max-width: 400px; margin: 0 auto;">
              <h2 style="color: #006E42;">%s</h2>
              <p>Your verification code is:</p>
              <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px; color: #111;">%s</p>
              <p style="color: #666;">This code expires in %d minutes. Do not share it with anyone.</p>
              <p style="color: #999; font-size: 12px;">If you did not request this code, you can ignore this email.</p>
            </div>
            """.formatted(appName, code, expiryMinutes);
    }
}
