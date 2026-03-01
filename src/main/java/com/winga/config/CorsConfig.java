package com.winga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CORS configuration — kuruhusu Next.js / frontend iongee na backend.
 */
@Configuration
public class CorsConfig {

    /** Extra origins from env (comma-separated) e.g. https://sokokuuonline.co.tz,https://admin.sokokuuonline.co.tz */
    @Value("${app.cors.allowed-origins-extra:}")
    private String allowedOriginsExtra;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> patterns = Stream.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://winga.co.tz"
        ).collect(Collectors.toList());
        if (StringUtils.hasText(allowedOriginsExtra)) {
            patterns.addAll(Arrays.stream(allowedOriginsExtra.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList());
        }
        config.setAllowedOriginPatterns(patterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
