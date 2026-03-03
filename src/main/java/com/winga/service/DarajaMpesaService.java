package com.winga.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winga.entity.PaymentGatewayConfig;
import com.winga.exception.BusinessException;
import com.winga.repository.PaymentGatewayConfigRepository;
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
 * Credentials: from admin-configured PaymentGatewayConfig (slug=mpesa) if present and active,
 * otherwise from env (app.mpesa.*).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DarajaMpesaService {

    private static final String MPESA_SLUG = "mpesa";

    private final RestTemplate restTemplate;
    private final PaymentGatewayConfigRepository paymentGatewayConfigRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.mpesa.enabled:false}")
    private boolean enabledFromEnv;

    @Value("${app.mpesa.base-url:}")
    private String baseUrlEnv;

    @Value("${app.mpesa.consumer-key:}")
    private String consumerKeyEnv;

    @Value("${app.mpesa.consumer-secret:}")
    private String consumerSecretEnv;

    @Value("${app.mpesa.shortcode:}")
    private String shortcodeEnv;

    @Value("${app.mpesa.passkey:}")
    private String passkeyEnv;

    @Value("${app.mpesa.callback-url:}")
    private String callbackUrlEnv;

    /** Prefer DB config (admin-configured); fallback to env. */
    private MpesaCredentials getCredentials() {
        if (paymentGatewayConfigRepository != null) {
            var opt = paymentGatewayConfigRepository.findByGatewaySlug(MPESA_SLUG);
            if (opt.isPresent()) {
                PaymentGatewayConfig cfg = opt.get();
                if (Boolean.TRUE.equals(cfg.getIsActive()) && cfg.getConfigJson() != null && !cfg.getConfigJson().isBlank()) {
                    try {
                        Map<String, String> map = objectMapper.readValue(cfg.getConfigJson(), new TypeReference<Map<String, String>>() {});
                        String base = map.getOrDefault("baseUrl", "").trim();
                        String cKey = map.getOrDefault("consumerKey", "").trim();
                        String cSecret = map.getOrDefault("consumerSecret", "").trim();
                        String sc = map.getOrDefault("shortcode", "").trim();
                        String pk = map.getOrDefault("passkey", "").trim();
                        String cb = map.getOrDefault("callbackUrl", "").trim();
                        if (!base.isEmpty() && !cKey.isEmpty() && !cSecret.isEmpty() && !sc.isEmpty() && !pk.isEmpty() && !cb.isEmpty()) {
                            return new MpesaCredentials(base, cKey, cSecret, sc, pk, cb);
                        }
                    } catch (Exception e) {
                        log.warn("M-Pesa config from DB invalid: {}", e.getMessage());
                    }
                }
            }
        }
        return new MpesaCredentials(
                nullToEmpty(baseUrlEnv), nullToEmpty(consumerKeyEnv), nullToEmpty(consumerSecretEnv),
                nullToEmpty(shortcodeEnv), nullToEmpty(passkeyEnv), nullToEmpty(callbackUrlEnv));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public boolean isEnabled() {
        MpesaCredentials c = getCredentials();
        if (c.isComplete()) return true;
        return enabledFromEnv && baseUrlEnv != null && !baseUrlEnv.isBlank()
                && consumerKeyEnv != null && !consumerKeyEnv.isBlank()
                && consumerSecretEnv != null && !consumerSecretEnv.isBlank()
                && shortcodeEnv != null && !shortcodeEnv.isBlank()
                && passkeyEnv != null && !passkeyEnv.isBlank();
    }

    /**
     * Initiate STK push. Returns checkout request ID or null if Daraja not configured.
     * Uses admin-configured M-Pesa config from DB if present, else env.
     */
    public String stkPush(String phoneNumber, String amount, String accountReference) {
        MpesaCredentials cred = getCredentials();
        if (!cred.isComplete()) {
            return null;
        }
        try {
            String token = getAccessToken(cred);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String password = Base64.getEncoder().encodeToString(
                    (cred.shortcode + cred.passkey + timestamp).getBytes(StandardCharsets.UTF_8));

            String url = cred.baseUrl.replaceAll("/$", "") + "/mpesa/stkpush/v1/processrequest";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> body = new HashMap<>();
            body.put("BusinessShortCode", cred.shortcode);
            body.put("Password", password);
            body.put("Timestamp", timestamp);
            body.put("TransactionType", "CustomerPayBillOnline");
            body.put("Amount", amount);
            body.put("PartyA", normalizePhone(phoneNumber));
            body.put("PartyB", cred.shortcode);
            body.put("PhoneNumber", normalizePhone(phoneNumber));
            body.put("CallBackURL", cred.callbackUrl);
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

    private String getAccessToken(MpesaCredentials cred) {
        String auth = Base64.getEncoder().encodeToString(
                (cred.consumerKey + ":" + cred.consumerSecret).getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + auth);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = cred.baseUrl.replaceAll("/$", "") + "/oauth/v1/generate?grant_type=client_credentials";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<?, ?> resBody = response.getBody();
        if (resBody != null && resBody.get("access_token") != null) {
            return resBody.get("access_token").toString();
        }
        throw new BusinessException("Failed to get M-Pesa access token");
    }

    private record MpesaCredentials(String baseUrl, String consumerKey, String consumerSecret,
                                    String shortcode, String passkey, String callbackUrl) {
        boolean isComplete() {
            return baseUrl != null && !baseUrl.isBlank()
                    && consumerKey != null && !consumerKey.isBlank()
                    && consumerSecret != null && !consumerSecret.isBlank()
                    && shortcode != null && !shortcode.isBlank()
                    && passkey != null && !passkey.isBlank()
                    && callbackUrl != null && !callbackUrl.isBlank();
        }
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
