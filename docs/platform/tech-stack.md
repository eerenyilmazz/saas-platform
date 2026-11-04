# Technology Stack Decisions

This document settles technical choices that were discussed earlier but not committed to in writing. With rationales — for detailed rationale see the relevant [adr/](../adr/) file.

| Layer | Choice | Short rationale |
|---|---|---|
| Backend | Spring Boot 4.1.0, Java 21 (LTS) | A mature ecosystem for a modular monolith + multi-tenancy (see [adr/0002](../adr/0002-spring-boot-modular-monolith.md)) |
| Build tool | Gradle 9.x (with wrapper) | The Spring Initializr default; thanks to `./gradlew` no global Gradle installation is needed |
| Database | PostgreSQL 16 | JSONB support is ideal for `tenant.settings`, seamless integration with Spring Data JPA, open source (see [adr/0004](../adr/0004-postgresql-flyway.md)) |
| Migration | Flyway (timestamp versioning) | SQL-based, less abstraction/lower learning curve than Liquibase, integrates automatically with Spring Boot — naming details in [adr/0009](../adr/0009-flyway-timestamp-versioning.md) |
| Frontend | React + TypeScript (Vite) | A wide ecosystem, directly compatible with Capacitor mobile wrapping |
| Mobile | Capacitor | iOS + Android from the same web codebase (see [adr/0005](../adr/0005-capacitor-mobile-wrapping.md)) |
| CI/CD | GitHub Actions (`.github/workflows/`) | The repo will be on GitHub anyway so no additional tool is needed, and the agent pipeline is triggered from the same place |
| Containerization | Docker (local development) | PostgreSQL comes up with a single command under `infra/docker` |

## Why decide this early

This layer has to be fixed before module development starts — every module shares the same DB/migration/CI infrastructure, and if the decision changes midway all modules are affected. That is why it is part of Phase 0 (see [roadmap/phases.md](../roadmap/phases.md)).

## Corrections verified in a real environment in Phase 0

- **Spring Boot version:** The `3.4.0` requested from Spring Initializr was no longer supported, so it was resolved to the current `4.1.0` — the document was updated accordingly (see [adr/0002](../adr/0002-spring-boot-modular-monolith.md) was not revisited, since it was not the framework choice but only the version number that changed).
- **CI file location:** `repo-structure.md` initially said `infra/github-actions/` — GitHub scans only `.github/workflows/`, so this was a documentation error and was corrected.
- **`packages/*` is not a physical folder:** `platform`/`core`/`modules` were set up not as separate Gradle projects but as Java packages inside the single `apps/api` Gradle project — details in [repo-structure.md](../repo-structure.md).
