package com.saas.core.auth;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.core.auth.dto.LoginResponse;
import com.saas.testsupport.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real HTTP login flow (AuthController -> AuthenticationService -> JwtService)
 * against a tenant/user seeded with raw JDBC - proves the whole chain from credentials to a
 * signed token, not just JwtService in isolation.
 */
@AutoConfigureMockMvc
class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    private static final String RAW_PASSWORD = "correct-horse-battery-staple";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String email;

    @BeforeEach
    void seedTenantAndUser() throws Exception {
        email = "owner-" + UUID.randomUUID() + "@example.com";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            setPlatformContext(connection);

            UUID tenantId = insertTenant(connection);
            insertUser(connection, tenantId, email, passwordEncoder.encode(RAW_PASSWORD));

            connection.commit();
        }
    }

    @Test
    void validCredentialsReturnAnAccessTokenAndARefreshToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + RAW_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void refreshingWithAValidTokenRotatesBothTokens() throws Exception {
        LoginResponse initial = login();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + initial.refreshToken() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        LoginResponse rotated = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
        assertThat(rotated.refreshToken()).isNotEqualTo(initial.refreshToken());
    }

    @Test
    void refreshTokenCannotBeReusedAfterRotation() throws Exception {
        LoginResponse initial = login();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + initial.refreshToken() + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + initial.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    void unknownRefreshTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"not-a-real-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    void blankRefreshTokenFailsValidation() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void logoutRevokesTheRefreshTokenSoItCannotBeUsedAgain() throws Exception {
        LoginResponse initial = login();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + initial.refreshToken() + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + initial.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    void aProtectedPathWithNoTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/does-not-matter"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));
    }

    /**
     * JwtAuthenticationFilter throws (via JwtService) inside a servlet filter, not a controller -
     * GlobalExceptionHandler (a @RestControllerAdvice) never sees it, so without the filter's own
     * catch this would surface as a bare 500 instead of the standard error envelope.
     */
    @Test
    void aProtectedPathWithAnInvalidTokenIsRejectedWithTheStandardErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/does-not-matter")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_TOKEN"));
    }

    private LoginResponse login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + RAW_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
    }

    @Test
    void wrongPasswordIsRejectedWithTheStandardErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void unknownEmailIsRejectedTheSameWayAsAWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"" + RAW_PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void missingFieldsFailValidationWithTheStandardErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    private void setPlatformContext(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SET LOCAL app.platform_context = 'true'")) {
            statement.execute();
        }
    }

    private UUID insertTenant(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO tenant (name) VALUES (?) RETURNING id")) {
            statement.setString(1, "Auth Test Tenant");
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getObject("id", UUID.class);
            }
        }
    }

    private void insertUser(Connection connection, UUID tenantId, String email, String passwordHash) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO \"user\" (tenant_id, email, password_hash, role) VALUES (?, ?, ?, 'TENANT_ADMIN')")) {
            statement.setObject(1, tenantId);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.execute();
        }
    }
}
