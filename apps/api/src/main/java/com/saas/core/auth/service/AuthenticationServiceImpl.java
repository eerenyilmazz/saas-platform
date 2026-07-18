package com.saas.core.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saas.core.auth.dto.LoginRequest;
import com.saas.core.auth.dto.LoginResponse;
import com.saas.core.auth.entity.User;
import com.saas.core.auth.repository.UserRepository;
import com.saas.core.auth.security.LoginAttemptTracker;
import com.saas.core.exception.ForbiddenException;
import com.saas.core.exception.UnauthorizedException;
import com.saas.core.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "E-posta veya şifre hatalı.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptTracker loginAttemptTracker;

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        String ipKey = "ip:" + clientIp;
        String emailKey = "email:" + request.email();

        if (loginAttemptTracker.isLocked(ipKey) || loginAttemptTracker.isLocked(emailKey)) {
            // 403, not the semantically closer 429: error-handling.md's exception hierarchy is a
            // fixed set of 6 types with no rate-limit entry, and forbids adding a 7th.
            throw new ForbiddenException("AUTH_TOO_MANY_ATTEMPTS",
                    "Çok fazla başarısız deneme. Lütfen daha sonra tekrar deneyin.");
        }

        try {
            LoginResponse response = authenticate(request);
            loginAttemptTracker.recordSuccess(ipKey);
            loginAttemptTracker.recordSuccess(emailKey);
            return response;
        } catch (UnauthorizedException exception) {
            loginAttemptTracker.recordFailure(ipKey);
            loginAttemptTracker.recordFailure(emailKey);
            throw exception;
        }
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        var user = refreshTokenService.validateAndConsume(refreshToken);
        return issueTokens(user);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private LoginResponse authenticate(LoginRequest request) {
        var user = findUserAcrossTenants(request.email())
                .orElseThrow(() -> new UnauthorizedException("AUTH_INVALID_CREDENTIALS", INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("AUTH_INVALID_CREDENTIALS", INVALID_CREDENTIALS_MESSAGE);
        }

        return issueTokens(user);
    }

    private LoginResponse issueTokens(User user) {
        return new LoginResponse(jwtService.issueAccessToken(user), refreshTokenService.issue(user));
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
