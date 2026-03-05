package com.winga.service;

/**
 * Sends OTP to a phone number via WhatsApp (e.g. Winga-otp service using Baileys).
 * If the service is not configured or fails, login still works via email OTP.
 */
public interface WhatsappOtpSender {

    /**
     * Send the same OTP code to the user's WhatsApp number.
     * Implementation may be no-op when WhatsApp service URL is not set.
     *
     * @param phoneNumber phone in any format (0XXXXXXXX, +255XXXXXXXX, 255XXXXXXXX)
     * @param otpCode     6-digit OTP code
     * @param expiryMinutes validity in minutes (for message text)
     */
    void sendOtpToWhatsApp(String phoneNumber, String otpCode, int expiryMinutes);
}
