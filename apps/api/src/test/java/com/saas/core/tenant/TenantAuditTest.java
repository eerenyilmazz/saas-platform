package com.saas.core.tenant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.saas.core.tenant.entity.Tenant;
import com.saas.core.tenant.repository.TenantRepository;
import com.saas.testsupport.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * audit-log.md's scope list includes "tenant.settings" (feature flag changes) - this proves
 * Tenant actually reaches audit_log, using tenant_id = its own id (SelfTenantIdentity), not just
 * that saving a Tenant no longer throws (which TenantModuleFlagPersistenceTest already covers).
 */
class TenantAuditTest extends AbstractIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void creatingATenantAndTogglingAModuleFlagEachWriteAnAuditLogEntry() throws Exception {
        TenantContext.setPlatformContext(true);
        Tenant tenant = tenantRepository.save(new Tenant("Audit Test Tenant"));
        UUID tenantId = tenant.getId();

        Tenant reloaded = tenantRepository.findById(tenantId).orElseThrow();
        reloaded.enableModule("fitness");
        tenantRepository.save(reloaded);

        List<String> actions = queryAuditActions(tenantId);
        assertThat(actions).containsExactly("create", "update");
    }

    private List<String> queryAuditActions(UUID recordId) throws Exception {
        TenantContext.clear();
        TenantContext.setPlatformContext(true);
        List<String> actions = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT action, tenant_id FROM audit_log WHERE record_id = ? AND table_name = 'tenant' "
                            + "ORDER BY occurred_at")) {
                statement.setObject(1, recordId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        assertThat(resultSet.getObject("tenant_id", UUID.class)).isEqualTo(recordId);
                        actions.add(resultSet.getString("action"));
                    }
                }
            }
            connection.rollback();
        } finally {
            TenantContext.clear();
        }
        return actions;
    }
}
