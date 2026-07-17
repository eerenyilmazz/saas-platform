package com.saas.core.membership.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.saas.core.audit.AuditListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "membership_plan")
@EntityListeners(AuditListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipPlan {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int durationDays;

    /** Null means unlimited entries for the plan's duration. */
    private Integer entryAllowance;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public MembershipPlan(UUID tenantId, String name, int durationDays, Integer entryAllowance) {
        this.tenantId = tenantId;
        this.name = name;
        this.durationDays = durationDays;
        this.entryAllowance = entryAllowance;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
