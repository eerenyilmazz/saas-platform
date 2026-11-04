# ADR 0011: Application-layer tenant filter deferred, RLS as the single layer

**Status:** Accepted
**Date:** 2026-07-18

## Context

[tenant-model.md](../platform/tenant-model.md) and [tenant-isolation-enforcement.md](../platform/tenant-isolation-enforcement.md) described a "two-layer defense": (1) a shared repository/base class in `core` adds `tenant_id` to every query, (2) RLS. In the full Phase 1 audit it was found that layer 1 was never coded — all repositories (`UserRepository`, `MemberRepository`, `MembershipRepository`, ...) are plain `JpaRepository<T, ID>`, with no shared base class.

Hibernate's `@Filter` mechanism (`@FilterDef`+`@Filter`, session-based, automatically adds a `tenant_id` condition to all queries) is the standard solution for this. But in this project, because of `spring.jpa.open-in-view=false` + each repository call opening its own separate transaction (an architecture explained repeatedly in the comments of `TenantAwareDataSource`/`AuthenticationServiceImpl`), there is no single stable hook where we could say "reliably enable the filter at the start of every transaction": Spring's shared `EntityManager` proxy opens its own Session on each repository call, and enabling the filter elsewhere per request (e.g. in a servlet filter) would apply it not to the Session that call uses but to a different Session that gets thrown away.

## Decision

Layer 1 (the application-layer tenant filter) is **not being built** for now. Tenant isolation is currently provided solely by RLS (layer 2) — a mechanism that genuinely works and has been proven repeatedly by tests such as `TenantRlsTest`/`MembershipModelTest`/`TenantAuditTest`. `tenant-model.md` and `tenant-isolation-enforcement.md` were updated to reflect the current state.

## Consequences

- There is no regression in security: RLS was already the layer that works "even if the code is written wrong/forgotten", and it always provided the real guarantee.
- If the transaction architecture changes in the future (e.g. a single `EntityManager` is opened per request, or a `@Transactional` boundary wrapping the whole request is added), enabling the Hibernate `@Filter` reliably becomes possible — this ADR will be re-evaluated at that point.

## Historical context

- **Original incident/finding:** In the full Phase 1 audit, the "shared repository/base class" that the docs claimed could not be found anywhere.
- **Rejected alternatives:** A Hibernate `@Filter` + a custom base class wrapping `SimpleJpaRepository` via `repositoryBaseClass` was considered — but this wraps only `SimpleJpaRepository`'s own methods (`findById`, `findAll`); Spring Data's derived query methods (e.g. `UserRepository.findByEmail`) go straight to the `EntityManager` and never see this base class. The result would have been a misleading partial solution of the "works for some queries, not for others" kind — worse than having nothing, because it would give the impression that it exists.
- **Failure scenario:** Had this ADR not been written, someone could have said "the docs say so", relied on a protection that does not exist, and loosened RLS.
