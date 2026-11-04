# Tenant Offboarding — Leaving and Data Export

[data-import.md](data-import.md) described how a tenant **comes in** — this document defines the opposite: what happens when a tenant cancels its subscription/leaves.

## Why it is needed

This was a symmetric gap: onboarding existed, offboarding did not. There is also a requirement — KVKK's right to data portability requires that the tenant be able to take its own data (member list, historical records) and go; silently deleting it or keeping it indefinitely are both problematic.

## Flow

```
1) The tenant/platform_owner initiates the subscription cancellation
        ↓
2) Grace period (suggestion: 30 days) — during this time the tenant can still log in and view their data
        ↓
3) Export — the tenant can download their own data as CSV (the reverse direction of data-import.md:
   member, membership, tenant_payment, checkin_log/appointment — everything belonging to their own tenant_id)
        ↓
4) When the grace period ends → access is cut (user accounts are deactivated)
        ↓
5) Data retention period (suggestion: 90 days, except for legal obligations) → then anonymization/deletion
```

## MVP decision: manual, supported by the platform_owner

The same logic as in [onboarding.md](onboarding.md): there is no self-service "close my account" flow in the MVP. The tenant passes the cancellation request to you, and you manually run the grace period and the export with an export script that is the reverse of the import script in [data-import.md](data-import.md).

## Data model

- A new value for the `tenant.status` field: `cancelled` (see [tenant-model.md](tenant-model.md)) — if not `active`, no new logins/operations can be made, but the data can still be read during the grace period.
- Deletion is subject to the soft-delete rule in [database-conventions.md](database-conventions.md) — `deleted_at` is set, and a real `DELETE` does not run (financial record-keeping requirement, see [kvkk-checklist.md](kvkk-checklist.md)).

## Relationship with platform-billing

The cancellation sets `platform_subscription.status` in [billing-model.md](billing-model.md) to `cancelled` — this means your revenue stops, and it is not mixed with the tenant's own data (tenant-billing).

## Out of scope (not in the MVP)

- A self-service "close my account" button — considered together with the platform admin panel (see [roadmap/phases.md](../roadmap/phases.md) Phase 6).
- Enriching the automatic export format (CSV only, no other format).

## Related files

- [data-import.md](data-import.md) — the symmetric counterpart of this file
- [kvkk-checklist.md](kvkk-checklist.md) — data portability/deletion rights
- [billing-model.md](billing-model.md) — cancelling platform_subscription
