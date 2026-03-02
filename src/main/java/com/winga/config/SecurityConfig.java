package com.winga.config;

import com.winga.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    private static final String[] PUBLIC_WHITELIST = {
            "/",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/admin/login",
            "/api/auth/send-otp",
            "/api/auth/verify-otp",
            "/api/auth/register/complete",
            "/api/payments/callback",   // M-Pesa / gateway webhook
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/ws/**"   // WebSocket handshake
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight (must be allowed so browser gets CORS headers)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Admin dashboard login (explicit so 403 never happens when backend is up to date)
                        .requestMatchers(HttpMethod.POST, "/api/auth/admin/login").permitAll()
                        // Public
                        .requestMatchers(PUBLIC_WHITELIST).permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/categories", "/api/jobs/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/platform/config").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/portfolio/user/**", "/api/certifications/user/**").permitAll()
                        // Client restricted (post, update, delete jobs)
                        .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/jobs/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/**").hasRole("CLIENT")
                        .requestMatchers("/api/client/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/workers").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/jobs/my-jobs").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/contracts/hire/**").hasRole("CLIENT")
                        .requestMatchers("/api/contracts/*/approve").hasRole("CLIENT")
                        // Freelancer restricted
                        .requestMatchers(HttpMethod.POST, "/api/proposals/**").hasRole("FREELANCER")
                        .requestMatchers("/api/freelancer/**").hasRole("FREELANCER")
                        .requestMatchers("/api/contracts/*/submit").hasRole("FREELANCER")
                        // Admin / Super Admin only
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // Employer dashboard (Client / Employer Admin)
                        .requestMatchers("/api/employer/**").hasAnyRole("CLIENT", "EMPLOYER_ADMIN", "ADMIN", "SUPER_ADMIN")
                        // Everything else needs auth
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
