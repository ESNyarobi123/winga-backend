package com.winga.repository;

import com.winga.entity.PaymentGatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentGatewayConfigRepository extends JpaRepository<PaymentGatewayConfig, Long> {

    List<PaymentGatewayConfig> findAllByOrderByDisplayNameAsc();

    Optional<PaymentGatewayConfig> findByGatewaySlug(String gatewaySlug);

    boolean existsByGatewaySlug(String gatewaySlug);
}
