package com.winga.service;

import com.winga.dto.response.ReferralResponse;
import com.winga.entity.ReferralCode;
import com.winga.entity.User;
import com.winga.exception.ResourceNotFoundException;
import com.winga.repository.ReferralCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralCodeService {

    private final ReferralCodeRepository referralCodeRepository;

    @Value("${app.platform.referral-signup-commission:500}")
    private BigDecimal signupCommission;

    @Value("${app.platform.referral-base-url:https://winga.co.tz}")
    private String referralBaseUrl;

    @Transactional(readOnly = true)
    public ReferralResponse getMyReferral(User user) {
        ReferralCode ref = getOrCreateForUser(user);
        String link = referralBaseUrl + "/register?ref=" + ref.getCode();
        return new ReferralResponse(
                ref.getCode(),
                link,
                ref.getSignupCount(),
                ref.getHireCount(),
                ref.getCommissionBalance());
    }

    @Transactional
    public ReferralCode getOrCreateForUser(User user) {
        return referralCodeRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    String code = "WINGA-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    ReferralCode ref = ReferralCode.builder()
                            .user(user)
                            .code(code)
                            .signupCount(0)
                            .hireCount(0)
                            .commissionBalance(BigDecimal.ZERO)
                            .build();
                    return referralCodeRepository.save(ref);
                });
    }

    @Transactional
    public void recordSignup(String code) {
        if (code == null || code.isBlank()) return;
        referralCodeRepository.findByCode(code.trim().toUpperCase()).ifPresent(ref -> {
            ref.setSignupCount(ref.getSignupCount() + 1);
            ref.setCommissionBalance(ref.getCommissionBalance().add(signupCommission));
            referralCodeRepository.save(ref);
            log.info("Referral signup: code={}, new signupCount={}", ref.getCode(), ref.getSignupCount());
        });
    }

    @Transactional
    public void recordHire(String code, BigDecimal commission) {
        if (code == null || code.isBlank() || commission == null || commission.compareTo(BigDecimal.ZERO) <= 0) return;
        referralCodeRepository.findByCode(code.trim().toUpperCase()).ifPresent(ref -> {
            ref.setHireCount(ref.getHireCount() + 1);
            ref.setCommissionBalance(ref.getCommissionBalance().add(commission));
            referralCodeRepository.save(ref);
            log.info("Referral hire: code={}, new hireCount={}", ref.getCode(), ref.getHireCount());
        });
    }
}
