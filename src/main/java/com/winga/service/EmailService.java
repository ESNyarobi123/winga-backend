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

    /**
     * Send job moderation result (approved or rejected) to the job owner.
     */
    public void sendJobModerationEmail(String toEmail, String jobTitle, long jobId, boolean approved) {
        String subject = approved ? appName + " — Job approved" : appName + " — Job not approved";
        String body = buildJobModerationBody(jobTitle, jobId, approved);
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
            log.info("Job moderation email sent to {} (approved={})", toEmail, approved);
        } catch (MessagingException e) {
            log.error("Failed to send job moderation email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildJobModerationBody(String jobTitle, long jobId, boolean approved) {
        if (approved) {
            return """
                <div style="font-family: sans-serif; max-width: 500px; margin: 0 auto;">
                  <h2 style="color: #006E42;">%s</h2>
                  <p>Your job <strong>\"%s\"</strong> has been approved and is now visible on the board.</p>
                  <p>Job ID: %d. You can now receive proposals from freelancers.</p>
                  <p style="color: #666;">Thank you for using %s.</p>
                </div>
                """.formatted(appName, escapeHtml(jobTitle), jobId, appName);
        } else {
            return """
                <div style="font-family: sans-serif; max-width: 500px; margin: 0 auto;">
                  <h2 style="color: #666;">%s</h2>
                  <p>Your job <strong>\"%s\"</strong> was not approved.</p>
                  <p>Please check our guidelines or contact support for more information. Job ID: %d.</p>
                  <p style="color: #666;">Thank you for using %s.</p>
                </div>
                """.formatted(appName, escapeHtml(jobTitle), jobId, appName);
        }
    }

    /** Send email when freelancer is hired (contract created). */
    public void sendHiredEmail(String toEmail, String jobTitle, long contractId) {
        String subject = appName + " — You've been hired!";
        String body = """
            <div style="font-family: sans-serif; max-width: 500px; margin: 0 auto;">
              <h2 style="color: #006E42;">%s</h2>
              <p>Congratulations! You have been hired for <strong>\"%s\"</strong>.</p>
              <p>Funds are secured in escrow. Contract ID: %d.</p>
              <p style="color: #666;">Thank you for using %s.</p>
            </div>
            """.formatted(appName, escapeHtml(jobTitle), contractId, appName);
        sendHtml(toEmail, subject, body);
        log.info("Hired email sent to {}", toEmail);
    }

    /** Send email when freelancer submits work for client review. */
    public void sendWorkSubmittedEmail(String toEmail, String freelancerName, long contractId) {
        String subject = appName + " — Work submitted for review";
        String body = """
            <div style="font-family: sans-serif; max-width: 500px; margin: 0 auto;">
              <h2 style="color: #006E42;">%s</h2>
              <p><strong>%s</strong> has submitted work for your review.</p>
              <p>Contract ID: %d. Please review and approve or request changes.</p>
              <p style="color: #666;">Thank you for using %s.</p>
            </div>
            """.formatted(appName, escapeHtml(freelancerName), contractId, appName);
        sendHtml(toEmail, subject, body);
        log.info("Work submitted email sent to {}", toEmail);
    }

    /** Send email when payment is released to freelancer. */
    public void sendPaymentReleasedEmail(String toEmail, String amount, long contractId) {
        String subject = appName + " — Payment released";
        String body = """
            <div style="font-family: sans-serif; max-width: 500px; margin: 0 auto;">
              <h2 style="color: #006E42;">%s</h2>
              <p><strong>TZS %s</strong> has been released to your wallet.</p>
              <p>Contract ID: %d.</p>
              <p style="color: #666;">Thank you for using %s.</p>
            </div>
            """.formatted(appName, escapeHtml(amount), contractId, appName);
        sendHtml(toEmail, subject, body);
        log.info("Payment released email sent to {}", toEmail);
    }

    /** Send email when a dispute is raised on a contract (to the other party). */
    public void sendDisputeOpenedEmail(String toEmail, String jobTitle, long contractId) {
        String subject = appName + " — Dispute raised";
        String body = """
            <div style="font-family: sans-serif; max-width: 500px; margin: 0 auto;">
              <h2 style="color: #666;">%s</h2>
              <p>A dispute has been raised on the contract for <strong>\"%s\"</strong>.</p>
              <p>Contract ID: %d. Our team will review and contact you.</p>
              <p style="color: #666;">Thank you for using %s.</p>
            </div>
            """.formatted(appName, escapeHtml(jobTitle), contractId, appName);
        sendHtml(toEmail, subject, body);
        log.info("Dispute opened email sent to {}", toEmail);
    }

    /** Send email when a dispute is resolved (to both parties). */
    public void sendDisputeResolvedEmail(String toEmail, String jobTitle, long contractId, String outcome) {
        String subject = appName + " — Dispute resolved";
        String body = """
            <div style="font-family: sans-serif; max-width: 500px; margin: 0 auto;">
              <h2 style="color: #006E42;">%s</h2>
              <p>The dispute for <strong>\"%s\"</strong> has been resolved.</p>
              <p>%s</p>
              <p>Contract ID: %d.</p>
              <p style="color: #666;">Thank you for using %s.</p>
            </div>
            """.formatted(appName, escapeHtml(jobTitle), escapeHtml(outcome), contractId, appName);
        sendHtml(toEmail, subject, body);
        log.info("Dispute resolved email sent to {}", toEmail);
    }

    /** Send email when client receives a new proposal. */
    public void sendProposalReceivedEmail(String toEmail, String freelancerName, long jobId) {
        String subject = appName + " — New proposal received";
        String body = """
            <div style="font-family: sans-serif; max-width: 500px; margin: 0 auto;">
              <h2 style="color: #006E42;">%s</h2>
              <p><strong>%s</strong> has applied to your job.</p>
              <p>Job ID: %d. View the proposal in your dashboard.</p>
              <p style="color: #666;">Thank you for using %s.</p>
            </div>
            """.formatted(appName, escapeHtml(freelancerName), jobId, appName);
        sendHtml(toEmail, subject, body);
        log.info("Proposal received email sent to {}", toEmail);
    }

    private void sendHtml(String toEmail, String subject, String body) {
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
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
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
