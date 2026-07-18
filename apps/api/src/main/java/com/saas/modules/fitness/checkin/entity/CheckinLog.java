package com.saas.modules.fitness.checkin.entity;

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
@Table(name = "checkin_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckinLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private UUID membershipId;

    @Column(nullable = false, updatable = false)
    private Instant checkedInAt;

    private Instant checkedOutAt;

    public CheckinLog(UUID tenantId, UUID memberId, UUID membershipId) {
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.membershipId = membershipId;
    }

    @PrePersist
    void onCreate() {
        checkedInAt = Instant.now();
    }

    public void checkOut() {
        this.checkedOutAt = Instant.now();
    }
}
