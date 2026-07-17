package com.saas.core.auth.entity;

/**
 * platform_owner is deliberately not here - it has no row in "user" (see glossary.md and
 * V202607172044__create_user_table.sql), it's a separate, not-yet-built concern under
 * com.saas.platform.* (roles-and-access.md: no platform admin panel in MVP).
 */
public enum Role {
    TENANT_ADMIN,
    TENANT_STAFF
}
