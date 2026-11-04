# ADR 0004: PostgreSQL + Flyway

**Status:** Accepted
**Date:** 2026-07-17

## Context

We needed to decide on the database engine and the migration tool. Alternatives: MySQL, PostgreSQL; for migrations, Flyway or Liquibase.

## Decision

PostgreSQL + Flyway.

## Consequences

- PostgreSQL's JSONB support is a direct fit for flexible/schemaless fields such as `tenant.settings` (see [tenant-model.md](../platform/tenant-model.md)) — MySQL is weaker here.
- Spring Data JPA integration with PostgreSQL is mature and widespread.
- Flyway uses SQL-based migration files — compared with Liquibase's XML/YAML abstraction it has a lower learning curve for the team and integrates automatically with Spring Boot.
