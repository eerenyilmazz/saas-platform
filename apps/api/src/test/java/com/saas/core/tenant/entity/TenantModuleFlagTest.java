package com.saas.core.tenant.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure domain logic (JSON manipulation on Tenant.settings) - the first test in this codebase that
 * deliberately does not extend AbstractIntegrationTest, since none of this needs a real database
 * (testing-strategy.md's "birim testi" level, as opposed to every other existing test here which
 * happens to also need Postgres for what it's proving).
 */
class TenantModuleFlagTest {

    @Test
    void newTenantHasNoModulesEnabledByDefault() {
        Tenant tenant = new Tenant("Acme Gym");

        assertThat(tenant.isModuleEnabled("fitness")).isFalse();
    }

    @Test
    void enablingAModuleMakesItEnabled() {
        Tenant tenant = new Tenant("Acme Gym");

        tenant.enableModule("fitness");

        assertThat(tenant.isModuleEnabled("fitness")).isTrue();
    }

    @Test
    void disablingAModuleMakesItDisabledAgain() {
        Tenant tenant = new Tenant("Acme Gym");
        tenant.enableModule("fitness");

        tenant.disableModule("fitness");

        assertThat(tenant.isModuleEnabled("fitness")).isFalse();
    }

    @Test
    void enablingOneModuleDoesNotAffectAnother() {
        Tenant tenant = new Tenant("Acme Gym");

        tenant.enableModule("fitness");

        assertThat(tenant.isModuleEnabled("berber")).isFalse();
    }

    @Test
    void moduleConfigIsNullByDefault() {
        Tenant tenant = new Tenant("Acme Gym");

        assertThat(tenant.getModuleConfig("fitness", "qrSecretKey")).isNull();
    }

    @Test
    void settingModuleConfigMakesItReadable() {
        Tenant tenant = new Tenant("Acme Gym");

        tenant.setModuleConfig("fitness", "qrSecretKey", "abc123");

        assertThat(tenant.getModuleConfig("fitness", "qrSecretKey")).isEqualTo("abc123");
    }

    @Test
    void moduleConfigDoesNotDisturbTheEnabledFlag() {
        Tenant tenant = new Tenant("Acme Gym");
        tenant.enableModule("fitness");

        tenant.setModuleConfig("fitness", "qrSecretKey", "abc123");

        assertThat(tenant.isModuleEnabled("fitness")).isTrue();
        assertThat(tenant.getModuleConfig("fitness", "qrSecretKey")).isEqualTo("abc123");
    }
}
