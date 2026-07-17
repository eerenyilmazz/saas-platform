package com.saas.core.tenant;

import java.util.UUID;

/**
 * Per-request tenant identity, populated by JwtAuthenticationFilter from the caller's JWT and
 * read by TenantAwareDataSource on every connection checkout to set app.current_tenant_id (see
 * V202607171822__create_tenant_table.sql / ADR 0008). Thread-bound rather than request-scoped:
 * the servlet container reuses threads across requests, so JwtAuthenticationFilter must clear
 * this in a finally block after every request or a value would leak into the next one handled by
 * the same thread.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> PLATFORM_CONTEXT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static UUID getTenantId() {
        return TENANT_ID.get();
    }

    public static void setPlatformContext(boolean platformContext) {
        PLATFORM_CONTEXT.set(platformContext);
    }

    public static boolean isPlatformContext() {
        return Boolean.TRUE.equals(PLATFORM_CONTEXT.get());
    }

    public static void clear() {
        TENANT_ID.remove();
        PLATFORM_CONTEXT.remove();
    }
}
