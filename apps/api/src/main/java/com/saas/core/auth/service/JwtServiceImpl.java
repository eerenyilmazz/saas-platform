package com.saas.core.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.saas.core.auth.entity.Role;
import com.saas.core.auth.entity.User;
import com.saas.core.config.JwtProperties;
import com.saas.core.exception.UnauthorizedException;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    @Override
    public String issueAccessToken(User user) {
        var now = new Date();
        var expiresAt = new Date(now.getTime() + jwtProperties.getAccessTokenTtl().toMillis());
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("tenant_id", user.getTenantId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(signingKey())
                .compact();
    }

    @Override
    public AccessTokenClaims parseAccessToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new AccessTokenClaims(
                    UUID.fromString(claims.getSubject()),
                    UUID.fromString(claims.get("tenant_id", String.class)),
                    Role.valueOf(claims.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("AUTH_INVALID_TOKEN", "Geçersiz ya da süresi dolmuş oturum.");
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
