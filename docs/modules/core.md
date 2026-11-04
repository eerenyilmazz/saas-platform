# Module: core

**Package:** `com.saas.core.*`
**Dependency:** none (every other module depends on this)
**Priority:** 1 — the first module to be developed

## Responsibilities

- **Tenant model and data isolation** — `tenant_id` on every table, a mandatory filter on every query
- **Auth + role management** — login with Spring Security, roles: `tenant_admin`, `tenant_staff` (platform_owner is a separate layer, see [platform/roles-and-access.md](../platform/roles-and-access.md))
- **Membership/package engine** — all modules (fitness, barber) use this engine; it is not module-specific
- **Tenant-billing** — tracking of payments/invoices the tenant receives from its own customers (not to be mixed with the platform's own revenue, see [platform/billing-model.md](../platform/billing-model.md))
- **Notifications** — a shared interface for sending SMS/email (see [platform/notifications.md](../platform/notifications.md))
- **Feature flag system** — which module is enabled for which tenant, kept in the tenant `settings` field

## Data model (draft)

- `tenant` — id, name, active_modules (JSON)
- `user` — id, tenant_id, role (`tenant_admin`/`tenant_staff`), login details — **a person inside the business who logs into the system** (see [glossary.md](../glossary.md))
- `member` — id, tenant_id, name, phone, consent_at, user_id (nullable) — **the business's own customer**; `user_id` is filled only in the scenario where the member logs in with their own account (e.g. the fitness QR app); in scenarios like the barber, where staff open the appointment on the member's behalf, `user_id` may stay empty
- `membership_plan` — id, tenant_id, name, duration, entry_allowance
- `membership` — id, tenant_id, member_id, plan_id, start_at, end_at, status (`active`/`frozen`/`expired`)
- `tenant_payment` — id, tenant_id, source_type (`membership`/`appointment`/`order`/...), source_id, amount, method, paid_at (tenant-billing)

> Field names are always in English (see the "Language rule" in [code-style.md](../platform/code-style.md)) — the `consent_at` field holds the date of KVKK (Turkish personal data protection law) explicit consent; the abbreviation "KVKK" does not appear in code, and its legal reference is in [kvkk-checklist.md](../platform/kvkk-checklist.md).

## Relationship with other modules

The fitness and barber modules use the `membership` and `tenant-billing` tables directly and do not rewrite their own membership/payment logic. The only module-specific thing is "what the service is" (QR check-in vs appointment) — membership/payment stay in the core.

The `source_type`/`source_id` fields of `tenant_payment` are deliberately kept generic: so that modules with no concept that replaces membership (e.g. an order-based vertical) can also use tenant-billing. A new module adds its own domain object (`order`, `reservation`, etc.) as a `source_type`, with no new table/migration needed in `core`.

## Out of scope

- Module-specific logic such as QR generation and the appointment calendar does not belong here — these live under `modules/*`.
- The subscription revenue the platform receives from tenants (platform-billing) is not here, it is under `packages/platform`.

## Related files

- The precise definition of terms (including the `user` vs `member` difference): [glossary.md](../glossary.md)
- Details of the tenant/feature flag mechanism: [platform/tenant-model.md](../platform/tenant-model.md)
- Role hierarchy: [platform/roles-and-access.md](../platform/roles-and-access.md)
- Billing separation: [platform/billing-model.md](../platform/billing-model.md)
- Fields related to KVKK: [platform/kvkk-checklist.md](../platform/kvkk-checklist.md)
- Change history (who changed what): [platform/audit-log.md](../platform/audit-log.md)
- Security (rate limiting, token lifetime): [platform/security.md](../platform/security.md)
- How to use this file when adding a new module: [workflow/new-module-checklist.md](../workflow/new-module-checklist.md)
