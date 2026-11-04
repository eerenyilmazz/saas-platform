# Repo Structure

A single monorepo with folder-based module separation. Every module depends on `core` and not on each other — if one breaks the other is not affected, and adding a new module means opening a new folder without changing existing code.

```
saas-platform/
├── .github/
│   └── workflows/             # the REAL CI/CD definitions — GitHub scans only this folder
├── apps/
│   ├── api/                   # Spring Boot — a single Gradle project, the runnable application that combines all modules
│   │   └── src/main/java/com/saas/
│   │       ├── platform/      # the SaaS owner's own operating layer — tenant provisioning, platform billing
│   │       ├── core/          # the shared layer every tenant uses: auth, tenant, membership engine, tenant-billing, notifications
│   │       └── modules/
│   │           ├── berber/    # appointment type, service duration, business rules
│   │           └── fitness/   # QR check-in/check-out, PT appointments, freezing
│   └── web/                   # React — admin panel + member UI (wrapped with Capacitor)
├── infra/
│   ├── docker/                # local development environment (db + api + web come up with a single command)
│   └── environments/          # staging/production environment definitions
└── docs/
    └── adr/                   # architecture decision records (Architecture Decision Records)
```

> **Decision settled in Phase 0:** `platform`/`core`/`modules` are not separate Gradle projects (physical modules) but Java packages inside **a single Gradle project** (`apps/api`). Instead of physical module isolation, the ArchUnit test suite in [architecture-constraints.md](platform/architecture-constraints.md) catches boundary violations in the post-compile test stage — this fits the modular monolith philosophy better (a single deploy unit, low operational overhead). Package names match the `com.saas.*` scheme in [package-naming.md](package-naming.md) one to one.

## Why this separation

- `apps/` holds the runnable applications, and the Java packages inside `apps/api` separate the logic.
- `com.saas.platform` and `com.saas.core` are not mixed: **platform** = you (the SaaS owner) use it when managing tenants, **core** = every tenant uses it when managing its own business. Details: [platform/roles-and-access.md](platform/roles-and-access.md)
- `docs/adr/` is small but important: it records decisions like "why Spring Boot", "why a monorepo" for posterity — the full list: [adr/](adr/)
- Adding a new vertical (e.g. a beauty salon) = a new package under `com.saas.modules`, without changing `core`.

## Dependency rule

```
apps/api          → com.saas.platform, com.saas.core, com.saas.modules.*  (all in the same Gradle project)
apps/web          → (consumes all APIs over HTTP, no direct dependency)
com.saas.platform → com.saas.core (allowed — platform is a management layer above core)
modules/*         → com.saas.core (allowed)
modules/*         → modules/* (forbidden — modules cannot depend on each other, enforced by ArchUnit)
modules/*         → com.saas.platform (forbidden — modules know nothing of the platform layer)
```
