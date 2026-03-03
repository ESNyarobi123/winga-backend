package com.winga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Production checks: require JWT_SECRET when running with profile=prod.
 */
@Configuration
@Profile("prod")
public class ProdConfig {

    @Bean
    public ApplicationRunner jwtSecretCheck(@Value("${app.jwt.secret:}") String secret) {
        return args -> {
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException(
                    "Production requires JWT_SECRET. Set the environment variable (min 32 characters).");
            }
            if (secret.length() < 32) {
                throw new IllegalStateException("JWT_SECRET must be at least 32 characters in production.");
            }
        };
    }
}
