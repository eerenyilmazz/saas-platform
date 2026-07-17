package com.saas.core.onboarding;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.saas.core.auth.entity.Role;
import com.saas.core.tenant.TenantContext;
import com.saas.testsupport.AbstractIntegrationTest;

class TenantOnboardingServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TenantOnboardingService tenantOnboardingService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void onboardsATenantAndATenantAdminWithAWorkingBcryptHash() {
        var result = tenantOnboardingService.onboardTenant(
                "Seed Test Gym", "owner@seed-test.example.com", "correct-horse-battery-staple");

        assertThat(result.tenant().getId()).isNotNull();
        assertThat(result.tenant().getName()).isEqualTo("Seed Test Gym");

        assertThat(result.admin().getTenantId()).isEqualTo(result.tenant().getId());
        assertThat(result.admin().getEmail()).isEqualTo("owner@seed-test.example.com");
        assertThat(result.admin().getRole()).isEqualTo(Role.TENANT_ADMIN);
        assertThat(passwordEncoder.matches("correct-horse-battery-staple", result.admin().getPasswordHash())).isTrue();
    }
}
