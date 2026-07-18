package com.saas.core.billing.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.core.billing.entity.TenantPayment;

public interface TenantPaymentRepository extends JpaRepository<TenantPayment, UUID> {
}
