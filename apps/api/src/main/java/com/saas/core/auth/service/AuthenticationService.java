package com.saas.core.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saas.core.auth.dto.LoginRequest;
import com.saas.core.auth.dto.LoginResponse;
import com.saas.core.auth.entity.User;
import com.saas.core.auth.repository.UserRepository;
import com.saas.core.exception.UnauthorizedException;
import com.saas.core.tenant.TenantContext;

@Service
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "E-posta veya şifre hatalı.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        var user = findUserAcrossTenants(request.email())
                .orElseThrow(() -> new UnauthorizedException("AUTH_INVALID_CREDENTIALS", INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("AUTH_INVALID_CREDENTIALS", INVALID_CREDENTIALS_MESSAGE);
        }

        return new LoginResponse(jwtService.issueAccessToken(user));
    }

    /**
     * Login doesn't know the caller's tenant_id yet - resolving it is the whole point - so this
     * looks the user up across all tenants under the same app.platform_context bypass the tenant
     * table itself uses for system-level reads (see V202607172044's RLS policy / ADR 0008).
     *
     * Deliberately not @Transactional itself: Spring's transaction interceptor checks a
     * connection out (and TenantAwareDataSource stamps it) when the proxied method is entered,
     * which would be before the setPlatformContext(true) below ever runs. Leaving this
     * non-transactional lets userRepository.findByEmail's own (Spring Data JPA default,
     * read-only) transaction begin - and check out its connection - after the ThreadLocal is
     * already set.
     */
    private Optional<User> findUserAcrossTenants(String email) {
        TenantContext.setPlatformContext(true);
        try {
            return userRepository.findByEmail(email);
        } finally {
            TenantContext.setPlatformContext(false);
        }
    }
}
