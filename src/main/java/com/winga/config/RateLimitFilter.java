package com.winga.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limit by client IP for sensitive endpoints: login, forgot-password, upload.
 * Returns 429 Too Many Requests when limit exceeded.
 */
@Component
@Order(1)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String PATH_LOGIN = "/api/auth/login";
    private static final String PATH_FORGOT = "/api/auth/forgot-password";
    private static final String PATH_UPLOAD = "/api/upload";

    @Value("${app.rate-limit.login-per-minute:15}")
    private int loginPerMinute;

    @Value("${app.rate-limit.forgot-per-minute:5}")
    private int forgotPerMinute;

    @Value("${app.rate-limit.upload-per-minute:30}")
    private int uploadPerMinute;

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> forgotBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> uploadBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith(request.getContextPath())) {
            path = path.substring(request.getContextPath().length());
        }

        String key = clientKey(request);
        Bucket bucket = null;
        if (PATH_LOGIN.equals(path)) {
            bucket = loginBuckets.computeIfAbsent(key, k -> buildBucket(loginPerMinute));
        } else if (PATH_FORGOT.equals(path)) {
            bucket = forgotBuckets.computeIfAbsent(key, k -> buildBucket(forgotPerMinute));
        } else if (PATH_UPLOAD.equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            bucket = uploadBuckets.computeIfAbsent(key, k -> buildBucket(uploadPerMinute));
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}");
            log.warn("Rate limit exceeded for {} on {}", key, path);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static Bucket buildBucket(int capacityPerMinute) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(capacityPerMinute).refillGreedy(capacityPerMinute, Duration.ofMinutes(1)))
                .build();
    }

    private static String clientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
