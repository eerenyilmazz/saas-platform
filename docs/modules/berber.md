# Module: berber (barber)

**Package:** `com.saas.modules.berber.*`
**Dependency:** `core` (auth, tenant, membership, billing)
**Priority:** 3

## Responsibilities

- **Appointment calendar** — date/time, availability per staff member
- **Service definitions** — service duration, price
- **Service + product sales** — through `core.billing`, no separate payment logic is written

## Data model (draft)

- `service` — id, tenant_id, name, duration (minutes), price
- `appointment` — id, tenant_id, staff_id (→ `core.user`), member_id (→ `core.member`), service_id, start_at, end_at, status
- `staff_availability` — staff_id, day_of_week, start_time, end_time

> Terminology note: this table previously used `customer_id` — it was changed to `member_id` to be consistent with the `member` table in `core` (see [glossary.md](../glossary.md)). `appointment` carries `start_at`/`end_at` instead of a single `scheduled_at` — this is needed so that the appointment conflict constraint (see [concurrency-and-idempotency.md](../platform/concurrency-and-idempotency.md)) can work over a time range.

## Commonalities with fitness

Fitness and barber both fit the "appointment/membership + side product sales" model. `core.membership` and `core.billing` are shared; the only difference is the shape of the service (QR check-in vs appointment booking).

## Out of scope

- QR/check-in-check-out logic is specific to the fitness module, it is not here.
- Membership package/payment definition → in `core`.
