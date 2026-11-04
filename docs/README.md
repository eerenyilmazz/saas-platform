# Modular SaaS — Documentation Index

> **If you are a new agent/session, read [kickoff.md](kickoff.md) first.** It explains step by step how to ask the open questions to the user and how to move on to Phase 0 — look there before scanning this README.

A SaaS project that starts with the core + barber + fitness modules, is built with Spring Boot in a monorepo, and proceeds with a git-connected agent workflow.

Every file in this folder is an independent spec — an agent (coder/reviewer/tester) can read only the relevant file before starting a task and work within that scope.

The platform is designed to be industry-independent: barber + fitness today, and when you want to add a new vertical such as construction/hair salon/car wash tomorrow, the architecture is not rethought — you follow the steps in [workflow/new-module-checklist.md](workflow/new-module-checklist.md).

## Table of Contents

**Getting started**
- [kickoff.md](kickoff.md) — the first file a new agent/session reads: how to ask the open questions, where to record the answers, how to move on to Phase 0

**Terms and code style**
- [glossary.md](glossary.md) — glossary (the exact definition of concepts such as `tenant`/`user`/`member` + banned words + rationale)
- [platform/code-style.md](platform/code-style.md) — class/table naming, commit format

**Documentation meta-management (keeping information consistent over time)**
- [docs-versioning.md](docs-versioning.md) — spec versioning based on git commit SHA, context drift detection
- [docs-dependency-map.md](docs-dependency-map.md) — which files are affected when a core file changes

**Architecture**
- [repo-structure.md](repo-structure.md) — monorepo folder structure (`platform` / `core` / `modules`)
- [package-naming.md](package-naming.md) — Java package naming scheme
- [data-model-overview.md](data-model-overview.md) — a one-page reference of all tables + module capability matrix

**Platform (the SaaS's own operating layer)**
- [platform/security-overview.md](platform/security-overview.md) — a one-page index of security spread across 8 files + a checklist for every PR
- [platform/architecture-constraints.md](platform/architecture-constraints.md) — inviolable architecture rules (the NEVER list), complexity budget, in-memory tenant isolation (the layer RLS cannot protect)
- [platform/tenant-customization.md](platform/tenant-customization.md) — a 3-level solution for tenant-specific requests (feature flag / config / extension point), the right counterpart to the `if (tenantId==...)` ban
- [platform/bounded-context-matrix.md](platform/bounded-context-matrix.md) — which module can see which, package ownership
- [platform/anti-patterns.md](platform/anti-patterns.md) — banned code patterns (God Class, N+1, Entity inside DTO, etc.)
- [platform/decision-tree.md](platform/decision-tree.md) — a decision tree for whether something new belongs in core, a module, a feature flag, or a new module
- [platform/event-catalog.md](platform/event-catalog.md) — standard domain event names (MemberCreated, PaymentReceivedEvent, etc.)
- [platform/domain-invariants.md](platform/domain-invariants.md) — invariants of the business rules that can never be violated (INV-1..8)
- [platform/tenant-model.md](platform/tenant-model.md) — tenant, feature flags (`@RequiresModule`/`@RequiresFeature`), shared use of membership/billing across modules
- [platform/tenant-isolation-enforcement.md](platform/tenant-isolation-enforcement.md) — enforcing RLS at the code/migration/test level
- [platform/roles-and-access.md](platform/roles-and-access.md) — the platform_owner / tenant_admin / tenant_staff hierarchy
- [platform/support-access.md](platform/support-access.md) — the platform_owner's logged, time-limited support access (impersonation)
- [platform/billing-model.md](platform/billing-model.md) — the distinction between platform-billing (your revenue) and tenant-billing (the tenant's revenue)
- [platform/pricing-packaging.md](platform/pricing-packaging.md) — subscription package structure (without figures)
- [platform/onboarding.md](platform/onboarding.md) — how a new tenant is opened (MVP: manual)
- [platform/tenant-offboarding.md](platform/tenant-offboarding.md) — data export when a tenant leaves, grace period, KVKK data portability
- [platform/accessibility.md](platform/accessibility.md) — minimum WCAG AA commitment, keyboard navigation, semantic HTML
- [platform/data-import.md](platform/data-import.md) — importing existing Excel/list data into the system
- [platform/notifications.md](platform/notifications.md) — SMS/email notification infrastructure
- [platform/kvkk-checklist.md](platform/kvkk-checklist.md) — data protection checklist (KVKK is Turkey's personal data protection law)
- [platform/audit-log.md](platform/audit-log.md) — who changed what and when on critical tables
- [platform/security.md](platform/security.md) — rate limiting, JWT token lifetime, input validation
- [platform/mobile-compatibility.md](platform/mobile-compatibility.md) — compatibility strategy between backend deploy speed and mobile store review speed
- [platform/tech-stack.md](platform/tech-stack.md) — DB, migration, framework decisions
- [platform/api-conventions.md](platform/api-conventions.md) — REST rules, error format
- [platform/error-handling.md](platform/error-handling.md) — exception hierarchy, producing the error format in code
- [platform/database-conventions.md](platform/database-conventions.md) — migration rules, mandatory indexes, N+1/soft-delete
- [platform/concurrency-and-idempotency.md](platform/concurrency-and-idempotency.md) — duplicate records, race conditions, idempotency keys
- [platform/api-documentation.md](platform/api-documentation.md) — automatic OpenAPI generation, frontend type synchronization
- [platform/dependency-management.md](platform/dependency-management.md) — rules for adding third-party libraries + mandatory centralized version management (BOM, preventing jar hell)
- [platform/caching-strategy.md](platform/caching-strategy.md) — caching tenants/feature flags, invalidation, and the mandatory tenant_id key rule
- [platform/external-integration-resilience.md](platform/external-integration-resilience.md) — timeouts/circuit breakers/retries for external services such as SMS/payment, the rule of not blocking the main flow
- [platform/file-storage.md](platform/file-storage.md) — logo/image upload (object storage, presigned URLs)
- [platform/localization.md](platform/localization.md) — concrete implementation of language support (date/number format, plural patterns)
- [platform/theming.md](platform/theming.md) — light/dark mode + tenant brand color/logo system
- [platform/static-analysis.md](platform/static-analysis.md) — code quality/security static analysis (SonarQube/CodeQL), a layer separate from lint

**Modules**
- [modules/_template.md](modules/_template.md) — the template to copy when adding a new module
- [modules/core.md](modules/core.md) — core module spec (auth, tenant, membership)
- [modules/fitness.md](modules/fitness.md) — fitness module spec (QR check-in, PT, QR token security)
- [modules/berber.md](modules/berber.md) — barber (`berber`) module spec (appointments, services)

**Workflow**
- [workflow/issue-management.md](workflow/issue-management.md) — issue types, labels, template, issue→branch→PR link
- [workflow/branch-strategy.md](workflow/branch-strategy.md) — branch naming and PR flow
- [workflow/pr-and-definition-of-done.md](workflow/pr-and-definition-of-done.md) — PR template, the criteria for a piece of work to count as "done"
- [workflow/code-review-checklist.md](workflow/code-review-checklist.md) — the reviewer's (agent or human) concrete 6-category checklist
- [workflow/agent-pipeline.md](workflow/agent-pipeline.md) — git-connected agent roles
- [workflow/new-module-checklist.md](workflow/new-module-checklist.md) — the fixed process to follow when adding a new industry/vertical (9 steps)
- [workflow/testing-strategy.md](workflow/testing-strategy.md) — test pyramid, the levels the tester agent runs
- [workflow/performance-testing.md](workflow/performance-testing.md) — load/scale testing (not correctness, but the question "how many concurrent tenants can it handle")
- [workflow/example-feature-walkthrough.md](workflow/example-feature-walkthrough.md) — an end-to-end example of the "Create Member" feature from issue to PR (a one-glance reference for agents)
- [workflow/change-impact-analysis.md](workflow/change-impact-analysis.md) — the process for changing an existing concept (not adding a new one)
- [workflow/ai-failure-recovery.md](workflow/ai-failure-recovery.md) — the procedure to follow when a faulty AI change gets merged

**Infra**
- [infra/environments.md](infra/environments.md) — local/staging/production, secrets management
- [infra/backup-recovery.md](infra/backup-recovery.md) — backups, RPO/RTO targets
- [infra/observability.md](infra/observability.md) — logging, error tracking, uptime/alerting

**Getting started**
- [dev-setup.md](dev-setup.md) — steps to bring up the repo on a local machine

**Roadmap**
- [roadmap/versions.md](roadmap/versions.md) — release roadmap
- [roadmap/phases.md](roadmap/phases.md) — step-by-step starting order (Phases 0–6)
- [roadmap/pending-work.md](roadmap/pending-work.md) — items waiting on an open business decision (along with which open-questions.md item each depends on)

**Rationale for decisions**
- [adr/](adr/) — the context and rationale for the decisions on monorepo, Spring Boot, shared-DB+tenant_id, PostgreSQL+Flyway, Capacitor, i18n, UTC timestamps, Row-Level Security, and Flyway timestamp versioning (the ADR template also carries the "historical context/original incident/rejected alternatives" fields)

**Open questions**
- [open-questions.md](open-questions.md) — 10 items that are not engineering decisions and that you need to answer (domain name, payment/SMS provider, onboarding model, KVKK/ToS approval, pricing, hosting, e-Invoice)

## General rules

- Every module depends only on `core`, not on other modules; and not on `platform` at all.
- Changes touching shared code (`core`) are reviewed with priority.
- Branches are kept short-lived and stay within module boundaries.
- Adding a new module never changes the existing architecture — it only adds a new folder/files.
- Platform-billing (your revenue) and tenant-billing (the tenant's revenue) are never mixed in the same table/code.
- On critical tables (membership, payment, role), every change is written to audit_log; the platform_owner's access to tenant data is always logged and time-limited.
