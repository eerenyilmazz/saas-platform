-- The role that runs Flyway migrations (configured via spring.datasource.username) is a
-- PostgreSQL superuser in local/Docker setups (POSTGRES_USER always creates a superuser).
-- Superusers bypass Row-Level Security unconditionally - FORCE ROW LEVEL SECURITY has no
-- effect on them. If the application connected as that same superuser at runtime, every RLS
-- policy in this schema would be silently inert. So the application never runs as the
-- migration role: it connects as this separate, unprivileged role instead.
--
-- Local/dev password only - production credential handling is tracked in
-- docs/infra/environments.md and docs/open-questions.md, not solved here.
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'saas_app') THEN
        CREATE ROLE saas_app LOGIN PASSWORD 'saas_app_local_dev_only' NOSUPERUSER NOBYPASSRLS;
    END IF;

    -- current_database() used instead of a hardcoded name: this migration runs unmodified
    -- against both the docker-compose database (saas_platform) and each ephemeral
    -- Testcontainers database (an auto-generated name), per testing-strategy.md.
    EXECUTE format('GRANT CONNECT ON DATABASE %I TO saas_app', current_database());
END
$$;

GRANT USAGE ON SCHEMA public TO saas_app;
