package com.saas.core.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * database-conventions.md: "gerçek DELETE SQL komutu hiç kullanılmaz." Proves the @SQLDelete/
 * @SQLRestriction pair on User actually does that - not just that userRepository.deleteById()
 * doesn't throw, but that the row is still physically present (raw JDBC bypasses Hibernate's
 * @SQLRestriction filter, the only way to see a soft-deleted row) with deleted_at set, while the
 * normal JPA path correctly treats it as gone.
 */
class UserSoftDeleteTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void deletingAUserOnlyMarksItDeletedAtTheRowNeverActuallyDisappears() throws Exception {
        UUID tenantId = insertTenant();
        TenantContext.setTenantId(tenantId);
        User user = userRepository.save(
                new User(tenantId, "soft-delete-test@example.com", "hashed-password", Role.TENANT_STAFF));
        UUID userId = user.getId();

        userRepository.deleteById(userId);

        assertThat(userRepository.findById(userId)).isEmpty();

        Timestamp deletedAt = queryRawDeletedAt(userId);
        assertThat(deletedAt).isNotNull();
    }

    private Timestamp queryRawDeletedAt(UUID userId) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT deleted_at FROM \"user\" WHERE id = ?")) {
                statement.setObject(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertThat(resultSet.next())
                            .as("the row must still physically exist for a raw, unfiltered query")
                            .isTrue();
                    Timestamp deletedAt = resultSet.getTimestamp("deleted_at");
                    connection.rollback();
                    return deletedAt;
                }
            }
        }
    }

    private UUID insertTenant() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("SET LOCAL app.platform_context = 'true'")) {
                statement.execute();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO tenant (name) VALUES (?) RETURNING id")) {
                statement.setString(1, "Soft Delete Test Tenant");
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    UUID id = resultSet.getObject("id", UUID.class);
                    connection.commit();
                    return id;
                }
            }
        }
    }
}
