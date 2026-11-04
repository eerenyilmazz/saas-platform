# Notification Infrastructure

`com.saas.core.notifications` — sending notifications needed by multiple modules (membership ending, appointment reminder, check-in confirmation) is gathered in one place. Each module does not write its own SMS/email logic.

## Design

```
[Module event] → [core.notifications.NotificationEvent] → [channel selection] → [provider API]
```

- Modules do not send SMS/WhatsApp directly, they only publish an event: such as `MembershipExpiringEvent`, `AppointmentReminderEvent`, `CheckinFailedEvent`.
- `core.notifications` listens to this event and sends it through the channel the tenant prefers (SMS/email/WhatsApp).
- Thanks to the channel abstraction (the `NotificationChannel` interface), adding a new channel in the future does not require changing the modules.

## MVP decision: SMS only

- **WhatsApp Business API** integration requires a Meta approval process + cost, and is not added to the MVP.
- **SMS** is enough as the first channel — the widespread bulk SMS providers in Turkey (Netgsm, İletimerkezi, etc.) offer a simple REST API, so integration is quick.
- Email is the second priority (for less urgent notifications than SMS — e.g. invoice information).

## Example event flow

```
core.membership → "membership ends in 3 days" → MembershipExpiringEvent
        → core.notifications listens
        → if tenant.settings.notifications.channel = "sms" → SmsChannel.send(...)
```

## SMS quota (cost protection)

SMS costs money and `core.notifications` sends automatically on every event triggered — a code bug (e.g. a loop accidentally triggering the same event 1000 times) can turn into real monetary cost. This is a different matter from the API rate limiting in [security.md](security.md) — not abuse but **cost protection**.

- A daily/monthly SMS sending limit is defined for each tenant (suggestion: according to the package, see [pricing-packaging.md](pricing-packaging.md)).
- If the limit is exceeded, sending stops and the `platform_owner` is informed (by email, not SMS — so it does not loop).
- This limit is kept under `tenant.settings.notifications.daily_sms_limit`, fitting into the flexible `settings` schema in [tenant-model.md](tenant-model.md) without an additional migration.

## Out of scope

- Per-tenant customization of notification templates (writing messages in the brand's voice) — after v1.

## Open question

Which SMS provider to contract with (Netgsm, İletimerkezi, Twilio, etc.) depends on your preference — see the open questions list at the end of the conversation.
