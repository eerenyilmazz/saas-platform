package com.saas.core.tenant.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant {

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
