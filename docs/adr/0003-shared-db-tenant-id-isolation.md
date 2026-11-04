# ADR 0003: Shared DB + tenant_id isolation

**Status:** Accepted
**Date:** 2026-07-17

## Context

Three options were evaluated for multi-tenancy: (1) shared DB + a `tenant_id` column, (2) schema-per-tenant, (3) database-per-tenant.

## Decision

Isolate data using a shared DB with a `tenant_id` column on every table.

## Consequences

- The lowest-cost and easiest-to-manage option, suitable for the early stage with small/medium scale.
- Risk: data isolation must be enforced strictly at the code level — this risk is mitigated by applying the `tenant_id` filter centrally through the shared repository/base class in [core.md](../modules/core.md) and by the mandatory tenant isolation test in [testing-strategy.md](../workflow/testing-strategy.md).
- Revisit condition: if a large/enterprise customer says "my data must stay on my own server", a move to database-per-tenant for that tenant would be considered — the architecture is not closed to it, it is just not needed for the MVP.
