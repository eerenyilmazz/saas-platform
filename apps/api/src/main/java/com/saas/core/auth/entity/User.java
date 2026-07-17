package com.saas.core.auth.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.saas.core.audit.AuditListener;
import com.saas.core.audit.AuditSensitive;

@Entity
@Table(name = "`user`")
@EntityListeners(AuditListener.class)
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String email;

    @AuditSensitive
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {
        // JPA
    }

    public User(UUID tenantId, String email, String passwordHash, Role role) {
        this.tenantId = tenantId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @PrePersist
    void onCreate() {
        // See Tenant.onCreate() - Hibernate's generated INSERT always lists this column, which
        // overrides the DB-side DEFAULT now() with a literal NULL unless set here.
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
