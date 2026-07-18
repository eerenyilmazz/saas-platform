package com.saas.core.auth.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.core.auth.service.JwtService;
import com.saas.core.exception.ErrorResponse;
import com.saas.core.exception.UnauthorizedException;
import com.saas.core.tenant.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Populates both Spring Security's SecurityContext (for @PreAuthorize/role checks) and
 * TenantContext (read by TenantAwareDataSource on every connection checkout, so RLS sees the
 * right tenant_id for this request) from the caller's JWT. TenantContext.clear() in finally is
 * required, not optional: the servlet container reuses request-handling threads, so a value left
 * behind here would apply to whatever unrelated request that thread picks up next.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(AUTHORIZATION_HEADER);
            if (header != null && header.startsWith(BEARER_PREFIX)) {
                try {
                    authenticate(header.substring(BEARER_PREFIX.length()));
                } catch (UnauthorizedException exception) {
                    // Thrown by JwtService while parsing the token, inside a servlet filter - it
                    // would never reach GlobalExceptionHandler (a @RestControllerAdvice, which
                    // only sees exceptions from a dispatched controller), so it's written out
                    // directly here instead of being left to propagate as a bare 500.
                    writeUnauthorized(response, exception);
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private void writeUnauthorized(HttpServletResponse response, UnauthorizedException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = ErrorResponse.of(exception.getCode(), exception.getMessage(), exception.getField());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void authenticate(String token) {
        var claims = jwtService.parseAccessToken(token);
        TenantContext.setTenantId(claims.tenantId());

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name()));
        var authentication = new UsernamePasswordAuthenticationToken(claims.userId(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
