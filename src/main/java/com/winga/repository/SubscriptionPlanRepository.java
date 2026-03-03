package com.winga.repository;

import com.winga.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    List<SubscriptionPlan> findAllByOrderBySortOrderAsc();

    List<SubscriptionPlan> findByIsActiveTrueOrderBySortOrderAsc();

    Optional<SubscriptionPlan> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
