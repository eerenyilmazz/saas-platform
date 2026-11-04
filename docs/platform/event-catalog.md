# Domain Event Catalog

[notifications.md](notifications.md) and [external-integration-resilience.md](external-integration-resilience.md) assumed an event-based design (such as `MembershipExpiringEvent`) but there was no **single place where all event names are collected** — so that an agent does not invent its own name when it needs a new event, this is checked first.

## Naming rule

`<Subject><PastTenseVerb>` — always past tense (an event represents something that has already happened), in English, consistent with the terms in [glossary.md](../glossary.md).

## Existing events

| Event | When it is published | Who listens |
|---|---|---|
| `MemberCreated` | When a new `member` record is created | `core.audit` |
| `MembershipExpiringEvent` | N days before a membership ends (see [notifications.md](notifications.md)) | `core.notifications` |
| `MembershipExpiredEvent` | When a membership expires | `core.notifications`, the relevant module's access control |
| `MembershipFrozenEvent` | A membership was frozen | `core.audit` |
| `AppointmentCreatedEvent` | A new appointment (barber) | `core.notifications` (confirmation message) |
| `AppointmentReminderEvent` | N hours before an appointment | `core.notifications` |
| `AppointmentCancelledEvent` | An appointment was cancelled | `core.notifications`, `core.audit` |
| `CheckinFailedEvent` | QR check-in was rejected (fitness) | `core.audit`, [observability.md](../infra/observability.md) (abnormal rate monitoring) |
| `PaymentReceivedEvent` | A `tenant_payment` record was created | `core.audit` |
| `SubscriptionExpiredEvent` | A `platform_subscription` became overdue/cancelled | `platform` (the trigger for restricting tenant access) |

## Rule when adding a new event

1. First check whether a similar event exists in this table — if it does, that one is used and the name is not invented again.
2. If not, a new name is derived according to the naming rule above and **added to this table** (in the same PR as the code).
3. An event is defined only in the package of the module that publishes it (if `fitness` publishes an event, under `com.saas.modules.fitness.event`), but thanks to the entry in this catalog other modules/agents know the event exists — the rule "modules cannot import each other's classes" in [bounded-context-matrix.md](bounded-context-matrix.md) applies here too: a module **cannot listen to** another module's events; events are listened to only by `core` and, if needed, reflected to another module through `core`.

## Related files

- [notifications.md](notifications.md)
- [audit-log.md](audit-log.md)
- [glossary.md](../glossary.md)
