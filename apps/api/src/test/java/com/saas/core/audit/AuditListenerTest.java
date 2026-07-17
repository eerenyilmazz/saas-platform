package com.saas.core.audit;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.saas.core.auth.entity.Role;
import com.saas.core.auth.entity.User;
import com.saas.core.auth.repository.UserRepository;
import com.saas.core.tenant.TenantContext;
import com.saas.testsupport.AbstractIntegrationTest;

/**
 * Proves the generic mechanism (AuditListener + AuditWriter), not just that User happens to have
 * rows in audit_log - the point of audit-log.md's design is that a future @Audited entity needs
 * no code of its own, so exercising create/update/delete here through plain
 * JpaRepository/EntityManager calls (never touching AuditListener/AuditWriter directly) is what
 * actually verifies that.
 */
class AuditListenerTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createUpdateAndDeleteEachWriteAnAuditLogEntry() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);

        User created = userRepository.save(
                new User(tenantId, "audit-test@example.com", "hashed-password", Role.TENANT_STAFF));
        UUID userId = created.getId();

        User reloaded = userRepository.findById(userId).orElseThrow();
        reloaded.changeRole(Role.TENANT_ADMIN);
        userRepository.save(reloaded);

        userRepository.deleteById(userId);

        List<AuditRow> rows = queryAuditLog(userId);
        assertThat(rows).extracting(AuditRow::action).containsExactly("create", "update", "delete");

        AuditRow createRow = rows.get(0);
        assertThat(createRow.tenantId()).isEqualTo(tenantId);
        assertThat(createRow.oldValue()).isNull();
        assertThat(createRow.newValue()).contains("\"role\": \"TENANT_STAFF\"");
        assertThat(createRow.newValue()).contains("***REDACTED***");
        assertThat(createRow.newValue()).doesNotContain("hashed-password");

        AuditRow updateRow = rows.get(1);
        assertThat(updateRow.oldValue()).contains("\"role\": \"TENANT_STAFF\"");
        assertThat(updateRow.newValue()).contains("\"role\": \"TENANT_ADMIN\"");

        AuditRow deleteRow = rows.get(2);
        assertThat(deleteRow.newValue()).isNull();
        assertThat(deleteRow.oldValue()).contains("\"role\": \"TENANT_ADMIN\"");
    }

    private UUID insertTenant() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("SET LOCAL app.platform_context = 'true'")) {
                statement.execute();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO tenant (name) VALUES (?) RETURNING id")) {
                statement.setString(1, "Audit Test Tenant");
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    UUID id = resultSet.getObject("id", UUID.class);
                    connection.commit();
                    return id;
                }
            }
        }
    }

    private List<AuditRow> queryAuditLog(UUID recordId) throws Exception {
        TenantContext.clear();
        TenantContext.setPlatformContext(true);
        List<AuditRow> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT tenant_id, action, old_value, new_value FROM audit_log "
                            + "WHERE record_id = ? ORDER BY occurred_at")) {
                statement.setObject(1, recordId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        rows.add(new AuditRow(
                                resultSet.getObject("tenant_id", UUID.class),
                                resultSet.getString("action"),
                                resultSet.getString("old_value"),
                                resultSet.getString("new_value")));
                    }
                }
            }
            connection.rollback();
        } finally {
            TenantContext.clear();
        }
        return rows;
    }

    private record AuditRow(UUID tenantId, String action, String oldValue, String newValue) {
    }
}
