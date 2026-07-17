package com.saas.core.auth.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.saas.testsupport.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Overrides app.login-rate-limit.* to a tiny threshold (2 attempts) so the lockout triggers
 * within a handful of requests instead of needing security.md's real default (5) - the mechanism
 * being tested doesn't care about the specific numbers, only that it fires and holds.
 */
@AutoConfigureMockMvc
class LoginRateLimitTest extends AbstractIntegrationTest {

    private static final String RAW_PASSWORD = "correct-horse-battery-staple";

    @DynamicPropertySource
    static void rateLimitProperties(DynamicPropertyRegistry registry) {
        registry.add("app.login-rate-limit.max-attempts", () -> "2");
        registry.add("app.login-rate-limit.window", () -> "1h");
        registry.add("app.login-rate-limit.lockout-duration", () -> "1h");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptTracker loginAttemptTracker;

    private String email;

    @BeforeEach
    void seedTenantAndUser() throws Exception {
        loginAttemptTracker.clear();
        email = "rate-limit-" + UUID.randomUUID() + "@example.com";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("SET LOCAL app.platform_context = 'true'")) {
                statement.execute();
            }
            UUID tenantId;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO tenant (name) VALUES (?) RETURNING id")) {
                statement.setString(1, "Rate Limit Test Tenant");
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    tenantId = resultSet.getObject("id", UUID.class);
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO \"user\" (tenant_id, email, password_hash, role) VALUES (?, ?, ?, 'TENANT_ADMIN')")) {
                statement.setObject(1, tenantId);
                statement.setString(2, email);
                statement.setString(3, passwordEncoder.encode(RAW_PASSWORD));
                statement.execute();
            }
            connection.commit();
        }
    }

    @Test
    void afterMaxAttemptsEvenCorrectCredentialsAreLockedOut() throws Exception {
        attemptLogin("wrong-password").andExpect(status().isUnauthorized());
        attemptLogin("wrong-password").andExpect(status().isUnauthorized());

        attemptLogin(RAW_PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOO_MANY_ATTEMPTS"));
    }

    @Test
    void aSuccessfulLoginResetsThePreviousFailureCount() throws Exception {
        attemptLogin("wrong-password").andExpect(status().isUnauthorized());
        attemptLogin(RAW_PASSWORD).andExpect(status().isOk());

        // Without the reset, this would be failure #2 (max-attempts=2) and trip the lockout -
        // the next line proves it didn't.
        attemptLogin("wrong-password").andExpect(status().isUnauthorized());
        attemptLogin(RAW_PASSWORD).andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions attemptLogin(String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }
}
