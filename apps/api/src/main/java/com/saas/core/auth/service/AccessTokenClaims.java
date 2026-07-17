package com.saas.core.auth.service;

import java.util.UUID;

import com.saas.core.auth.entity.Role;

public record AccessTokenClaims(UUID userId, UUID tenantId, Role role) {
}
