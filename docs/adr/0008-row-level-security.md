# ADR 0008: Tenant isolation is also enforced at the database level (RLS), in addition to the application layer

**Status:** Accepted
**Date:** 2026-07-17

## Context

[ADR 0003](0003-shared-db-tenant-id-isolation.md) decided on shared-DB + `tenant_id`. [tenant-model.md](../platform/tenant-model.md) said this isolation was "applied centrally in the shared repository/base class in core" — but that is a discipline **at the application layer only**. If an agent (or a human) forgets the `tenant_id` filter in a hand-written `@Query`, in a reporting query, or in an admin panel added later, nothing prevents it — one tenant's data could leak to another tenant. This is the shared-DB model's **single biggest risk**.

## Decision

**In addition to** (not instead of) filtering at the application layer, PostgreSQL's **Row-Level Security (RLS)** feature is enabled on all tables that hold tenant data. The database knows which tenant a connection was opened for and **never returns** rows outside that tenant, **no matter what query is written**.

## How it works (summary)

```sql
ALTER TABLE member ENABLE ROW LEVEL SECURITY;
ALTER TABLE member FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON member
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid);
```

- At the start of every request (in the auth interceptor, right after extracting `tenant_id` from the JWT), the backend runs `SET LOCAL app.current_tenant_id = '<tenant_id>'` on the database connection.
- From then on, **every** query written over that connection (hand-written, JPA-generated, or for reporting) automatically sees only that tenant's rows — forgetting to write `WHERE tenant_id = ...` is no longer a security hole, at worst it is a redundant clause.
- **The application never connects with the same role that runs the migrations** — see the Phase 1 implementation note below. Without this separation, the two lines above (`ENABLE`/`FORCE`) are useless.

## Consequences

- **Defense in depth:** The application layer (shared repository) is the primary defense and RLS is the secondary/backup defense — if one is bypassed, the other still holds.
- Cost: enabling RLS on every table is a migration step (see [database-conventions.md](../platform/database-conventions.md)) — this step is added as a mandatory step to [new-module-checklist.md](../workflow/new-module-checklist.md) when a new table is added.
- For scenarios where the `platform_owner` **deliberately** has to cross the tenant boundary, such as the support mode in [support-access.md](../platform/support-access.md), a separate, separately logged database role/bypass mechanism is defined — RLS anticipates this too: it is not "no one can ever see it" but "cannot see it by default, except through a deliberate, logged exception."
- Detailed implementation and test strategy: [platform/tenant-isolation-enforcement.md](../platform/tenant-isolation-enforcement.md).

## Revisit condition

None — this is a security decision that is not easy to reverse but is low in cost; it remains valid for as long as the shared-DB model is used.

## Historical context

- **Original incident/finding:** This decision came out of an external architecture review done before any code was written — an external AI evaluation (Gemini) pointed out that the design up to that point (the "the shared repository filters" wording in `tenant-model.md`) relied only on **application discipline**, and that the database itself offered no guarantee. Until then this had been implicitly accepted under the assumption that "of course agents won't forget the `tenant_id` filter."
- **Rejected alternatives:** Hibernate `@Filter` (works only for access through JPA and leaves raw SQL/reporting queries unprotected — found insufficient for that reason). Relying on code review discipline alone (open to human/agent error, a single-layer defense).
- **Failure scenario:** If an agent forgot the `tenant_id` filter in a reporting endpoint or a hand-written `@Query` (statistically this will happen sooner or later, especially in an environment where many agents work in parallel), Tenant A's member list/appointments could have leaked to Tenant B — this was the one big risk accepted with the shared-DB model, and without RLS it was **only a matter of time**.

## Phase 1 implementation note — a second finding, while writing the code

When this decision was actually put into code in Phase 1 (the first `tenant` table + RLS test), **`FORCE ROW LEVEL SECURITY` alone turned out to be insufficient**: in PostgreSQL, superuser roles bypass RLS unconditionally, `FORCE` included. In a local/Docker setup, the role created with `POSTGRES_USER` (the role that runs the migrations) is always a superuser — had the application also connected with this role at runtime, **none** of the `ENABLE`/`FORCE`/`CREATE POLICY` lines above would have done anything, silently.

**Fix:** The role that runs the migrations (a superuser, configured separately via `spring.flyway.*`) and the role the application connects with at runtime (`saas_app`, `NOSUPERUSER NOBYPASSRLS`, via `spring.datasource.*`) were **strictly separated**. This separation was added as a mandatory step to [tenant-isolation-enforcement.md](../platform/tenant-isolation-enforcement.md) — a Testcontainers test (`rlsCannotBeBypassed`) caught this bug, not a code review but an actual test run.
