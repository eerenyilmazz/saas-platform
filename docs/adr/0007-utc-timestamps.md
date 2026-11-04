# ADR 0007: All timestamps are UTC, converted to local time at display

**Status:** Accepted
**Date:** 2026-07-17

## Context

Many tables carry timestamps, such as appointment (`appointment`), check-in/check-out (`checkin_log`), and membership start/end. Even though the platform currently operates only in Turkey (`Europe/Istanbul`), which time zone the server/database keeps time in had never been settled — even with daylight saving time abolished, this carries a risk of inconsistency depending on the server location or Docker container configuration.

## Decision

In the database and the backend, **all timestamps are stored as UTC** (`TIMESTAMP WITH TIME ZONE` / Java `Instant`). Conversion to local time (`Europe/Istanbul`) happens **only at display time**, in the frontend or in the API's presentation layer.

## Consequences

- Where the server is physically hosted (see [open-questions.md](../open-questions.md) item 8, hosting) has no effect on time calculations.
- Whether an appointment was saved as "10:00" becomes unambiguous — the DB always holds the UTC equivalent, and the question "10:00 in which time zone" never arises.
- The API returns time fields in ISO 8601 format in UTC (`2026-07-17T07:00:00Z`), and the frontend formats them according to the user's browser time zone (in practice `Europe/Istanbul`).
- Revisit condition: if the platform is opened to tenants in other time zones (in the future), this decision needs no change — the design is already prepared for it.
- The concrete form of this preparation: the conversion to local time is not hardcoded to `Europe/Istanbul` but read from the `tenant.settings.timezone` field (default `Europe/Istanbul`) — today all tenants carry the same value, but because the field is already per tenant, when a tenant is opened in another country only that tenant's record changes value and the code does not change at all.
