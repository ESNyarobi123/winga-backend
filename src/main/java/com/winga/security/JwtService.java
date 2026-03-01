package com.winga.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.registration-token-expiration-ms:900000}")
    private long registrationTokenExpirationMs;

    private static final String CLAIM_PURPOSE = "purpose";
    private static final String PURPOSE_REGISTER = "register";

    // ─── Token Generation ────────────────────────────────────────────────────────

    /**
     * Access token includes "role" claim so Frontend can enforce RBAC without calling /me.
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        var authorities = userDetails.getAuthorities();
        if (authorities != null && !authorities.isEmpty()) {
            String role = authorities.iterator().next().getAuthority().replace("ROLE_", "");
            claims.put("role", role);
        }
        return generateToken(claims, userDetails, jwtExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateToken(claims, userDetails, refreshExpirationMs);
    }

    /** Short-lived token for completing registration after OTP (claim: email + purpose=register). */
    public String generateRegistrationToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_PURPOSE, PURPOSE_REGISTER);
        return Jwts.builder()
                .claims(claims)
                .subject(email.toLowerCase().trim())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + registrationTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Extract email from registration token; validates purpose=register and expiry. */
    public String extractEmailFromRegistrationToken(String token) {
        Claims claims = extractAllClaims(token);
        if (!PURPOSE_REGISTER.equals(claims.get(CLAIM_PURPOSE, String.class))) {
            throw new JwtException("Invalid registration token");
        }
        if (extractExpiration(token).before(new Date())) {
            throw new JwtException("Registration token expired");
        }
        return claims.getSubject();
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ─── Token Validation ────────────────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ─── Claims Extraction ───────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /** Extract role from JWT (for Frontend / middleware). */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
