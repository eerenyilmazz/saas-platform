# Support Access (Impersonation)

## Problem

[roles-and-access.md](roles-and-access.md) says the `platform_owner` "can see all tenants", but this definition alone is insufficient — in practice the following scenario comes up: a salon owner calls you saying "I can't find my member in the system". You (platform_owner) need to look at that tenant's data to solve their problem. How and with what traces you would do this had not been defined.

## Decision

The platform_owner can temporarily look at a tenant's data with an **explicitly logged, time-limited "support mode"**. There is no unlimited/silent access.

## Flow

```
platform_owner → "start support mode for this tenant" (a reason is entered, e.g. "complaint: member record not found")
        → the system opens a support_session (time-limited, e.g. 30 minutes)
        → during this time the platform_owner can see that tenant's data (like the salon owner)
        → every view/operation is written to audit_log with a "support mode" tag
        → when the time expires or the platform_owner closes it, access ends automatically
```

## Rules

- **A reason is mandatory** when starting support mode — access cannot be opened without a reason.
- Every operation performed while in support mode is written to [audit-log.md](audit-log.md) with a mark that sets it apart from normal operations (`via_support_session: true`) — so that when a tenant asks "who looked at my account", a clear answer can be given.
- Support mode starts **read-only**; if data needs to be changed (e.g. correcting a membership) this requires a separate, more visible approval step (in the MVP: done by informing the tenant_admin).
- A tenant cannot fully disable support access from its own settings (the platform_owner must be able to give technical support, for KVKK/security reasons) but because every access is visible in audit_log, transparency is preserved.

## Data model

- `support_session` — id, tenant_id, platform_owner_user_id, reason, started_at, ended_at, status (`active`/`closed`)

## Out of scope (not in the MVP)

- Showing the tenant a "you are currently in support mode" notification — can be added in a later phase, audit_log is enough in the MVP.

## Related files

- [roles-and-access.md](roles-and-access.md) — the platform_owner's general authority
- [audit-log.md](audit-log.md) — logging of support access
- [kvkk-checklist.md](kvkk-checklist.md) — third-party (platform team) data access falls under KVKK
