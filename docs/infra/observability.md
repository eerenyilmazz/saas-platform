# Observability — Logging, Error Tracking, Uptime

When something breaks in production, you need to find out before the tenant does. This document defines three layers: logging, error tracking, uptime/alerting.

## 1. Structured logging

- All services (the Spring Boot `api`) produce **structured (JSON) logs** — instead of plain-text logs, every line carries fields such as `tenant_id`, `request_id`, `level` (info/warn/error).
- `request_id` stays the same across all log lines from a request's entry into the API to its exit — so that when investigating an error, all related logs can be found with a single query.
- Logs are collected in a central place (suggestion: the log service offered by the hosting provider, e.g. a simple self-hosted log collector on a VPS; a large log infrastructure is not needed in the MVP).

### PII masking (related to KVKK)

Logs freely carry technical fields such as `tenant_id`/`request_id`, but **personal data is never logged in plain text** — fields such as phone number and full name are masked before being written to a log line (e.g. partial masking like `05XX XXX XX 34`) or not logged at all, with only an ID reference (`member_id`) passed. This is the reflection of the data protection principle in [kvkk-checklist.md](../platform/kvkk-checklist.md) at the logging layer — so that a log leak does not turn into a KVKK (Turkish personal data protection law) violation.

## 2. Error tracking

- Uncaught errors in the backend and frontend are reported automatically (suggestion: Sentry — there is also an open-source self-host option, and the free tier is enough for the MVP).
- Every error report is tagged with `tenant_id` and `request_id` — so the question "in which tenant, on which request" can be answered immediately.
- Critical errors (e.g. payment record failure, an attempted tenant isolation violation) trigger an instant notification (email/Slack).

## 3. Uptime / alerting

- A simple external monitoring service (e.g. UptimeRobot, Better Uptime) checks the `api`'s `/health` endpoint at regular intervals.
- The `/health` endpoint checks not only "am I up" but also that the DB connection works (by running a simple query against the DB).
- Instant notification (SMS/email) on downtime — these services generally offer a free/low-cost tier, so there is no need to build separate infrastructure for the MVP.

## What to monitor — in priority order

1. Is the API up (`/health`)
2. Error rate in payment/membership records (number of uncaught exceptions)
3. Did the backup job run successfully (see [backup-recovery.md](backup-recovery.md))
4. QR check-in failure rate (for the fitness module — an abnormal increase may point to a hardware or API problem)

## Out of scope (not in the MVP)

- Distributed tracing (tracking requests across multiple services) — unnecessary for now because the modular monolith is a single service; it comes up if we move to microservices.
- Advanced dashboard/metrics infrastructure (Grafana/Prometheus) — considered as the number of tenants and traffic grows.

## Related files

- [backup-recovery.md](backup-recovery.md)
- [environments.md](environments.md)
