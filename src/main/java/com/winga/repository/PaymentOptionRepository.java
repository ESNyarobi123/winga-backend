package com.winga.repository;

import com.winga.entity.PaymentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentOptionRepository extends JpaRepository<PaymentOption, Long> {

    boolean existsBySlug(String slug);

    List<PaymentOption> findAllByOrderBySortOrderAsc();
}
