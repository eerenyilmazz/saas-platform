package com.saas.core.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.saas.testsupport.AbstractIntegrationTest;

/**
 * TenantRlsTest proves the RLS policies themselves work when app.current_tenant_id is set by
 * hand (SET LOCAL) on a raw connection. This test proves the other half: that
 * TenantAwareDataSource actually sets that session variable automatically from TenantContext on
 * every connection checkout - the mechanism JwtAuthenticationFilter relies on for every real
 * request, with no per-query code needed to opt in.
 */
class TenantAwareDataSourceTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void connectionCheckoutScopesQueriesToTheThreadLocalTenant() throws Exception {
        UUID tenantAId;
        UUID tenantBId;
        TenantContext.setPlatformContext(true);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            tenantAId = insertTenant(connection, "DataSource Test Tenant A");
            tenantBId = insertTenant(connection, "DataSource Test Tenant B");
            connection.commit();
        } finally {
            TenantContext.setPlatformContext(false);
        }

        TenantContext.setTenantId(tenantAId);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
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

        assertThat(tenantBId).isNotNull();
    }

    @Test
    void aPooledConnectionPreviouslyScopedToOneTenantDoesNotLeakIntoTheNext() throws Exception {
        UUID tenantAId;
        TenantContext.setPlatformContext(true);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            tenantAId = insertTenant(connection, "Leak Test Tenant");
            connection.commit();
        } finally {
            TenantContext.setPlatformContext(false);
        }

        TenantContext.setTenantId(tenantAId);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            connection.rollback();
        }

        TenantContext.clear();
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
