# External Integration Resilience

The platform depends on external services that are outside its control: the SMS provider ([notifications.md](notifications.md)) and, in the future, the payment provider ([billing-model.md](billing-model.md) v1.3). If one of these services slows down/crashes, it must not drag down the whole API.

## Rule: timeout + circuit breaker + retry

- **Timeout:** A short upper limit is set on every external service call (suggestion: 3-5 seconds) — if the SMS provider does not respond, the user's API request does not wait forever.
- **Circuit breaker:** If an external service starts failing consecutively (suggestion: 5 consecutive errors), calls to that service are automatically stopped for a short period (suggestion: 1 minute) ("the circuit opens") — preventing every request from timing out needlessly. When the time is up a single attempt is made, and if it succeeds the circuit closes.
- **Retry:** For transient errors (such as a network drop) 1-2 automatic retries are made, for permanent errors (such as an invalid phone number) no retry is made — this distinction is based on the exception hierarchy in [error-handling.md](error-handling.md) (`ValidationException` is not retried, network-related ones are).

## This must not block the main flow

**The most important rule:** If sending an SMS fails, the main operation that triggered it (e.g. creating a membership record) **does not fail**. The notification is sent through an asynchronous queue/job separate from the main operation — the failure state of `core.notifications` does not roll back the operation of `core.membership`.

- Technically: the notification sending is triggered **after** the main transaction commits (a mechanism such as Spring's `@TransactionalEventListener(phase = AFTER_COMMIT)`).
- If notification sending fails permanently, this lands in the error tracking in [observability.md](../infra/observability.md), but the main operation shown to the user (membership created, appointment booked) remains completed without error.

## Out of scope (not in the MVP)

- A separate message queue infrastructure for external services (RabbitMQ/Kafka) — the traffic volume does not require it, and an in-application asynchronous event is enough.

## Related files

- [notifications.md](notifications.md)
- [error-handling.md](error-handling.md)
- [../infra/observability.md](../infra/observability.md)
