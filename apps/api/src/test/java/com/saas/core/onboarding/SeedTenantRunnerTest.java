package com.saas.core.onboarding;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.saas.core.auth.entity.Role;
import com.saas.core.auth.repository.UserRepository;
import com.saas.core.tenant.TenantContext;
import com.saas.core.tenant.repository.TenantRepository;
import com.saas.testsupport.AbstractIntegrationTest;

/**
 * Constructs SeedTenantRunner directly rather than @Autowired-ing it: the class is @Profile
 * ("seed")-gated so Spring never registers it under the test context's default profile, but its
 * seed() method (what actually matters here) only needs the same beans any other test already
 * gets - no need to fight the profile gate just to call it.
 */
class SeedTenantRunnerTest extends AbstractIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void seedsATenantAndATenantAdminWithAWorkingBcryptHash() {
        var runner = new SeedTenantRunner(tenantRepository, userRepository, passwordEncoder, applicationContext);

        var result = runner.seed("Seed Test Gym", "owner@seed-test.example.com", "correct-horse-battery-staple");

        assertThat(result.tenant().getId()).isNotNull();
        assertThat(result.tenant().getName()).isEqualTo("Seed Test Gym");

        assertThat(result.admin().getTenantId()).isEqualTo(result.tenant().getId());
        assertThat(result.admin().getEmail()).isEqualTo("owner@seed-test.example.com");
        assertThat(result.admin().getRole()).isEqualTo(Role.TENANT_ADMIN);
        assertThat(passwordEncoder.matches("correct-horse-battery-staple", result.admin().getPasswordHash())).isTrue();
    }
}
