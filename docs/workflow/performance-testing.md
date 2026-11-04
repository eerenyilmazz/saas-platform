# Performance/Load Testing

The four levels in [testing-strategy.md](testing-strategy.md) (unit/integration/tenant isolation/e2e) all tested **correctness** — "does it give the right result". This document answers a different question: **"how many concurrent tenants/requests can it handle"**.

## MVP decision: no early investment, a single threshold test is enough

Building a comprehensive load-testing infrastructure without real usage data (how many tenants, how much traffic) would be speculative — the same rationale as the early-abstraction warning in [modules/_template.md](../modules/_template.md). Instead:

- **A single baseline load test is run at the end of Phase 1** (suggestion: a lightweight tool such as k6 or Gatling, no separate infrastructure needed) — the goal: whether a rough reference load such as "50 concurrent tenants, 10 requests per second" is handled without problems.
- This test **does not run on every PR in CI** (slow and costly) — it is triggered manually only after major architectural changes (e.g. a new cache layer, [caching-strategy.md](../platform/caching-strategy.md)).

## What to monitor (in priority order)

1. `tenant_id`-based query latency — whether the mandatory indexes in [database-conventions.md](../platform/database-conventions.md) really work, and whether query time grows linearly as the number of tenants increases
2. The performance impact of RLS — the policy check in [tenant-isolation-enforcement.md](../platform/tenant-isolation-enforcement.md) adds overhead to every query; verify that this overhead is acceptable
3. The QR check-in endpoint — the fitness module's busiest endpoint; check that the idempotency check in [concurrency-and-idempotency.md](../platform/concurrency-and-idempotency.md) does not create a bottleneck

## When to take it seriously

As the real number of tenants grows (see the ~10-tenant threshold in [onboarding.md](../platform/onboarding.md)), these tests become regular, and concrete threshold values are defined (once the ToS/SLA in item 9 of [open-questions.md](../open-questions.md) is settled). No numeric SLA target (e.g. "p99 200ms") is set in the MVP — without real traffic this number would be meaningless.

## Related files

- [testing-strategy.md](testing-strategy.md) — correctness tests (what this file does not cover)
- [../infra/observability.md](../infra/observability.md) — monitoring real performance in production
