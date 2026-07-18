package com.saas.core.audit;

import java.util.UUID;

/**
 * Implemented by an @Audited entity that IS itself the tenant (only Tenant today) instead of
 * carrying a separate tenantId field like every other audited entity. Keeps AuditWriter from
 * depending on com.saas.core.tenant.entity directly - the dependency stays one-way (entities
 * depend on audit, not the reverse).
 */
public interface SelfTenantIdentity {

    UUID auditTenantId();
}
