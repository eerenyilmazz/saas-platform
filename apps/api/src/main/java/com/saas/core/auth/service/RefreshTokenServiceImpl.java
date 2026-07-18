package com.saas.core.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.saas.core.auth.entity.RefreshToken;
import com.saas.core.auth.entity.User;
import com.saas.core.auth.repository.RefreshTokenRepository;
import com.saas.core.auth.repository.UserRepository;
import com.saas.core.config.JwtProperties;
import com.saas.core.exception.UnauthorizedException;
import com.saas.core.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final String INVALID_REFRESH_TOKEN_MESSAGE = "Oturum süresi dolmuş, lütfen tekrar giriş yapın.";
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Sets the tenant explicitly (not app.platform_context) because, unlike validateAndConsume,
     * the caller already knows it - this is called right after a successful login/refresh, so
     * the insert should behave like any other request scoped to that tenant. Without this, the
     * row's own tenant_id would satisfy the policy but no session variable would - login runs
     * before JwtAuthenticationFilter ever sets one - and the insert would be rejected by RLS.
     */
    @Override
    public String issue(User user) {
        String rawToken = generateRawToken();
        var expiresAt = Instant.now().plus(jwtProperties.getRefreshTokenTtl());
        var refreshToken = new RefreshToken(user.getTenantId(), user.getId(), hash(rawToken), expiresAt);
        TenantContext.setTenantId(user.getTenantId());
        try {
            refreshTokenRepository.save(refreshToken);
        } finally {
            TenantContext.setTenantId(null);
        }
        return rawToken;
    }

    /**
     * Mirrors AuthenticationServiceImpl.findUserAcrossTenants: a raw refresh token carries no
     * tenant_id by itself, so both the token-hash lookup and the user lookup it resolves to need
     * app.platform_context's bypass of the tenant_id RLS filter (see
     * V202607181000__create_refresh_token_table.sql) - resolving the tenant is the whole point of
     * this call. Deliberately not @Transactional for the same reason as that method: it would
     * check out a connection before the flag below is set.
     */
    @Override
    public User validateAndConsume(String refreshToken) {
        TenantContext.setPlatformContext(true);
        try {
            var token = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                    .filter(RefreshToken::isActive)
                    .orElseThrow(() -> new UnauthorizedException("AUTH_INVALID_REFRESH_TOKEN", INVALID_REFRESH_TOKEN_MESSAGE));
            token.revoke();
            refreshTokenRepository.save(token);
            return userRepository.findById(token.getUserId())
                    .orElseThrow(() -> new UnauthorizedException("AUTH_INVALID_REFRESH_TOKEN", INVALID_REFRESH_TOKEN_MESSAGE));
        } finally {
            TenantContext.setPlatformContext(false);
        }
    }

    @Override
    public void revoke(String refreshToken) {
        TenantContext.setPlatformContext(true);
        try {
            refreshTokenRepository.findByTokenHash(hash(refreshToken))
                    .filter(RefreshToken::isActive)
                    .ifPresent(token -> {
                        token.revoke();
                        refreshTokenRepository.save(token);
                    });
        } finally {
            TenantContext.setPlatformContext(false);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available on every JVM", exception);
        }
    }
}
