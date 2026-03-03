package com.winga.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Admin-managed payment gateway config (API keys, shortcode, callback URL, etc.).
 * e.g. M-Pesa: consumerKey, consumerSecret, shortcode, passkey, callbackUrl.
 */
@Entity
@Table(name = "payment_gateway_config", indexes = {
        @Index(name = "idx_payment_gateway_slug", columnList = "gateway_slug", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGatewayConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gateway_slug", nullable = false, unique = true, length = 50)
    private String gatewaySlug;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** JSON object: e.g. {"consumerKey":"...","consumerSecret":"...","shortcode":"...","passkey":"...","callbackUrl":"..."} */
    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
