# Audit Log — Change History

`com.saas.core.audit`

The central mechanism that answers the question: who changed which record, and when. Especially for membership and payment records, this log is the single source when an objection like "I was charged the wrong fee" comes in, or when it is necessary to check whether a staff member made an erroneous transaction.

## Scope

Changes (create/update/delete) in the following tables are logged automatically:

- `membership`, `membership_plan` — membership record/plan changes
- `tenant_payment` — tenant-billing records
- `platform_subscription` — platform-billing records
- `user` — role changes, activating/deactivating accounts
- `tenant.settings` — feature flag (module on/off) changes

Other tables (e.g. `checkin_log`, `appointment`) are not audited separately since they are already an event record on their own — the real risk is in records that are "irreversible/affect money".

## Data model

- `audit_log` — id, tenant_id, table_name, record_id, action (`create`/`update`/`delete`), old_value (JSON), new_value (JSON), actor_user_id, actor_role, occurred_at

## Technical approach

Audit code is not written separately in each module — a shared mechanism in `core` (e.g. a JPA entity listener / AOP interceptor) watches the tables above and writes to `audit_log` automatically. When adding a new module, if there is a table that needs auditing, that table is registered with this shared mechanism (see [workflow/new-module-checklist.md](../workflow/new-module-checklist.md)).

## Who can see it

- `platform_owner` — can see the audit log of all tenants
- `tenant_admin` — can see only its own tenant's audit log
- `tenant_staff` — cannot see it

## Out of scope (not in the MVP)

- A search/filter interface over the audit log — in the first release records are only kept, and queried from the DB when review is needed. The interface is added together with the platform admin panel (see [roadmap/phases.md](../roadmap/phases.md) Phase 6).

## Related files

- [tenant-model.md](tenant-model.md) — tenant isolation, audit_log also carries tenant_id
- [support-access.md](support-access.md) — support access is also written to audit_log
