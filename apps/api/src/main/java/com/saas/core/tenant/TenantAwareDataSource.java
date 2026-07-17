package com.saas.core.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Stamps every connection checkout with the current request's RLS session variables before
 * handing the connection back to the caller (JPA, JdbcTemplate, ...). Uses session-level SET
 * (not SET LOCAL/transaction-scoped) because Hikari pools and reuses physical connections across
 * requests - if this ran only once per transaction, a connection previously stamped for tenant A
 * could otherwise be reused for tenant B's request. Running unconditionally on every checkout
 * (RESET when TenantContext is empty) means a pooled connection is always correct for whichever
 * request happens to be borrowing it, regardless of what the previous borrower left behind.
 *
 * tenantId is always a UUID parsed from a JWT this application itself issued (JwtService), never
 * raw user input, so string concatenation into SET carries no injection risk - SET does not
 * support JDBC bind parameters (see the same rationale in TenantRlsTest).
 */
public class TenantAwareDataSource extends DelegatingDataSource {

    public TenantAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return applyTenantContext(super.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return applyTenantContext(super.getConnection(username, password));
    }

    private Connection applyTenantContext(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            var tenantId = TenantContext.getTenantId();
            statement.execute(tenantId != null
                    ? "SET app.current_tenant_id = '" + tenantId + "'"
                    : "RESET app.current_tenant_id");
            statement.execute(TenantContext.isPlatformContext()
                    ? "SET app.platform_context = 'true'"
                    : "RESET app.platform_context");
        }
        return connection;
    }
}
