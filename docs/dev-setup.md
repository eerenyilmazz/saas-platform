# Developer Setup Guide

This guide shows someone who clones the repo (a human or a coder agent) how to bring the project up on a local machine. It was **verified** against the real project skeleton in Phase 0 (see [roadmap/phases.md](roadmap/phases.md)).

## Requirements

| Tool | Version | Why |
|---|---|---|
| JDK | 21 (LTS) | Spring Boot backend |
| Node.js | 20+ | React frontend |
| Docker + Docker Compose | current | Local PostgreSQL |
| Git | current | — |

### If there is a Java/Node version conflict

Other projects on this machine may require different Java/Node versions — to isolate the right version for this project without breaking the global installation:

- **Node:** use [nvm](https://github.com/nvm-sh/nvm); the `.nvmrc` (`20`) in the repo root selects the right version automatically: `nvm use`
- **Java:** `brew install openjdk@21` (installed keg-only, does not touch the global `java` command) — `org.gradle.java.home` in `apps/api/gradle.properties` already points to the right path (`/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`), so no extra PATH setup is needed; `./gradlew` automatically uses the right JDK.

## First-time setup

```bash
git clone <repo-url>
cd saas-platform

# Bring up the local database (PostgreSQL, defined under infra/docker)
docker compose -f infra/docker/docker-compose.yml up -d

# Backend
cd apps/api
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
# → Flyway migrations run automatically, the API comes up at http://localhost:8080
# → verify: curl http://localhost:8080/actuator/health  →  {"status":"UP"}

# Frontend (in a separate terminal)
cd apps/web
nvm use   # selects Node 20 from .nvmrc
npm install
npm run dev
# → comes up at http://localhost:5173 (the Vite default)
```

> **Port note:** The local PostgreSQL runs on port `5433` (not the standard `5432`) — a native Postgres process belonging to another project was already using `5432` on this machine, so it was moved to avoid the conflict. If you have no such conflict on your own machine, you can revert `infra/docker/docker-compose.yml` and `apps/api/src/main/resources/application-local.properties` to `5432`; it is not required.

## Environment variables

- `SPRING_PROFILES_ACTIVE=local` — activates `application-local.properties` (local DB connection)
- `DB_PASSWORD` — optional; if not provided, the `local_dev_only` default is used (only in the local profile, see [infra/environments.md](infra/environments.md))

## Running the tests

```bash
cd apps/api
./gradlew test              # unit + integration tests (Testcontainers brings up a real PostgreSQL)
```

Details: [workflow/testing-strategy.md](workflow/testing-strategy.md)

## CI

`.github/workflows/ci.yml` — on every PR the backend (`./gradlew build`) and frontend (`npm run lint` + `npm run build`) run as separate jobs. **Note:** GitHub Actions workflow files must be under `.github/workflows/`, the only place GitHub scans — not under `infra/` (this had been documented incorrectly in [repo-structure.md](repo-structure.md) earlier and was corrected in Phase 0).

## Sample data (seed/fixture)

Not added yet — in Phase 1, once the core tables (auth/tenant/membership) exist, a seed migration that runs only in the `local` profile will be added. Right now the database is empty, with only Flyway's own schema history table.

## When working on a new module

Read the relevant module's spec file before writing code: `docs/modules/<module_name>.md`. The coder agent follows the same rule — see [workflow/agent-pipeline.md](workflow/agent-pipeline.md).
