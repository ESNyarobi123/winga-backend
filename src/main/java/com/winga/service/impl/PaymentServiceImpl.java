package com.winga.service.impl;

import com.winga.dto.request.DepositRequest;
import com.winga.domain.enums.MobileMoneyProvider;
import com.winga.entity.User;
import com.winga.exception.BusinessException;
import com.winga.exception.ResourceNotFoundException;
import com.winga.repository.UserRepository;
import com.winga.service.DarajaMpesaService;
import com.winga.service.PaymentService;
import com.winga.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/** Implementation: M-Pesa callbacks & Escrow-related payment logic. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final WalletService walletService;
    private final UserRepository userRepository;
    private final DarajaMpesaService darajaMpesaService;

    @Override
    @Transactional
    public void handleMpesaCallback(Map<String, Object> callbackPayload) {
        log.info("M-Pesa callback received: {}", callbackPayload.keySet());

        // Daraja STK callback: Body.stkCallback.ResultCode, CallbackMetadata.Item (Amount, PhoneNumber)
        if (callbackPayload.containsKey("Body")) {
            handleDarajaStkCallback(callbackPayload);
            return;
        }

        // Simple format (e.g. simulation or other gateway): PhoneNumber, Amount at root
        Object phone = callbackPayload.get("PhoneNumber");
        Object amount = callbackPayload.get("Amount");
        if (phone == null || amount == null) {
            throw new BusinessException("Invalid callback: missing PhoneNumber or Amount");
        }
        creditWalletByPhone(phone.toString(), new BigDecimal(amount.toString()));
    }

    @SuppressWarnings("unchecked")
    private void handleDarajaStkCallback(Map<String, Object> payload) {
        Object body = payload.get("Body");
        if (!(body instanceof Map)) {
            throw new BusinessException("Invalid Daraja callback: Body not an object");
        }
        Map<String, Object> bodyMap = (Map<String, Object>) body;
        Object stkCallback = bodyMap.get("stkCallback");
        if (!(stkCallback instanceof Map)) {
            throw new BusinessException("Invalid Daraja callback: stkCallback missing");
        }
        Map<String, Object> stk = (Map<String, Object>) stkCallback;
        Object resultCodeObj = stk.get("ResultCode");
        int resultCode = resultCodeObj instanceof Number ? ((Number) resultCodeObj).intValue() : -1;
        if (resultCode != 0) {
            log.info("Daraja STK callback non-success: ResultCode={} Desc={}", resultCode, stk.get("ResultDesc"));
            return;
        }
        Object meta = stk.get("CallbackMetadata");
        if (!(meta instanceof Map)) {
            throw new BusinessException("Invalid Daraja callback: CallbackMetadata missing");
        }
        Object itemsObj = ((Map<String, Object>) meta).get("Item");
        if (!(itemsObj instanceof java.util.List)) {
            throw new BusinessException("Invalid Daraja callback: Item array missing");
        }
        java.util.List<?> items = (java.util.List<?>) itemsObj;
        BigDecimal amount = null;
        String phoneNumber = null;
        for (Object item : items) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> entry = (Map<String, Object>) item;
            String name = String.valueOf(entry.get("Name"));
            Object value = entry.get("Value");
            if ("Amount".equals(name) && value != null) {
                amount = value instanceof Number ? BigDecimal.valueOf(((Number) value).doubleValue()) : new BigDecimal(value.toString());
            } else if ("PhoneNumber".equals(name) && value != null) {
                phoneNumber = value.toString();
            }
        }
        if (amount == null || phoneNumber == null) {
            throw new BusinessException("Invalid Daraja callback: Amount or PhoneNumber missing in metadata");
        }
        creditWalletByPhone(phoneNumber, amount);
    }

    private void creditWalletByPhone(String phoneRaw, BigDecimal amount) {
        String normalized = normalizePhone(phoneRaw);
        User user = userRepository.findByPhoneNumber(normalized)
                .orElse(userRepository.findByPhoneNumber(phoneRaw)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found for phone: " + phoneRaw)));
        String phoneStr = user.getPhoneNumber() != null ? user.getPhoneNumber() : normalized;
        DepositRequest req = new DepositRequest(amount, phoneStr, MobileMoneyProvider.MPESA);
        walletService.simulateDeposit(user, req);
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String p = phone.replaceAll("\\s", "").replace("+", "");
        if (p.startsWith("0")) p = "255" + p.substring(1);
        else if (p.length() == 9) p = "255" + p;
        return p;
    }

    @Override
    public String initiateDeposit(User user, BigDecimal amount, String phoneNumber) {
        if (darajaMpesaService.isEnabled()) {
            String checkoutId = darajaMpesaService.stkPush(
                    phoneNumber,
                    amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString(),
                    "Winga-" + user.getId());
            if (checkoutId != null) {
                log.info("STK push initiated: user={} checkoutRequestId={}", user.getId(), checkoutId);
                return checkoutId;
            }
        }
        log.info("Initiate deposit (simulated): user={}, amount={}, phone={}", user.getId(), amount, phoneNumber);
        return "SIMULATED_REQUEST_ID";
    }
}
