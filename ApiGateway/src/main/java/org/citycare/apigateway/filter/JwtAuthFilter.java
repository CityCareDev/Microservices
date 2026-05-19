package org.citycare.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtUtil jwtUtil;

    // Endpoints that do NOT require a JWT token
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/auth/register",
            "/auth/login",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator"
    );

    public JwtAuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Internal endpoints are not exposed through the gateway.
            if (path.contains("/internal/")) {
                return forbidden(exchange, "Internal endpoint is not accessible via gateway");
            }

            // Skip JWT check for open endpoints
            if (isOpenEndpoint(path)) {
                return chain.filter(exchange);
            }

            // Require Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return unauthorized(exchange, "Missing Authorization header");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Invalid Authorization header format");
            }

            String token = authHeader.substring(7);
            try {
                if (!jwtUtil.validateToken(token)) {
                    return unauthorized(exchange, "Invalid or expired JWT token");
                }

                return chain.filter(exchange);

            } catch (Exception e) {
                log.error("JWT validation error: {}", e.getMessage());
                return unauthorized(exchange, "JWT validation failed");
            }
        };
    }

    private boolean isOpenEndpoint(String path) {
        return OPEN_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.warn("Unauthorized request: {}", message);
        return response.setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        log.warn("Forbidden request: {}", message);
        return response.setComplete();
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
