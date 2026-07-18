package com.saas.core.auth.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * database-conventions.md's soft-delete rule: a repository .delete()/.deleteById() call never
 * issues a real DELETE. @SQLDelete rewrites the DELETE statement Hibernate would otherwise send
 * into this UPDATE; @SQLRestriction adds "deleted_at IS NULL" to every query Hibernate generates
 * for this entity, so a soft-deleted row disappears from normal reads without any repository code
 * needing to filter for it itself.
 */
@Entity
@Table(name = "`user`")
@EntityListeners(AuditListener.class)
@SQLDelete(sql = "UPDATE \"user\" SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private Instant deletedAt;

    public User(UUID tenantId, String email, String passwordHash, Role role) {
        this.tenantId = tenantId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
