package com.winga.service;

import com.winga.dto.request.SubscriptionPlanRequest;
import com.winga.dto.response.SubscriptionPlanResponse;
import com.winga.entity.SubscriptionPlan;
import com.winga.exception.BusinessException;
import com.winga.exception.ResourceNotFoundException;
import com.winga.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    /** All plans (admin). */
    public List<SubscriptionPlanResponse> listAll() {
        return subscriptionPlanRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    /** Active plans only (public, for freelancer pricing page). */
    public List<SubscriptionPlanResponse> listActive() {
        return subscriptionPlanRepository.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public SubscriptionPlanResponse getById(Long id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", id));
        return toResponse(plan);
    }

    /** Look up plan by slug (e.g. for grantSubscription). */
    public java.util.Optional<SubscriptionPlan> findBySlug(String slug) {
        return subscriptionPlanRepository.findBySlug(slug);
    }

    @Transactional
    public SubscriptionPlanResponse create(SubscriptionPlanRequest request) {
        String slug = normalizeSlug(request.slug());
        if (subscriptionPlanRepository.existsBySlug(slug)) {
            throw new BusinessException("A subscription plan with slug '" + slug + "' already exists.");
        }
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.name().trim())
                .slug(slug)
                .description(request.description() != null ? request.description().trim() : null)
                .price(request.price())
                .currency(request.currency())
                .durationDays(request.durationDays() != null ? request.durationDays() : 30)
                .isActive(request.isActive())
                .sortOrder(request.sortOrder())
                .build();
        plan = subscriptionPlanRepository.save(plan);
        return toResponse(plan);
    }

    @Transactional
    public SubscriptionPlanResponse update(Long id, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", id));
        String slug = normalizeSlug(request.slug());
        if (subscriptionPlanRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BusinessException("A subscription plan with slug '" + slug + "' already exists.");
        }
        plan.setName(request.name().trim());
        plan.setSlug(slug);
        plan.setDescription(request.description() != null ? request.description().trim() : null);
        plan.setPrice(request.price());
        plan.setCurrency(request.currency());
        plan.setDurationDays(request.durationDays() != null ? request.durationDays() : 30);
        plan.setIsActive(request.isActive());
        plan.setSortOrder(request.sortOrder());
        plan = subscriptionPlanRepository.save(plan);
        return toResponse(plan);
    }

    @Transactional
    public void delete(Long id) {
        if (!subscriptionPlanRepository.existsById(id)) {
            throw new ResourceNotFoundException("SubscriptionPlan", id);
        }
        subscriptionPlanRepository.deleteById(id);
    }

    private static String normalizeSlug(String slug) {
        return slug == null ? "" : slug.trim().toLowerCase().replaceAll("\\s+", "_");
    }

    private SubscriptionPlanResponse toResponse(SubscriptionPlan p) {
        return new SubscriptionPlanResponse(
                p.getId(), p.getName(), p.getSlug(), p.getDescription(),
                p.getPrice(), p.getCurrency(), p.getDurationDays(),
                p.getIsActive(), p.getSortOrder(), p.getCreatedAt());
    }
}
