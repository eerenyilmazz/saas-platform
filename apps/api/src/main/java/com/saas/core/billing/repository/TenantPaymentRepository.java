package com.saas.core.billing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.core.billing.entity.TenantPayment;

public interface TenantPaymentRepository extends JpaRepository<TenantPayment, UUID> {

    Optional<TenantPayment> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey);
}
