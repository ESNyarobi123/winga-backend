package com.winga;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "Winga Freelance Marketplace API",
                version = "v1.0.0",
                description = """
                        🇹🇿 Tanzania's premier freelance marketplace.
                        
                        Features:
                        • JWT-secured authentication
                        • Escrow-based payment engine
                        • Mobile Money (M-Pesa / Tigo Pesa) simulation
                        • Real-time WebSocket chat
                        • Milestone tracking
                        """,
                contact = @Contact(name = "Winga Dev Team", email = "dev@winga.co.tz"),
                license = @License(name = "Winga Proprietary License")
        )
)
public class WingaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WingaApplication.class, args);
    }
}
