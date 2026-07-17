package com.saas.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests needing a real PostgreSQL instance (testing-strategy.md).
 * Flyway runs migrations as the container's superuser; the application - and therefore any
 * repository/JPA call in a subclass - connects as saas_app instead. See
 * V202607171821__create_application_role.sql and tenant-isolation-enforcement.md for why these
 * must be two different roles: RLS is inert for a superuser connection, FORCE or not.
 *
 * Singleton container pattern (deliberately not @Testcontainers/@Container): that JUnit 5
 * extension stops the container after each test class, but this static field is shared by every
 * subclass (ApiApplicationTests, TenantRlsTest, ...) since it is declared here, not per-subclass.
 * The first class to finish would stop the container out from under the next one, which is
 * exactly what caused every TenantRlsTest connection to fail with SQLTransientConnectionException
 * when the full suite ran, despite TenantRlsTest passing on its own. Starting it once in a static
 * initializer and never stopping it (Testcontainers' Ryuk container reaps it when the JVM exits)
 * keeps one instance alive for the whole test run.
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        // Resolves ${app_role_password} in V202607171821__create_application_role.sql - must
        // match spring.datasource.password below, same role.
        registry.add("spring.flyway.placeholders.app_role_password", () -> "saas_app_local_dev_only");

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "saas_app");
        registry.add("spring.datasource.password", () -> "saas_app_local_dev_only");

        registry.add("app.jwt.secret", () -> "test-only-jwt-signing-secret-not-used-outside-this-suite");
    }
}
