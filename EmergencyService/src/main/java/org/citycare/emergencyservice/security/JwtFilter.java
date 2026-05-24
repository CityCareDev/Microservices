package org.citycare.emergencyservice.security;

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
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)       
            throws ServletException, IOException {

        String authHeader = request.getHeader(\"Authorization\");

        if (authHeader != null && authHeader.startsWith(\"Bearer \")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtProvider.getClaims(token);

                if (!jwtProvider.isTokenExpired(claims)) {

                    String role = claims.get(\"role\", String.class);
                    Object userIdObj = claims.get(\"userId\");

                    if (role != null && userIdObj != null) {
                        String userId = String.valueOf(userIdObj);
                        
                        // Use consistent role naming
                        String roleName = role.toUpperCase().trim();
                        if (!roleName.startsWith(\"ROLE_\")) {
                            roleName = \"ROLE_\" + roleName;
                        }

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(roleName))
                        );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info(\"Authenticated User ID: {} with role: {}\", userId, roleName);
                    }
                }
            } catch (Exception e) {
                log.error(\"JWT Authentication failed: {}\", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
