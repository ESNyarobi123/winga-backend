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
    private final SubscriptionPlanService subscriptionPlanService;

    @Value("${app.platform.subscription-days:30}")
    private int subscriptionDays = 30;

    /** Freelancer can only bid if they have an active subscription. */
    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId, LocalDateTime.now()).isPresent();
    }

    public Optional<Subscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId, LocalDateTime.now());
    }

    /** Grant a new subscription (e.g. after payment). planId/slug can be plan slug (e.g. monthly_provider) or legacy id; duration from plan if found. */
    @Transactional
    public Subscription grantSubscription(User user, String planId) {
        if (user.getRole() != Role.FREELANCER) {
            throw new BusinessException("Only freelancers can have provider subscriptions.");
        }
        String slug = planId != null && !planId.isBlank() ? planId.trim() : "monthly_provider";
        int days = subscriptionPlanService.findBySlug(slug)
                .map(com.winga.entity.SubscriptionPlan::getDurationDays)
                .orElse(subscriptionDays);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusDays(days);
        Subscription sub = Subscription.builder()
                .user(user)
                .planId(slug)
                .status("ACTIVE")
                .startsAt(now)
                .endsAt(endsAt)
                .build();
        return subscriptionRepository.save(sub);
    }
}
