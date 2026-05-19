package org.citycare.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.citycare.authservice.entity.User;
import org.citycare.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Centralized JWT service for AuthService.
 * Handles token generation, parsing, and validation without external shared libraries.
 */
@Service
@RequiredArgsConstructor
public class JWTService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.ms:1800000}")
    private long expirationMs;

    private final UserRepository userRepository;

    /**
     * Generate a JWT token for an authenticated user.
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole().name());
        return buildToken(claims, user.getEmail(), expirationMs);
    }

    /**
     * Generate a service-to-service token.
     */
    public String generateServiceToken(String serviceName, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 0L);
        claims.put("role", "ROLE_ADMIN");
        claims.put("serviceId", serviceName);
        return buildToken(claims, serviceName, expirationMs);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUserName(token).equals(userDetails.getUsername())
                && !extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
