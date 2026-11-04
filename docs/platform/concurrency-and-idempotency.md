# Concurrency & Idempotency

The system's busiest endpoints (QR check-in, payment recording) can face multiple requests at the same time — this document closes the risks of "was the same operation recorded twice" and "race conditions".

## QR check-in — duplicate record risk

If a member scans the QR very quickly back to back (or the reader device sends the same request twice because of a network error), the same check-in can be written twice to `checkin_log`.

**Rule:** Before a `checkin_log` record is created, it is checked whether there is a record for the same `member_id` within the last N seconds (suggestion: 5 seconds) — if so, no new record is opened and the existing record is returned as the response (idempotent behavior). This check can also be supported at the database level with a unique constraint (`member_id` + time window).

## Membership freeze/update — race condition

If two different staff members update the same membership at the same time (e.g. one is freezing while the other is renewing), the last writer can silently overwrite the other's change.

**Rule:** **Optimistic locking** is used on the `membership` table (a JPA `@Version` field) — an update is rejected if the record has changed since it was last read (HTTP 409, `error.code = CONFLICT`, see [error-handling.md](error-handling.md)), and the user is shown the message "this record was updated by someone else, please refresh the page".

## Payment record — idempotency key

Every request that creates a `tenant_payment` record (especially once automatic payment integration arrives in the future, see [billing-model.md](billing-model.md) v1.3) carries an **idempotency key** — if the client (the frontend or the payment provider's webhook) sends the same request again because of a network error, a second request arriving with the same key does not open a new record and returns the first result.

- Schema: `tenant_payment.idempotency_key` (unique, nullable — mandatory only in flows that carry this risk).
- This is the industry standard especially for payment provider integrations (webhooks can send the same event more than once).

## Appointment conflict (barber) — correction: a simple unique constraint is insufficient

**Earlier mistake:** This section previously suggested a simple unique constraint on `(staff_id, date/time)` — this is **wrong/incomplete**. A unique constraint catches only an exactly identical start time; two appointments that **overlap but start at different times**, such as 10:00-10:30 and 10:15-10:45, pass this check without a problem, and the same staff member would have to be in two places at once.

**The right way:** PostgreSQL's **range exclusion constraint** (`EXCLUDE USING gist`) is used — this prevents **every case where two time ranges (start, end) overlap** at the database level, even when the overlap is not an exact match:

```sql
ALTER TABLE appointment ADD COLUMN duration_range tstzrange GENERATED ALWAYS AS (tstzrange(start_at, end_at)) STORED;
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE appointment ADD CONSTRAINT no_overlapping_appointments
    EXCLUDE USING gist (staff_id WITH =, duration_range WITH &&);
```

> For this constraint to work, the `appointment` table must carry `start_at`/`end_at` (not a single `scheduled_at`) — see the current schema in [modules/berber.md](../modules/berber.md).

- The application code catches a violation of this constraint and returns it to the user as `ConflictException("BERBER_SLOT_TAKEN", ...)` (see [error-handling.md](error-handling.md)) — exactly as in the earlier design, only the database constraint itself was corrected.
- This pattern is applied the same way to `training_appointment` in fitness (a PT cannot have two students at the same time).

## Related files

- [error-handling.md](error-handling.md) — the `ConflictException` → HTTP 409 mapping
- [database-conventions.md](database-conventions.md) — index/constraint rules
- [../modules/fitness.md](../modules/fitness.md), [../modules/berber.md](../modules/berber.md)
