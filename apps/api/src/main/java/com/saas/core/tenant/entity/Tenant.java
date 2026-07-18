package com.saas.core.tenant.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.saas.core.audit.AuditListener;
import com.saas.core.audit.SelfTenantIdentity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * audit-log.md scope: "tenant.settings - feature flag (modül aç/kapa) değişiklikleri". Implements
 * SelfTenantIdentity (rather than adding a redundant tenantId field pointing at its own id) so
 * AuditWriter can resolve which tenant_id to write to audit_log for a Tenant change.
 */
@Entity
@Table(name = "tenant")
@EntityListeners(AuditListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant implements SelfTenantIdentity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String settings;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Tenant(String name) {
        this.name = name;
        this.settings = "{}";
    }

    @PrePersist
    void onCreate() {
        // The column has a DB-side DEFAULT now(), but Hibernate always lists every mapped column
        // explicitly in its INSERT, which overrides that default with a literal NULL unless the
        // Java side sets a value itself.
        createdAt = Instant.now();
    }

    @Override
    public UUID auditTenantId() {
        return id;
    }

    public boolean isModuleEnabled(String module) {
        return TenantSettings.parse(settings).isModuleEnabled(module);
    }

    public void enableModule(String module) {
        setModuleEnabled(module, true);
    }

    public void disableModule(String module) {
        setModuleEnabled(module, false);
    }

    private void setModuleEnabled(String module, boolean enabled) {
        TenantSettings parsedSettings = TenantSettings.parse(settings);
        parsedSettings.setModuleEnabled(module, enabled);
        this.settings = parsedSettings.toJson();
    }
}
