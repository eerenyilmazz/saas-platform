package com.saas.core.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.saas.core.tenant.TenantAwareDataSource;

/**
 * Builds the app's DataSource (spring.datasource.* - the saas_app connection, see
 * V202607171821__create_application_role.sql) directly from DataSourceProperties instead of
 * letting Spring Boot's own DataSourceAutoConfiguration produce it, then wraps it in
 * TenantAwareDataSource so every connection JPA/JdbcTemplate borrows carries the current
 * request's tenant context. Building it here (@ConditionalOnMissingBean backs the
 * auto-configured one off) rather than wrapping the auto-configured bean avoids a circular
 * dependency: FlywayAutoConfiguration also looks up "the" DataSource bean by type for its own
 * ordering, and a wrapper bean that both provides and depends on that same type deadlocks against
 * that lookup during context startup.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return new TenantAwareDataSource(dataSourceProperties.initializeDataSourceBuilder().build());
    }
}
