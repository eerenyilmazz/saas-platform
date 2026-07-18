package com.saas.core.billing.entity;

import java.math.BigDecimal;
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * source_type/source_id are intentionally generic (modules/core.md) so a module without a
 * "membership" concept (e.g. an order-based vertical) can still record a payment without a core
 * migration.
 */
@Entity
@Table(name = "tenant_payment")
@EntityListeners(AuditListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenantPayment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String sourceType;

    @Column(nullable = false)
    private UUID sourceId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false)
    private Instant paidAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private String idempotencyKey;

    public TenantPayment(UUID tenantId, String sourceType, UUID sourceId, BigDecimal amount, PaymentMethod method,
            Instant paidAt) {
        this.tenantId = tenantId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt;
    }

    /**
     * For callers that can retry the same request (e.g. a payment provider's webhook) -
     * idempotencyKey is optional, per concurrency-and-idempotency.md.
     */
    public TenantPayment(UUID tenantId, String sourceType, UUID sourceId, BigDecimal amount, PaymentMethod method,
            Instant paidAt, String idempotencyKey) {
        this(tenantId, sourceType, sourceId, amount, method, paidAt);
        this.idempotencyKey = idempotencyKey;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
