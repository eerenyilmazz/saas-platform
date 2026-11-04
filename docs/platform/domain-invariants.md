# Domain Invariants

[architecture-constraints.md](architecture-constraints.md) constrained **how the code is structured** (layers, import rules). This file is something different: it lists **the facts of the business rules that can never be violated** — these are not code details but absolute truths about the business itself. While writing a new feature, an agent can write code that violates a rule on this list even if it conforms to the architecture; this list exists to prevent that.

## Why a separate file

`architecture-constraints.md`: *"A Controller cannot call a Repository"* — this is a **code structure** decision and can change (e.g. if the layer architecture changes).
`domain-invariants.md`: *"A member cannot have two active memberships at the same time"* — this is a **business fact** that must remain true however the code is written.

## Invariants

| # | Invariant | Why | Where it is enforced |
|---|---|---|---|
| INV-1 | A `member` cannot have more than one **active** `membership` at the same time within the same `tenant` | Risk of double charging/conflicting entitlements | `MembershipService`, DB constraint |
| INV-2 | Two appointment **time ranges for the same `staff_id` can never overlap** | A staff member cannot be in two places at once | DB exclusion constraint (see [concurrency-and-idempotency.md](concurrency-and-idempotency.md)) |
| INV-3 | A `platform_subscription` cannot move to `active` status **without payment being confirmed** | Risk of free/unauthorized access | `SubscriptionService`, state machine |
| INV-4 | A `tenant_payment` record can never remain linked to a deleted/nonexistent `source_id` | Accounting/audit integrity | FK constraint + soft-delete rule (see [database-conventions.md](database-conventions.md)) |
| INV-5 | A `member` cannot receive a message from any notification channel (SMS/email) while `consent_at` is empty | KVKK requirement | `NotificationService` check before sending |
| INV-6 | A `user` cannot access any data outside their own `tenant_id` (except the platform_owner's logged exception) | Tenant isolation | RLS + application layer (see [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md)) |
| INV-7 | No module can grant access/entry through a frozen (`frozen`) `membership` | So that freezing a membership does not become meaningless | Each module's access control checks `membership.status` |
| INV-8 | Once an `appointment`/`checkin_log` record is created, its `tenant_id` can **never** be changed | "Moving" a record between tenants is illogical and a security risk | An immutable field at the entity level |

## How this list grows

When a new module is added ([new-module-checklist.md](../workflow/new-module-checklist.md)), if that module has invariants of its own for its business rules (e.g. "an appointment cannot be booked for a past date"), they are added here — they may be repeated in the module's own spec file too, but **the authoritative source is here**; in case of a conflict this file wins.

## Test requirement

Each invariant must have at least one "try to violate this, the system must prevent it" test in [testing-strategy.md](../workflow/testing-strategy.md) — for INV-6 there is already the `rlsCannotBeBypassed` test ([tenant-isolation-enforcement.md](tenant-isolation-enforcement.md)); for the others, the same pattern is followed in the relevant module's Phase.

## Related files

- [architecture-constraints.md](architecture-constraints.md) — code structure constraints (a different layer from this file)
- [concurrency-and-idempotency.md](concurrency-and-idempotency.md)
- [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md)
- [../docs-dependency-map.md](../docs-dependency-map.md)
