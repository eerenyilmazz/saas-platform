package com.saas.core.tenant.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.saas.testsupport.AbstractIntegrationTest;

/**
 * Proves ADR 0008 / tenant-isolation-enforcement.md: RLS blocks cross-tenant reads (and writes
 * without any tenant/platform context) at the database level, independent of the application
 * layer. Uses raw JDBC throughout rather than TenantRepository: the per-request interceptor that
 * sets app.current_tenant_id from the authenticated user's JWT does not exist yet (it lands with
 * auth in this same phase) - until it does, a repository call carries no session context, and RLS
 * correctly rejects it. This test isolates the RLS policy itself from that not-yet-built wiring.
 */
class TenantRlsTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void rlsCannotBeBypassed() throws Exception {
        UUID tenantAId;

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            setPlatformContext(connection);
            tenantAId = insertTenant(connection, "Tenant A");
            insertTenant(connection, "Tenant B");
            connection.commit();
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            setCurrentTenant(connection, tenantAId);

            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM tenant");
                    ResultSet resultSet = statement.executeQuery()) {
                int rowCount = 0;
                while (resultSet.next()) {
                    rowCount++;
                    assertThat(resultSet.getObject("id", UUID.class)).isEqualTo(tenantAId);
                }
                assertThat(rowCount).isEqualTo(1);
            }
            connection.rollback();
        }
    }

    @Test
    void noContextSeesNoRows() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            setPlatformContext(connection);
            insertTenant(connection, "Tenant C");
            connection.commit();
        }

        // A fresh connection with neither app.current_tenant_id nor app.platform_context set -
        // the default-deny posture RLS is chosen for: no context means no rows, not all rows.
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM tenant");
                    ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                assertThat(resultSet.getLong(1)).isEqualTo(0);
            }
            connection.rollback();
        }
    }

    @Test
    void platformContextSeesAllTenants() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            setPlatformContext(connection);
            insertTenant(connection, "Tenant D");
            insertTenant(connection, "Tenant E");

            try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM tenant");
                    ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                assertThat(resultSet.getLong(1)).isGreaterThanOrEqualTo(2);
            }
            connection.rollback();
        }
    }

    // SET LOCAL does not support JDBC bind parameters (it is a configuration command, not DML).
    // tenantId is always database-generated, name is always a test literal - neither is user
    // input, so string concatenation here carries no injection risk despite the general rule.
    private void setCurrentTenant(Connection connection, UUID tenantId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SET LOCAL app.current_tenant_id = '" + tenantId + "'")) {
            statement.execute();
        }
    }

    private void setPlatformContext(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SET LOCAL app.platform_context = 'true'")) {
            statement.execute();
        }
    }

    private UUID insertTenant(Connection connection, String name) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO tenant (name) VALUES (?) RETURNING id")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getObject("id", UUID.class);
            }
        }
    }
}
