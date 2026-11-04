# Database Code Conventions

[tech-stack.md](tech-stack.md) made the PostgreSQL + Flyway decision and [data-model-overview.md](../data-model-overview.md) listed the tables — this document defines **how migrations are written** and **what is set up from the start for performance**. These are decisions that are expensive to add later and cheap to set up from the start.

## Migration file rules — timestamp versioning, not sequential

**Naming: `V<yyyyMMddHHmm>__<description>.sql`** — e.g. `V202607171530__create_member_table.sql`, `V202607181045__add_member_consent_date.sql`.

**Why not sequential numbers (`V1`, `V2`, `V3`):** If multiple coder agents working in parallel write migrations at the same time on different branches (one for barber, one for fitness), both take "the next free number" (e.g. `V3`) on their own branch — when merged into the main branch a Flyway version conflict occurs and the CI/CD pipeline locks up. A timestamp (minute precision) makes this conflict structurally impossible; even if two agents write migrations in the same minute, it is an extremely rare collision and is resolved like a normal git merge conflict, not turning into a "who wins" argument over version numbers. Rationale/history: [ADR 0009](../adr/0009-flyway-timestamp-versioning.md).

- **A merged migration is never modified.** If a bug is found it is fixed with a new migration (with a new timestamp) — because Flyway does checksum verification, a modified migration that has already run in another environment will cause an error.
- Each migration contains a single logical change (a table + its related indexes, or adding a single field) — combining multiple topics in the same migration makes review harder.

## Mandatory index rule

**`tenant_id` is indexed on every table** — since every query is filtered by `tenant_id` per the isolation rule in [tenant-model.md](tenant-model.md), if this field is left without an index the whole system slows down as the number of tenants grows. This is not an "we'll optimize later" matter, it is part of every migration.

- Composite indexes for fields frequently queried together (e.g. `(tenant_id, member_id)` on the `membership` table, `(tenant_id, member_id, checked_in_at)` on `checkin_log`).
- Foreign key fields (`member_id`, `plan_id`, etc.) are also indexed — JPA does not do this automatically by default, it is added by hand in the migration.

## N+1 query prevention

- When fetching related data with JPA (e.g. a member's `membership` + `membership_plan` information), default lazy-loading can lead to a separate query for each record inside a loop (the N+1 problem).
- Rule: when writing a listing endpoint (e.g. member list + active membership status), the data is fetched in a single query using `JOIN FETCH` or `@EntityGraph` — this is added to [code-review-checklist.md](../workflow/code-review-checklist.md) as a "performance" item.

## Soft delete rule

[api-conventions.md](api-conventions.md) said "DELETE is mostly soft-delete" — the concrete rule: deletable tables (`member`, `service`, etc.) have a `deleted_at` (nullable timestamp) field, and the real `DELETE` SQL command is never used. Queries by default carry the `deleted_at IS NULL` filter (applied centrally through JPA `@Where` or a similar mechanism, not repeated in every repository).

## Mandatory tenant isolation (RLS)

In addition to the `tenant_id` index, every new table also enables Row-Level Security in the same migration — this is no longer a suggestion but a mandatory step per [ADR 0008](../adr/0008-row-level-security.md). Details and the migration template: [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md).

## Industry-specific flexible fields (JSONB metadata)

A new vertical (see [new-module-checklist.md](../workflow/new-module-checklist.md)) usually brings a few small fields of its own — such as "vehicle plate" for a car wash, "hair type note" for a barber. For such **fields specific to a single module and rarely queried**, instead of adding a new column to the main table each time, a `metadata JSONB` column is used:

```sql
ALTER TABLE appointment ADD COLUMN metadata JSONB DEFAULT '{}';
```

**When a column, when metadata JSONB:**

| Situation | Decision |
|---|---|
| The field needs querying/filtering/indexing (e.g. `staff_id`, `start_at`) | A real column |
| The field repeats in multiple modules (see [decision-tree.md](decision-tree.md)) | Consider moving to `core`, as a column |
| The field is specific to a single module, rarely filtered, for display purposes (e.g. "hair type note") | Inside `metadata` JSONB |

This distinction is an extension of the "core or module" logic in [decision-tree.md](decision-tree.md) — here it additionally answers the question "real column or flexible field". The content of `metadata` is documented in the module's own spec file (`docs/modules/<name>.md`); the schema is free but not arbitrary.

## Related files

- [tenant-model.md](tenant-model.md) — the tenant_id isolation rule
- [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md) — RLS implementation details
- [tech-stack.md](tech-stack.md) — the Flyway decision
- [decision-tree.md](decision-tree.md) — the core/module/metadata distinction logic
- [../workflow/code-review-checklist.md](../workflow/code-review-checklist.md)
