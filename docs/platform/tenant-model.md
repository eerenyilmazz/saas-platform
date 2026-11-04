# Tenant & Feature Flag Model

This document defines the basic mechanism `core` provides, on which **all modules** are built. When adding a new module it is enough to follow the rules here; there is no need to rewrite them.

## Tenant

Every company (a barber salon, a fitness gym, a construction company in the future, etc.) is a `tenant` record.

```
tenant
├── id
├── name
├── settings (JSON)   → active modules + per-module configuration live here
└── created_at
```

### settings JSON example

```json
{
  "modules": {
    "fitness":      { "enabled": true, "qr_token_ttl_seconds": 30 },
    "berber":       { "enabled": false },
    "construction": { "enabled": true, "project_based_inventory": true }
  },
  "branding": { "logo_url": "...", "color": "#2C3E6B" }
}
```

When a new module is added you can add its own configuration fields under `settings.modules.<module_name>` — the schema is flexible and requires no migration.

For how the `branding` field is applied on the frontend (color token, contrast check) see [theming.md](theming.md); for how `logo_url` is uploaded see [file-storage.md](file-storage.md).

## Data isolation rule

- Every table carries a `tenant_id` column.
- Every query is restricted by `tenant_id` — today this is provided by RLS alone (see [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md)); a shared application-layer filter is not currently in place per [ADR 0011](../adr/0011-app-layer-tenant-filter-deferred.md).
- A module can never access data outside its own tenant_id; this rule is verified in the test suite (at the core level).

## Feature flag check — implementation at the code level

Each module's controller class carries a class-level `@RequiresModule("fitness")` annotation. This is read by a single Spring interceptor defined in `core` — when a request arrives, the interceptor checks the relevant tenant's `settings.modules.fitness.enabled` field, and if it is off, it returns HTTP 404 without ever letting the request reach the controller.

```java
@RestController
@RequiresModule("fitness")
class FitnessCheckinController { ... }
```

- The advantage of this approach: `if (tenant.hasModule(...))` is not written over and over in every controller method; the check stays central in one place (the interceptor) — consistent with the principle of "shared logic is not rewritten" in [code-style.md](code-style.md).
- When adding a new module, the only thing to do is give the right `@RequiresModule` value to that module's controllers — this step is already implicitly included in [new-module-checklist.md](../workflow/new-module-checklist.md) (package name = module name = flag name).

### Fine-grained flag — a package-based feature restriction within a module

`@RequiresModule` turns a module entirely on/off, but the tiers in [pricing-packaging.md](pricing-packaging.md) sometimes restrict a sub-feature **within a module** — e.g. "QR check-in is not in the basic package, only in Premium Fitness". For this a separate, more fine-grained annotation is used:

```java
@PostMapping("/checkin")
@RequiresFeature(Feature.FITNESS_QR)
ResponseEntity<?> checkin(...) { ... }
```

- `@RequiresModule` asks at class level "is this module on"; `@RequiresFeature` asks at method level "is this **specific feature** in the tenant's package" — both share the same interceptor mechanism, only the `settings` field they check is at a different depth (`settings.modules.fitness.enabled` vs `settings.modules.fitness.features.qr_checkin`).
- The purpose of this separation: an agent adding a new premium feature **cannot forget** this annotation — embedding the check in code is more reliable than leaving the possibility of "forgetting" to a human/agent (see the principle "constraints are enforced by code/tests, not prose" in [architecture-constraints.md](architecture-constraints.md)).
- When adding a new `Feature`, a central list like the one in [event-catalog.md](event-catalog.md) (a `Feature` enum, defined in `core`) is used; each module does not invent its own string.

## Membership — a concept shared across modules

The `membership_plan` and `membership` tables live in `core` and represent the concept of "a member being enrolled in a package" regardless of industry:

| Industry | What "membership" corresponds to |
|---|---|
| fitness | A monthly/annual gym membership |
| berber | An appointment package / loyalty card (optional) |
| construction (example) | A project-based service contract |

When adding a new module, first ask: **what does "membership" correspond to in this industry?** If there is an answer, use `core.membership`; if not, define the module's own domain object (e.g. in construction "project" may be a separate concept and is not forced into the membership model).

## Billing — shared payment/invoicing

All modules call `core.billing` for payment/invoicing operations. No module writes its own payment/invoicing logic.
