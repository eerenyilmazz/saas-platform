# Module: fitness

**Package:** `com.saas.modules.fitness.*`
**Dependency:** `core` (auth, tenant, membership, billing)
**Priority:** 2

## Responsibilities

- **QR-based check-in/check-out** — the member's QR code is a token refreshed at short intervals (member_id + timestamp + signature)
- **Access control** — when a token is read, activeness is checked through `core.membership`, and a check-in log entry is recorded
- **Turnstile/RFID support** — an optional sub-module, enabled with a per-tenant feature flag
- **PT (personal trainer) appointment calendar**
- **Product/stock sales** (protein, drinks, etc.) — later phase, through `core.billing`

## Flow (access control)

```
[QR Reader/Tablet] → [API: /fitness/checkin] → [core.membership check] → [approve/reject] → [check-in log]
```

- Token validation should also consider the offline scenario: the local device caches the last-synced membership list.
- If a turnstile is connected, a relay/GPIO is triggered after approval; otherwise the approval is shown on the staff screen.

## QR token security details

- The token is signed with HMAC-SHA256: `token = base64(member_id + timestamp) + "." + HMAC(member_id + timestamp, tenant_secret_key)` — using a JWT is also an alternative, but for such a short-lived/simple token it adds unnecessary breadth; plain HMAC is enough. The token represents the person (`member`), not a specific membership record — the activeness check is done dynamically from `core.membership` at validation time (see the cancellation scenario below).
- `tenant_secret_key` is generated and kept separately per tenant (if one tenant's key leaks it does not affect the others) — subject to the general secrets management rule in [security.md](../platform/security.md).
- The token lifetime is short (suggestion: 30 seconds) — the QR on the member's screen is refreshed automatically at this interval, which makes sharing a screenshot pointless (it is invalid once the time expires).
- The token is **not single-use** (the same member can enter and exit multiple times in a row), but the same token being read from a different device within a short time (e.g. at two different gym branches simultaneously) is logged as suspicious activity.
- Cancellation scenario: if a membership is frozen/cancelled, the currently valid token (even if it has not yet expired) automatically becomes useless because it will fail the `core.membership` check on the next validation — no separate "token revocation" mechanism is needed.

## Data model (draft)

- `checkin_log` — id, tenant_id, member_id, membership_id (which membership was active at check-in time — for reporting), checked_in_at, checked_out_at (optional)
- `qr_token` — member_id, token, issued_at, expires_at
- `training_appointment` — id, tenant_id, trainer_id (→ `core.user`), member_id, scheduled_at

## Out of scope

- Membership package definition, payment, invoicing → in `core`.
- Appointment/service duration logic is a concept specific to the barber module, not repeated here.
