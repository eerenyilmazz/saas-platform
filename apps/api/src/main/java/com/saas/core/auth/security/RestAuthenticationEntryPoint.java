package com.saas.core.auth.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.core.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security rejects unauthenticated requests before they ever reach a controller, so
 * GlobalExceptionHandler never sees them - without this, those responses would be Spring's
 * default error body instead of api-conventions.md's envelope.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = ErrorResponse.of("AUTH_REQUIRED", "Bu işlem için giriş yapmanız gerekiyor.", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
