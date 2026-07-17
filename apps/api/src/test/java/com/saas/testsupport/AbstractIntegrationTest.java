package com.saas.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests needing a real PostgreSQL instance (testing-strategy.md).
 * Flyway runs migrations as the container's superuser; the application - and therefore any
 * repository/JPA call in a subclass - connects as saas_app instead. See
 * V202607171821__create_application_role.sql and tenant-isolation-enforcement.md for why these
 * must be two different roles: RLS is inert for a superuser connection, FORCE or not.
 */
@Testcontainers
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "saas_app");
        registry.add("spring.datasource.password", () -> "saas_app_local_dev_only");
    }
}
