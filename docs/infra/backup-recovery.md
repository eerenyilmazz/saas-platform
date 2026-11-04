# Backup & Disaster Recovery

Data loss is unacceptable in a system that holds membership and payment data — this document defines the backup and restore strategy.

## Backups

| Environment | Frequency | Retention |
|---|---|---|
| production | Daily full backup + continuous WAL (write-ahead log) archiving | 30 days |
| staging | Weekly | 7 days |
| local | Not backed up (developer environment, data loss is not critical) | — |

- If a managed service is used for PostgreSQL (see [open-questions.md](../open-questions.md) — hosting decision), automated backups are usually offered by the provider; if self-hosted on your own server, they must be set up with `pg_dump` + cron.
- Backups are stored in **a different physical location/provider** than the production database (so the backup does not go down with the server if it fails).

## Restore testing

- That backups can actually be restored is tested in the staging environment **on a regular basis** (suggestion: once a month). A backup that has not been tested does not count as a backup.

## Recovery objectives

| Metric | Target | Meaning |
|---|---|---|
| RPO (Recovery Point Objective) | ≤ 24 hours | In the worst case the last 24 hours of data may be lost (with WAL archiving this window is in practice much shorter) |
| RTO (Recovery Time Objective) | ≤ 4 hours | The target time for the system to be up again in a disaster |

These targets are a reasonable starting point for MVP scale — as the number of tenants and SLA commitments grow (see [open-questions.md](../open-questions.md) item 9, ToS/SLA), tighter targets can be adopted.

## Disaster scenarios

- **The DB server crashes** → restore from the latest backup + WAL, connect to a new instance
- **Data is deleted by accident** (human error or a faulty migration) → return to the moment before the deletion with point-in-time recovery
- **The entire hosting provider becomes unreachable** → because backups are kept at a different provider/region, it can be brought up in a new environment (a detailed runbook for this scenario will be written once the actual hosting decision is settled)

## Out of scope (not in the MVP)

- Automatic failover (active-passive DB cluster) — considered when the number of tenants and SLA pressure grow.

## Related files

- [environments.md](environments.md) — environment definitions
- [observability.md](observability.md) — alerting if the backup job fails
