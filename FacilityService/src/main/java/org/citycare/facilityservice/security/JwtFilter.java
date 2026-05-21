package org.citycare.facilityservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j // Add this if you want to see logs
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtProvider.getClaims(token);

                if (!jwtProvider.isTokenExpired(claims)) {

                    String role = claims.get("role", String.class);
                    Object userIdObj = claims.get("userId");

                    // Safety check: only authenticate if a role exists
                    if (role != null && userIdObj != null) {
                        String authority = "ROLE_" + role.toUpperCase();
                        String userId = String.valueOf(userIdObj);

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                        // log.info("Authenticated userId: {} with role: {}", userId, authority);
                    }
                }
            } catch (Exception e) {
                // Log the error so you know why the token failed
                // log.error("JWT Authentication failed: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}