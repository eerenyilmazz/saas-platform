# Banned Code Patterns (Anti-Patterns)

[architecture-constraints.md](architecture-constraints.md) defines architectural boundaries; this file lists code patterns that **are not boundary violations but are still poor quality** — the concrete patterns the reviewer looks for in category 5 (business logic) of [code-review-checklist.md](../workflow/code-review-checklist.md).

## Banned patterns

| Pattern | Why it is banned | The right way |
|---|---|---|
| **God Class / Service** | A single class tries to do everything and exceeds the complexity budget in [architecture-constraints.md](architecture-constraints.md) | Split by responsibility (e.g. `MembershipService` separate, `MembershipNotificationService` separate) |
| **Repository Explosion** | A separate repository method for every small query; variations of the same query multiply | Shared query logic is written parametrically with `Specification`/a query builder |
| **Entity inside DTO** | A JPA entity is returned directly in an API response — the internal data model leaks outward, and a later entity change breaks the API | Always a separate `Response` DTO (see [code-style.md](code-style.md)) |
| **N+1 Query** | A list + related data is fetched with separate queries inside a loop | `JOIN FETCH`/`@EntityGraph` (see [database-conventions.md](database-conventions.md)) |
| **Business Logic Controller** | Making decisions with if/else inside a Controller | Business rules always in the `Service` layer (see [architecture-constraints.md](architecture-constraints.md)) |
| **Generic Repository** | A single `GenericRepository<T>` is used to try to manage all tables with the same pattern, forcing module-specific query needs | Each entity has its own `XxxRepository` interface (the Spring Data JPA standard) |
| **Utils Hell** | Unrelated helper methods pile up in a single `Utils`/`Helper` class | Each helper function goes into the narrow class/package it relates to; there is no "general" dumping-ground class |
| **Silent catch** | `catch (Exception e) {}` — the error is swallowed and leaves no trace | Every exception is either converted to the hierarchy in [error-handling.md](error-handling.md) or rethrown, never silently swallowed |

## How this list grows

If during review (human or agent) a recurring bad pattern is noticed that is not in the list above, it is added to this file as a new row — the list is not closed, it is an output of the [code-review-checklist.md](../workflow/code-review-checklist.md) process.

## Related files

- [architecture-constraints.md](architecture-constraints.md)
- [database-conventions.md](database-conventions.md) — N+1 details
- [error-handling.md](error-handling.md) — the right way to handle silent catch
