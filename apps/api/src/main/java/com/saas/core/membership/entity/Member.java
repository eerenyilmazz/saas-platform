package com.saas.core.membership.entity;

import java.time.Instant;
import java.util.UUID;

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
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    private String phone;

    private Instant consentAt;

    private UUID userId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Member(UUID tenantId, String name, String phone) {
        this.tenantId = tenantId;
        this.name = name;
        this.phone = phone;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void recordConsent() {
        this.consentAt = Instant.now();
    }

    public void linkUser(UUID userId) {
        this.userId = userId;
    }
}
