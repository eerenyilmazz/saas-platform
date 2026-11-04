# Roles & Access Hierarchy

Earlier documents had only tenant-level roles (admin, staff). What was missing: a role/layer for **you, the platform owner**, who can manage all tenants. A three-level hierarchy:

```
platform_owner   → your team, sees/manages all tenants
   └── tenant_admin    → salon owner, manages only their own tenant
         └── tenant_staff  → salon employee, limited operation permissions
```

## Permission table

| Operation | platform_owner | tenant_admin | tenant_staff |
|---|---|---|---|
| Create a new tenant | ✅ | ❌ | ❌ |
| Toggle a tenant's modules on/off | ✅ | ❌ (requests it, the platform_owner applies it) | ❌ |
| View platform subscription / invoices | ✅ (all tenants) | ✅ (only their own payment) | ❌ |
| Add/edit members (within the tenant) | ✅ | ✅ | ✅ (add only, no delete) |
| Define membership packages/prices | ❌ | ✅ | ❌ |
| Check-in/appointment approval (daily operations) | ❌ | ✅ | ✅ |
| View another tenant's data | ✅ (only with the logged "support mode", see [support-access.md](support-access.md)) | ❌ | ❌ |

## Technical implementation

- The `platform_owner` role is the only role that can access endpoints in the `com.saas.platform.*` packages.
- `tenant_admin` and `tenant_staff` access endpoints under `com.saas.core.*` and `com.saas.modules.*` **only within their own `tenant_id`** — this check is mandatory on every request (see [tenant-model.md](tenant-model.md)).
- The `platform_owner` has no `tenant_id` (or is tied to a special system tenant) — it is the only role that can run cross-tenant queries, which is why this role's endpoints are kept in a separate package (`platform`), not mixed with core.

## Platform admin panel — in the MVP?

**MVP decision: no.** The first pilot tenants (1-2 salons) are added to the database directly with a seed script, and no separate interface is needed. As the number of tenants grows (see [onboarding.md](onboarding.md)), a web interface (a `/platform` route inside `apps/web`, protected by the `platform_owner` role) is added — see [roadmap/versions.md](../roadmap/versions.md) v1.2.

## Related files

- [tenant-model.md](tenant-model.md) — tenant isolation
- [billing-model.md](billing-model.md) — the platform-billing that the platform_owner sees
- [onboarding.md](onboarding.md) — how a new tenant is opened
- [support-access.md](support-access.md) — the mechanism for the platform_owner to access tenant data for support purposes
- [audit-log.md](audit-log.md) — how every authorized operation in this table is tracked
