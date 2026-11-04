# Architecture Constraints (Inviolable Rules)

The files so far **described** the architecture; this file **enforces** it. The rules below are not suggestions but inviolable boundaries — if a coder agent violates one of them, the PR is rejected automatically under category 1 (boundary violation) of [code-review-checklist.md](../workflow/code-review-checklist.md), and it is not open to debate.

## NEVER do

- A `Controller` **NEVER** calls a `Repository` directly — it always goes through the `Service` layer (see the layer structure in [code-style.md](code-style.md)).
- A module (`modules/fitness`) **NEVER** imports another module's (`modules/berber`) entity/repository/service class (see the dependency rule in [repo-structure.md](../repo-structure.md), [bounded-context-matrix.md](bounded-context-matrix.md)).
- A shared "util" class **NEVER** contains business logic — a util holds only pure, stateless helper functions (e.g. date formatting yes, membership duration calculation no — the latter belongs to `MembershipService`).
- Circular dependencies are **NEVER** created (module A cannot depend on B while B depends on A) — this is already structurally prevented by the one-directional dependency graph in [repo-structure.md](../repo-structure.md); if a circular dependency **need arises**, it is a symptom of a design error, it is not "solved" with `@Lazy`, the module boundary is rethought.
- A hand-written mechanism outside the framework is **NEVER** built with Java reflection (Spring already uses reflection internally; application code does not repeat it).
- A `Controller` **NEVER** contains business rules (deciding with an if/else chain, calculations) — the controller only delegates the request/response to the `Service` (see "Business Logic Controller" in [anti-patterns.md](anti-patterns.md)).
- A class (especially a `@Service`/`@Component` in Spring's default **singleton scope**) **NEVER** has a mutable `field` that holds request/tenant-specific state — see "In-memory tenant isolation" below.
- A module **NEVER** defines an independent library version in its own `pom.xml`/`build.gradle` file — versions come only from the BOM in the root project (see [dependency-management.md](dependency-management.md)).
- Branching specific to a particular tenant such as `if (tenantId.equals("..."))` is **NEVER** written inside business logic (the `Service` layer) — see [tenant-customization.md](tenant-customization.md).

## In-memory tenant isolation (the layer RLS cannot protect)

The RLS in [ADR 0008](../adr/0008-row-level-security.md) protects data read from the database — but once data has been **legitimately** pulled from the DB into application memory (RAM), RLS no longer has any effect. The same thread/instance that handles the next tenant's request could gain access to the previous tenant's data through memory — this is a risk RLS **does not cover**, depending entirely on application code.

- **Static/global mutable state is banned:** a `static` `Map`/cache/singleton field is shared among all tenants in the background. If such a structure is **absolutely necessary** (e.g. a cache), the key must always start with `tenant_id` (`tenant_id + ":" + key`) — see [caching-strategy.md](caching-strategy.md).
- **Mutable fields in singleton beans are banned:** Spring's default `@Service` scope is singleton — assigning a value to a mutable field (such as "the current tenant") on this bean while processing a request causes that value to leak to another concurrent request (another tenant). Tenant/request-specific data is **always** carried via method parameters or `ThreadLocal`, never in a bean field.
- **If ThreadLocal is used, cleanup is mandatory:** Application server threads are **reused** (thread pool) — if the `ThreadLocal` contents are not cleared by hand when a request ends, **a different tenant** that gets the same thread on its next request could see the previous tenant's data. For every `ThreadLocal.set(...)` call, a `ThreadLocal.remove()` in a `finally` block is **mandatory** — this is checked under category 1 of [code-review-checklist.md](../workflow/code-review-checklist.md) (boundary violation, the most serious level).
- **Enforcement:** All three rules are added as a dedicated rule set in [static-analysis.md](static-analysis.md) (e.g. a check for "no mutable fields in singleton beans" with Checkstyle/ArchUnit) — code review alone is not relied upon, because this kind of bug is usually invisible in tests (the leak is not triggered in a single-tenant test scenario) and shows up only in production under concurrent multi-tenant traffic.

## Complexity budget (guideline numbers)

A numeric upper limit cuts off the "God Class" risk early, which agents otherwise let grow:

| Unit | Upper limit (guideline) |
|---|---|
| Method | ~30 lines |
| Class | ~500 lines |
| Service class (the main business logic of a single module) | ~200 lines — if exceeded, it probably carries multiple responsibilities and should be split |

These numbers follow the same logic as the cyclomatic complexity warning in [static-analysis.md](static-analysis.md): not a mechanical law but a "probably should be split" signal — but an agent cannot decide on its own whether something is "too long" without a clear number, so it is made concrete here.

## Enforcing these constraints

Wherever possible these rules are enforced by **automated tests, not prose** — an ArchUnit test suite (in `core`, scanning all modules) automatically verifies in CI the rules above such as "module A cannot import module B" and "a Controller cannot call a Repository", eliminating the risk of a human/reviewer agent missing it. This test suite is set up in Phase 0 (see [roadmap/phases.md](../roadmap/phases.md)).

## Related files

- [bounded-context-matrix.md](bounded-context-matrix.md) — which module can see which
- [anti-patterns.md](anti-patterns.md) — banned code patterns
- [static-analysis.md](static-analysis.md) — the automated enforcement layer
- [caching-strategy.md](caching-strategy.md) — the tenant_id rule for cache keys
- [dependency-management.md](dependency-management.md) — the BOM/centralized version requirement
- [tenant-customization.md](tenant-customization.md) — the right place to solve tenant-specific requests
- [../adr/0008-row-level-security.md](../adr/0008-row-level-security.md), [../adr/0009-flyway-timestamp-versioning.md](../adr/0009-flyway-timestamp-versioning.md)
