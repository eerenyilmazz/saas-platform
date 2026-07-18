package com.saas.core.tenant;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.saas.core.tenant.entity.Tenant;
import com.saas.core.tenant.repository.TenantRepository;
import com.saas.testsupport.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TenantModuleFlagTest already proves the JSON logic in isolation - this proves the same flag
 * actually survives a round trip through the real settings JSONB column (Tenant.settings is
 * mapped with @JdbcTypeCode(SqlTypes.JSON), which TenantModuleFlagTest's in-memory objects never
 * exercise).
 */
class TenantModuleFlagPersistenceTest extends AbstractIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void anEnabledModuleSurvivesBeingReloadedFromTheDatabase() {
        TenantContext.setPlatformContext(true);
        Tenant tenant = new Tenant("Acme Gym");
        tenant.enableModule("fitness");
        UUID tenantId = tenantRepository.saveAndFlush(tenant).getId();

        Tenant reloaded = tenantRepository.findById(tenantId).orElseThrow();

        assertThat(reloaded.isModuleEnabled("fitness")).isTrue();
        assertThat(reloaded.isModuleEnabled("berber")).isFalse();
    }
}
