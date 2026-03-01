package com.winga.service;

import com.winga.entity.Subscription;
import com.winga.entity.User;
import com.winga.domain.enums.Role;
import com.winga.exception.BusinessException;
import com.winga.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Value("${app.platform.subscription-days:30}")
    private int subscriptionDays = 30;

    /** Freelancer can only bid if they have an active subscription. */
    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId, LocalDateTime.now()).isPresent();
    }

    public Optional<Subscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId, LocalDateTime.now());
    }

    /** Grant a new subscription (e.g. after payment). */
    @Transactional
    public Subscription grantSubscription(User user, String planId) {
        if (user.getRole() != Role.FREELANCER) {
            throw new BusinessException("Only freelancers can have provider subscriptions.");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusDays(subscriptionDays);
        Subscription sub = Subscription.builder()
                .user(user)
                .planId(planId != null ? planId : "MONTHLY_PROVIDER")
                .status("ACTIVE")
                .startsAt(now)
                .endsAt(endsAt)
                .build();
        return subscriptionRepository.save(sub);
    }
}
