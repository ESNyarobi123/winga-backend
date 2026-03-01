package com.winga.entity;

import com.winga.domain.enums.Role;
import com.winga.domain.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_phone", columnList = "phoneNumber"),
        @Index(name = "idx_user_created", columnList = "createdAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(length = 1000)
    private String bio;

    @Column(length = 100)
    private String skills;

    /** Role-specific: e.g. "Technology", "Healthcare" (Employer/Seeker). */
    @Column(length = 100)
    private String industry;

    /** Role-specific: company name (Employer). */
    @Column(length = 200)
    private String companyName;

    /** Job seeker: Telegram handle e.g. @username */
    @Column(length = 100)
    private String telegram;

    /** Job seeker: country e.g. Tanzania */
    @Column(length = 100)
    private String country;

    /** Location: city e.g. Dar es Salaam */
    @Column(length = 100)
    private String city;

    /** Location: region / mkoa e.g. Dar es Salaam */
    @Column(length = 100)
    private String region;

    /** Geolocation: latitude (from browser/app) */
    @Column(precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    /** Geolocation: longitude (from browser/app) */
    @Column(precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    /** Freelancer: default job category ID (auto-assigned from industry/skills on registration) */
    @Column(name = "default_category_id")
    private Long defaultCategoryId;

    /** Job seeker: JSON array of languages e.g. ["English","Swahili"] */
    @Column(columnDefinition = "TEXT")
    private String languages;

    /** Job seeker: CV file URL (from upload) */
    @Column(length = 500)
    private String cvUrl;

    /** Job seeker: work type e.g. Full-time, Part-time */
    @Column(length = 50)
    private String workType;

    /** Job seeker: timezone e.g. EAT (UTC+3) */
    @Column(length = 100)
    private String timezone;

    /** Job seeker: JSON array of payment methods e.g. ["Bank Transfer","PayPal"] */
    @Column(length = 500)
    private String paymentPreferences;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Builder.Default
    @Column(nullable = false)
    private Integer profileCompleteness = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
