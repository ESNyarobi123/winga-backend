package com.winga.config;

import com.winga.entity.User;
import com.winga.entity.Wallet;
import com.winga.domain.enums.Currency;
import com.winga.domain.enums.Role;
import com.winga.repository.UserRepository;
import com.winga.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * Seeds the database with initial test data on first startup.
 * Only runs in 'dev' and 'test' profiles.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile({ "dev", "default" })
    public CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already seeded. Skipping.");
                return;
            }

            log.info("🌱 Seeding initial Winga data...");

            // Admin
            createUser("Winga Admin", "admin@winga.co.tz", "Admin@1234", "+255712000001", Role.ADMIN,
                    new BigDecimal("0"));

            // Clients
            User client1 = createUser("Amina Hassan", "amina@client.tz", "Client@1234", "+255712000002",
                    Role.CLIENT, new BigDecimal("2000000"));
            createUser("John Mwaura", "john@client.tz", "Client@1234", "+255712000003",
                    Role.CLIENT, new BigDecimal("5000000"));

            // Freelancers
            createUser("David Ochieng", "david@freelancer.tz", "Freelancer@1234", "+255712000004",
                    Role.FREELANCER, new BigDecimal("0"));
            createUser("Fatuma Said", "fatuma@freelancer.tz", "Freelancer@1234", "+255712000005",
                    Role.FREELANCER, new BigDecimal("150000"));

            log.info("✅ Seed data created successfully!");
            log.info("📧 Admin login: admin@winga.co.tz / Admin@1234");
            log.info("📧 Client login: amina@client.tz / Client@1234 (Balance: TZS 2,000,000)");
            log.info("📧 Freelancer login: david@freelancer.tz / Freelancer@1234");
        };
    }

    private User createUser(String name, String email, String password, String phone,
            Role role, BigDecimal walletBalance) {
        User user = User.builder()
                .fullName(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .phoneNumber(phone)
                .role(role)
                .isVerified(true)
                .isActive(true)
                .build();
        User saved = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(saved)
                .balance(walletBalance)
                .currency(Currency.TZS)
                .build();
        walletRepository.save(wallet);

        return saved;
    }
}
