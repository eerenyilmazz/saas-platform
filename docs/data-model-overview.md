# Data Model — Overview

> This file is a **derived reference, not a source**. The authoritative definition of each table is in its own module's spec file; it is collected here only so the whole system can be seen on a single page. If a module file is updated, this page should be updated too — in case of a conflict, the module file prevails.

## Module Capability Matrix (Feature Capability Matrix)

An agent answers a question like "does fitness have appointments, does barber have QR" from here, not by reading 10 files:

| Module | Appointments | QR/Check-in | Membership (`membership`) | Payment (`tenant_payment`) | Notifications |
|---|---|---|---|---|---|
| **core** | ❌ | ❌ | ✅ (engine lives here) | ✅ (engine lives here) | ✅ (engine lives here) |
| **fitness** | ✅ (`training_appointment`) | ✅ (`qr_token`, `checkin_log`) | ✅ uses | ✅ uses | ✅ uses |
| **berber** | ✅ (`appointment`) | ❌ | ✅ uses (optional, loyalty card) | ✅ uses | ✅ uses |

When a new module is added ([new-module-checklist.md](workflow/new-module-checklist.md)), a new row is added to this table — if a module has a single capability of its own other than ✅/❌ (e.g. "inventory" in the future), a new column is opened.

## All tables, by owner

### core (`packages/core`) — see [modules/core.md](modules/core.md)

| Table | Fields | Note |
|---|---|---|
| `tenant` | id, name, settings (JSON) | Feature flags are inside `settings` |
| `user` | id, tenant_id, role, login details | Only people inside the business (see [glossary.md](glossary.md)) |
| `member` | id, tenant_id, name, phone, consent_at, user_id (nullable) | The business's own customer |
| `membership_plan` | id, tenant_id, name, duration, entry_allowance | |
| `membership` | id, tenant_id, member_id, plan_id, start_at, end_at, status | |
| `tenant_payment` | id, tenant_id, source_type, source_id, amount, method, paid_at | `source_type` can expand to each module's own domain object |

### platform (`packages/platform`) — see [platform/](platform/)

| Table | Fields | Note |
|---|---|---|
| `platform_subscription` | id, tenant_id, plan, monthly_fee, status, next_payment_at | Your revenue — see [platform/billing-model.md](platform/billing-model.md) |
| `audit_log` | id, tenant_id, table_name, record_id, action, old_value, new_value, actor_user_id, actor_role, occurred_at | see [platform/audit-log.md](platform/audit-log.md) |
| `support_session` | id, tenant_id, platform_owner_user_id, reason, started_at, ended_at, status | see [platform/support-access.md](platform/support-access.md) |

### fitness (`packages/modules/fitness`) — see [modules/fitness.md](modules/fitness.md)

| Table | Fields | Note |
|---|---|---|
| `checkin_log` | id, tenant_id, member_id, membership_id, checked_in_at, checked_out_at | |
| `qr_token` | member_id, token, issued_at, expires_at | |
| `training_appointment` | id, tenant_id, trainer_id (→user), member_id, scheduled_at | |

### berber (`packages/modules/berber`) — see [modules/berber.md](modules/berber.md)

| Table | Fields | Note |
|---|---|---|
| `service` | id, tenant_id, name, duration, price | |
| `appointment` | id, tenant_id, staff_id (→user), member_id, service_id, start_at, end_at, status | |
| `staff_availability` | staff_id, day_of_week, start_time, end_time | |

## Relationship map (textual)

```
tenant
 ├─ user (tenant_admin / tenant_staff)
 ├─ member ──user (nullable, only for members who can log in)
 │    ├─ membership ──membership_plan
 │    ├─ checkin_log, qr_token, training_appointment   (fitness)
 │    └─ appointment                                    (berber)
 ├─ tenant_payment ──(via source_type/source_id to membership | appointment | order...)
 ├─ audit_log
 └─ support_session ──platform_owner (user, outside the tenant)

platform_subscription ──tenant   (platform layer, a table "about" the tenant, not one the tenant "sees")
```

## Updating this page when adding a new module

One more step was added to [workflow/new-module-checklist.md](workflow/new-module-checklist.md): the new module's data model must also be listed here (under its own section) — see the final form of the checklist.

## Related files

- [glossary.md](glossary.md) — definition of table/field names
- [workflow/new-module-checklist.md](workflow/new-module-checklist.md)
