package com.saas.modules.fitness.qr.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.saas.core.exception.BusinessRuleException;
import com.saas.core.exception.NotFoundException;
import com.saas.core.i18n.Messages;
import com.saas.core.membership.entity.Member;
import com.saas.core.membership.repository.MemberRepository;
import com.saas.core.tenant.TenantContext;
import com.saas.core.tenant.entity.Tenant;
import com.saas.core.tenant.repository.TenantRepository;
import com.saas.modules.fitness.FitnessModule;
import com.saas.modules.fitness.qr.FitnessQrProperties;
import com.saas.modules.fitness.qr.service.QrTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QrTokenServiceImpl implements QrTokenService {

    private static final String SECRET_CONFIG_KEY = "qrSecretKey";
    private static final int SECRET_BYTES = 32;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final TenantRepository tenantRepository;
    private final MemberRepository memberRepository;
    private final FitnessQrProperties fitnessQrProperties;
    private final Messages messages;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String issue(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("MEMBER_NOT_FOUND", messages.get("MEMBER_NOT_FOUND")));
        Tenant tenant = tenantRepository.findById(member.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Tenant not found for member " + memberId));
        String secret = tenantSecret(tenant);

        String payload = memberId + ":" + Instant.now().getEpochSecond();
        return encode(payload) + "." + sign(payload, secret);
    }

    @Override
    public UUID verify(String rawToken) {
        String[] parts = rawToken.split("\\.", 2);
        if (parts.length != 2) {
            throw invalidToken();
        }

        String payload = decode(parts[0]);
        String[] payloadParts = payload.split(":", 2);
        if (payloadParts.length != 2) {
            throw invalidToken();
        }

        UUID memberId;
        long issuedAt;
        try {
            memberId = UUID.fromString(payloadParts[0]);
            issuedAt = Long.parseLong(payloadParts[1]);
        } catch (RuntimeException exception) {
            throw invalidToken();
        }

        long age = Instant.now().getEpochSecond() - issuedAt;
        if (age < 0 || age > fitnessQrProperties.getTokenTtl().toSeconds()) {
            throw invalidToken();
        }

        Tenant tenant = tenantRepository.findById(TenantContext.getTenantId())
                .orElseThrow(this::invalidToken);
        if (!constantTimeEquals(sign(payload, tenantSecret(tenant)), parts[1])) {
            throw invalidToken();
        }

        return memberId;
    }

    private String tenantSecret(Tenant tenant) {
        String existing = tenant.getModuleConfig(FitnessModule.NAME, SECRET_CONFIG_KEY);
        if (existing != null) {
            return existing;
        }
        byte[] bytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(bytes);
        String generated = encode(bytes);
        tenant.setModuleConfig(FitnessModule.NAME, SECRET_CONFIG_KEY, generated);
        tenantRepository.save(tenant);
        return generated;
    }

    private String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return encode(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(HMAC_ALGORITHM + " must be available on every JVM", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(String value) {
        return encode(value.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String decode(String value) {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw invalidToken();
        }
    }

    private BusinessRuleException invalidToken() {
        return new BusinessRuleException("FITNESS_QR_INVALID", messages.get("FITNESS_QR_INVALID"));
    }
}
