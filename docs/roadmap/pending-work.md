# Pending Work

A list of items that could not yet be done on the code side but are waiting on an open business decision ([open-questions.md](../open-questions.md)). The goal: to be able to answer the question "why wasn't this done" at a glance, without redoing the analysis each time.

## Phase 0 — infrastructure

| Item | Status | Decision it depends on | Details |
|---|---|---|---|
| Structured logging (JSON, `tenant_id`/`request_id`) | Not started | [open-questions.md](../open-questions.md) item 8 (hosting) | [../infra/observability.md](../infra/observability.md) §1 |
| Error tracking (Sentry) | Not started | [open-questions.md](../open-questions.md) item 8 (hosting) | [../infra/observability.md](../infra/observability.md) §2 |
| Backup job (`pg_dump`/managed service) | Not started | [open-questions.md](../open-questions.md) item 8 (hosting) | [../infra/backup-recovery.md](../infra/backup-recovery.md) |
| Uptime/alerting (external monitoring service) | Not started | [open-questions.md](../open-questions.md) item 8 (hosting) | [../infra/observability.md](../infra/observability.md) §3 |

## Phase 1 — core

| Item | Status | Decision it depends on | Details |
|---|---|---|---|
| Feature-flag service logic (read/write `Tenant.settings`) | In progress | — (no dependency, can be finished right away) | [../platform/tenant-model.md](../platform/tenant-model.md) |
| SMS notification channel (actual sending) | The abstraction (`NotificationChannel`) can be written, actual sending does not work | [open-questions.md](../open-questions.md) item 3 (SMS provider) | [../platform/notifications.md](../platform/notifications.md) |

## Non-code, waiting on a business decision

| Item | Decision it depends on | Impact |
|---|---|---|
| Domain/company name | [open-questions.md](../open-questions.md) item 1 | The `com.saas.*` package names are placeholders; if this is settled late, a one-time bulk rename across hundreds of files is needed |
| Hosting target | [open-questions.md](../open-questions.md) item 8 | Blocks the four Phase 0 items above + the deploy step of CI/CD |
| Onboarding model approval | [open-questions.md](../open-questions.md) item 4 | Confirmation that the seed script's "manual onboarding" assumption matches the business plan |

## How this file is updated

When an item is completed, or when the relevant [open-questions.md](../open-questions.md) item is marked "resolved", it is removed from this table. In case of a conflict, `open-questions.md`/[phases.md](phases.md)/the relevant module spec prevails — this file is only a tracking/summary layer, not an authoritative source.

## Related files

- [open-questions.md](../open-questions.md)
- [phases.md](phases.md)
- [../infra/observability.md](../infra/observability.md)
- [../infra/backup-recovery.md](../infra/backup-recovery.md)
- [../platform/notifications.md](../platform/notifications.md)
