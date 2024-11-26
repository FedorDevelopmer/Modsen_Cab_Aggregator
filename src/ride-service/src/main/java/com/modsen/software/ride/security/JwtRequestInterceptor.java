package com.modsen.software.ride.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtRequestInterceptor implements RequestInterceptor {

    @Value("${jwt.auth.communication.username}")
    private String username;

    @Value("${jwt.auth.communication.password}")
    private String password;

    @Override
    public void apply(RequestTemplate template) {
        try {
            String token = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                Jwt jwt = (Jwt) jwtAuth.getPrincipal();
                token = jwt.getTokenValue();
            }
            if (token != null) {
                template.header("Authorization", "Bearer " + token);
                log.info("Bearer token attached to request: {}", token.substring(0, 10));
            }
        } catch (Exception e) {
            log.warn("Exception occurred during obtaining of JWT token : {}", e.getMessage());
        }
    }
}
