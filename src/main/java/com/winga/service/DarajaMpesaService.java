package com.winga.service;

import com.winga.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * M-Pesa Daraja API: OAuth token + STK push (Lipa na M-Pesa Online).
 * When app.mpesa.enabled=false or credentials missing, STK push is skipped (caller gets simulated ID).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DarajaMpesaService {

    private final RestTemplate restTemplate;

    @Value("${app.mpesa.enabled:false}")
    private boolean enabled;

    @Value("${app.mpesa.base-url:}")
    private String baseUrl;

    @Value("${app.mpesa.consumer-key:}")
    private String consumerKey;

    @Value("${app.mpesa.consumer-secret:}")
    private String consumerSecret;

    @Value("${app.mpesa.shortcode:}")
    private String shortcode;

    @Value("${app.mpesa.passkey:}")
    private String passkey;

    @Value("${app.mpesa.callback-url:}")
    private String callbackUrl;

    public boolean isEnabled() {
        return enabled && baseUrl != null && !baseUrl.isBlank()
                && consumerKey != null && !consumerKey.isBlank()
                && consumerSecret != null && !consumerSecret.isBlank()
                && shortcode != null && !shortcode.isBlank()
                && passkey != null && !passkey.isBlank();
    }

    /**
     * Initiate STK push. Returns checkout request ID or null if Daraja not configured.
     */
    public String stkPush(String phoneNumber, String amount, String accountReference) {
        if (!isEnabled()) {
            return null;
        }
        try {
            String token = getAccessToken();
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String password = Base64.getEncoder().encodeToString(
                    (shortcode + passkey + timestamp).getBytes(StandardCharsets.UTF_8));

            String url = baseUrl.replaceAll("/$", "") + "/mpesa/stkpush/v1/processrequest";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> body = new HashMap<>();
            body.put("BusinessShortCode", shortcode);
            body.put("Password", password);
            body.put("Timestamp", timestamp);
            body.put("TransactionType", "CustomerPayBillOnline");
            body.put("Amount", amount);
            body.put("PartyA", normalizePhone(phoneNumber));
            body.put("PartyB", shortcode);
            body.put("PhoneNumber", normalizePhone(phoneNumber));
            body.put("CallBackURL", callbackUrl);
            body.put("AccountReference", accountReference != null ? accountReference : "Winga");
            body.put("TransactionDesc", "Winga wallet deposit");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<?, ?> resBody = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && resBody != null) {
                Object checkoutId = resBody.get("CheckoutRequestID");
                return checkoutId != null ? checkoutId.toString() : null;
            }
            log.warn("Daraja STK push non-2xx: {}", response.getStatusCode());
            return null;
        } catch (Exception e) {
            log.error("Daraja STK push failed: {}", e.getMessage());
            throw new BusinessException("M-Pesa request failed: " + e.getMessage());
        }
    }

    private String getAccessToken() {
        String auth = Base64.getEncoder().encodeToString(
                (consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + auth);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = baseUrl.replaceAll("/$", "") + "/oauth/v1/generate?grant_type=client_credentials";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<?, ?> resBody = response.getBody();
        if (resBody != null && resBody.get("access_token") != null) {
            return resBody.get("access_token").toString();
        }
        throw new BusinessException("Failed to get M-Pesa access token");
    }

    /** Normalize to 254... (KE) or 255... (TZ) format without + */
    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String p = phone.replaceAll("\\s", "").replace("+", "");
        if (p.startsWith("0")) p = "255" + p.substring(1);
        else if (p.length() == 9 && !p.startsWith("255")) p = "255" + p;
        else if (p.length() == 9 && !p.startsWith("254")) p = "255" + p;
        return p;
    }
}
