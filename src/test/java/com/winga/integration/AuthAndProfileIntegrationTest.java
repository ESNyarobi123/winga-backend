package com.winga.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winga.domain.enums.Currency;
import com.winga.domain.enums.Role;
import com.winga.entity.User;
import com.winga.entity.Wallet;
import com.winga.repository.UserRepository;
import com.winga.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests: auth login, admin list users, workers list (public), profile update validation, upload auth.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndProfileIntegrationTest {

    static final String ADMIN_EMAIL = "admin@test.winga.co.tz";
    static final String FREELANCER_EMAIL = "freelancer@test.winga.co.tz";
    static final String PASSWORD = "Test123!";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired WalletRepository walletRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User admin = User.builder()
                .fullName("Test Admin")
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        admin = userRepository.save(admin);
        walletRepository.save(Wallet.builder().user(admin).currency(Currency.TZS).balance(BigDecimal.ZERO).build());

        User freelancer = User.builder()
                .fullName("Test Freelancer")
                .email(FREELANCER_EMAIL)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(Role.FREELANCER)
                .isActive(true)
                .build();
        freelancer = userRepository.save(freelancer);
        walletRepository.save(Wallet.builder().user(freelancer).currency(Currency.TZS).balance(BigDecimal.ZERO).build());
    }

    @Test
    void loginReturnsTokenAndUser() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", ADMIN_EMAIL,
                "password", PASSWORD
        ));
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.user.email").value(ADMIN_EMAIL));
    }

    @Test
    void adminListUsersRequiresAuth() throws Exception {
        String token = obtainAccessToken(ADMIN_EMAIL, PASSWORD);
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void workersListIsPublic() throws Exception {
        mockMvc.perform(get("/api/workers").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void profileUpdateValidationForFreelancerMissingRequired() throws Exception {
        String token = obtainAccessToken(FREELANCER_EMAIL, PASSWORD);
        // Only headline – missing country, languages, payment, workType, timezone
        String body = objectMapper.writeValueAsString(Map.of("headline", "Only headline"));
        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    if (!responseBody.contains("Profile incomplete")) {
                        throw new AssertionError("Expected response to contain 'Profile incomplete', got: " + responseBody);
                    }
                });
    }

    @Test
    void uploadRequiresAuth() throws Exception {
        mockMvc.perform(multipart("/api/upload")
                        .file("file", "dummy".getBytes())
                        .param("type", "profile"))
                .andExpect(status().isUnauthorized());
    }

    private String obtainAccessToken(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("data").get("accessToken").asText();
    }
}
