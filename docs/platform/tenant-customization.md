# Tenant-Specific Requests — Extension Points

## Problem

In a real SaaS there is an unavoidable scenario: one of the first customers will say "add a special button/rule for us to the appointment screen". When a coder agent is told "write a special flow for customer X", the shortest (and most destructive) solution is to write `if (tenantId.equals("x-uuid")) { ... }` inside the business logic — this is now explicitly banned in [architecture-constraints.md](architecture-constraints.md), because within a few months the codebase turns into an unmaintainable spaghetti carrying a branch for every tenant.

## Decision: customization always goes through config, not code

Tenant-specific behavior goes through a **three-level** mechanism, and none of them writes `if (tenantId == ...)` directly into business logic:

### Level 1 — Feature flag (already exists)

The most common case: "this feature is on for some tenants and off for others." This is already solved with `@RequiresModule`/`@RequiresFeature` in [tenant-model.md](tenant-model.md) — no new mechanism is needed.

### Level 2 — Configuration parameter

If the behavior is the same but its **parameters** vary by tenant (e.g. "the appointment reminder should go out 48 hours before for this tenant, not 24"), it is added as a parameter to the `settings` JSON in [tenant-model.md](tenant-model.md):

```json
"fitness": { "enabled": true, "reminder_hours_before": 48 }
```

The code reads this parameter (`tenant.getSetting("reminder_hours_before", defaultValue: 24)`), and no `if (tenantId)` is written.

### Level 3 — Extension point (interface)

If the **behavior itself** (not a parameter, but the shape of the logic) differs by tenant (e.g. "for this tenant, membership renewal approval should be two-step"), this is solved with a **strategy interface**:

```java
interface MembershipRenewalStrategy {
    RenewalResult renew(Membership membership);
}

// core provides the default implementation
class DefaultRenewalStrategy implements MembershipRenewalStrategy { ... }

// if tenant-specific behavior is needed, a separate implementation is added
// which implementation is used is read from tenant.settings, not hardcoded in code
```

- These implementations live in `core` in a **separate package** from the business logic (`com.saas.core.membership.strategy`) — the actual `MembershipService` does not know which strategy it is, it only calls the interface.
- Adding a new strategy is done as a separate issue/PR with a process like [new-module-checklist.md](../workflow/new-module-checklist.md) — it is never embedded inside an existing `Service` class.

## Which level when

| Situation | Level |
|---|---|
| On/off for some tenants | 1 — feature flag |
| Same behavior, different number/threshold | 2 — config parameter |
| The flow/steps of the behavior differ | 3 — extension point (rare, used carefully) |

Level 3 is **the exception, not the rule** — not every tenant request automatically gives rise to a strategy interface; 1 and 2 are pushed first, and 3 is moved to only if the shape of the logic really changes (this decision is made deliberately, similar to [decision-tree.md](decision-tree.md), and not left to the agent to choose on its own — if unclear, it becomes an [ADR](../adr/_template.md) topic).

## Scope in the MVP

The Level 3 (extension point) mechanism is **not built as code** in Phases 0-1 — this document only describes which pattern we will follow when a need arises. When the first real tenant-specific request comes, this pattern is applied; it is not built speculatively in advance (early abstraction risk, see [modules/_template.md](../modules/_template.md)).

## Related files

- [architecture-constraints.md](architecture-constraints.md) — the `if (tenantId == ...)` ban
- [tenant-model.md](tenant-model.md) — the `settings` mechanism that Levels 1 and 2 rely on
- [decision-tree.md](decision-tree.md)
