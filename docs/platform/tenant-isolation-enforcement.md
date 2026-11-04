# Enforcing and Testing Tenant Isolation

[ADR 0008](../adr/0008-row-level-security.md) made the RLS decision — this document is its concrete counterpart at the code/test level.

## Defense layers — a single layer today

| Layer | Mechanism | Status |
|---|---|---|
| 1. Application | A shared repository/base class adds `tenant_id` to every query (see [tenant-model.md](tenant-model.md)) | **Deferred** — [ADR 0011](../adr/0011-app-layer-tenant-filter-deferred.md): it cannot be built reliably in the current transaction architecture (each repository call opens its own Session) |
| 2. Database | PostgreSQL RLS (see [ADR 0008](../adr/0008-row-level-security.md)) | Active — works even if the code is written wrong/forgotten |

Today, tenant isolation relies entirely on RLS — this is not fragile (the tests below prove it), but it is a single layer. If layer 1 is added (through a re-evaluation of ADR 0011), this table will be updated.

## Mandatory step in migrations

In addition to the migration rule in [database-conventions.md](database-conventions.md): **every new table that holds tenant data** also enables RLS in the same migration:

```sql
CREATE TABLE service (...);
CREATE INDEX idx_service_tenant_id ON service(tenant_id);
ALTER TABLE service ENABLE ROW LEVEL SECURITY;
ALTER TABLE service FORCE ROW LEVEL SECURITY;  -- see the "table owner" trap below, NEVER skipped
CREATE POLICY tenant_isolation ON service
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid);
```

These four lines (index + RLS enabled + RLS forced + policy) are a mandatory check item added to [new-module-checklist.md](../workflow/new-module-checklist.md) — the reviewer looks for this under category 1 (boundary violation) of [code-review-checklist.md](../workflow/code-review-checklist.md), and the PR is blocked if it is missing.

### Critical trap: without `FORCE`, RLS does not affect the table owner at all

`ENABLE ROW LEVEL SECURITY` is **not enough on its own** — by default PostgreSQL applies RLS to roles that are **not the table owner**. Because the application connects with the **same** database user that created the table with the migration (i.e. it is the table owner itself), if `FORCE ROW LEVEL SECURITY` is not added the policy **silently never works** — it throws no error, it simply does nothing. This was caught in Phase 1 by a real Testcontainers test (not by code review): the first migration written for the `tenant` table did not include `FORCE`, and the test, expecting "0 rows should be visible without a context", saw all the rows. It stayed that way until `FORCE` was added.

**Conclusion:** The `FORCE ROW LEVEL SECURITY` line is never skipped; "I already did ENABLE" is not enough.

### More critical trap: a superuser bypasses even `FORCE`

Even `FORCE` does nothing for **superuser roles** — in PostgreSQL a superuser bypasses RLS unconditionally and this behavior cannot be turned off. In a local/Docker setup, the role created with `POSTGRES_USER` is **always a superuser**. If the role that runs the migrations and the role the application connects with at runtime are the **same**, all of the RLS setup above (ENABLE + FORCE + policy) silently does nothing.

**Mandatory separation — two roles in every environment:**

| Role | Usage | Privilege |
|---|---|---|
| Migration role (e.g. `saas`) | Runs only the Flyway migrations (`spring.flyway.url/user/password`) | Superuser (needed for DDL, `CREATE ROLE`) |
| Application role (`saas_app`) | The application's runtime connection (`spring.datasource.url/username/password`) | `NOSUPERUSER NOBYPASSRLS`, a `SELECT/INSERT/UPDATE/DELETE` grant only on the tables it needs |

The `saas_app` role and its basic grants are created in the first migration (`V202607171821__create_application_role.sql`) — every new table must add `GRANT SELECT, INSERT, UPDATE, DELETE ON <table> TO saas_app;` to this role in its own migration, otherwise the application cannot access that table at all (independent of RLS, as a basic permission error).

These two findings (FORCE and superuser) came out of a single real Testcontainers test run — neither would have been noticed by code review, and both gave the appearance of "RLS is set up" while silently protecting nothing.

## Test — this is not a "nice to have", it is a mandatory gate

The "tenant isolation test" in [../workflow/testing-strategy.md](../workflow/testing-strategy.md) now covers both scenarios:

```java
@Test void applicationLayerFilteringWorks() {
    // normal flow: the repository queries with the correct tenant_id
}

@Test void rlsCannotBeBypassed() {
    // run a raw SQL query that DELIBERATELY skips the tenant_id filter
    // expected: only the rows of that session's tenant are still returned
    // (proof that RLS really works — tests not just that it exists, but that it does its job)
}
```

The second test is especially important: it verifies not that RLS is "enabled" but that it "actually prevents leakage" — if `ENABLE ROW LEVEL SECURITY` is forgotten in a migration, this test breaks.

## Deliberate exception for the platform_owner — two different flags, two different scenarios

There are **two separate scenarios** in which the platform_owner has to deliberately bypass RLS, and both are solved with a separate session flag:

| Flag | When active | Which tables | Example |
|---|---|---|---|
| `app.platform_context` | **Routine** tenant management operations the platform_owner performs through `com.saas.platform.*` (opening a new tenant, listing tenants) | The `tenant` table itself | `tenant` in [modules/core.md](../modules/core.md) — see the migration example in [ADR 0008](../adr/0008-row-level-security.md) |
| `app.support_mode` | The **temporary, reason-requiring** support mode in [support-access.md](support-access.md) | All other tenant-scoped tables (`member`, `appointment`, etc.) | When a tenant says "I can't find my member" |

Both write every access to [audit-log.md](audit-log.md) — not "no one can bypass it" but "it can be bypassed only through a logged/authorized path." `app.platform_context` asks for no reason since it is the platform_owner's daily work; `app.support_mode` always asks for a reason text (see [support-access.md](support-access.md)).

## Related files

- [../adr/0008-row-level-security.md](../adr/0008-row-level-security.md)
- [database-conventions.md](database-conventions.md)
- [../workflow/testing-strategy.md](../workflow/testing-strategy.md)
- [support-access.md](support-access.md)
