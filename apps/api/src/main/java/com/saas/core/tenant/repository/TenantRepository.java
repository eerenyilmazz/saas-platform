package com.saas.core.tenant.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.core.tenant.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
