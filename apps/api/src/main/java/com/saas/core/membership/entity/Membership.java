package com.saas.core.membership.entity;

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
import jakarta.persistence.Version;

import com.saas.core.audit.AuditListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * INV-1 (domain-invariants.md) - a member can't hold two ACTIVE memberships in the same tenant at
 * once - is enforced by idx_membership_one_active_per_member (a partial unique index), not here:
 * a Java-side check racing another request could still both pass before either commits, where the
 * DB constraint can't be bypassed regardless of timing.
 */
@Entity
@Table(name = "membership")
@EntityListeners(AuditListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private UUID planId;

    @Column(nullable = false)
    private Instant startAt;

    @Column(nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public Membership(UUID tenantId, UUID memberId, UUID planId, Instant startAt, Instant endAt) {
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.planId = planId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = MembershipStatus.ACTIVE;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void freeze() {
        this.status = MembershipStatus.FROZEN;
    }

    public void expire() {
        this.status = MembershipStatus.EXPIRED;
    }
}
