# ADR 0006: Externalizing strings from day one (i18n readiness)

**Status:** Accepted
**Date:** 2026-07-17

## Context

The platform currently targets Turkish only; multi-language support is not in the MVP scope. However, if all user-facing text (error messages, button labels, notification templates) is embedded directly in code (hardcoded), adding a second language later would require a large retrofit effort.

## Decision

All user-facing text is kept **outside** the code (resource bundle / i18n file) from the start — even though there is currently a single language (Turkish).

## Consequences

- Backend: error messages (see the `message` field in the error format in [platform/api-conventions.md](../platform/api-conventions.md)) are produced from a message file (such as `messages_tr.properties`) keyed by `code`; no literal Turkish strings are written in Java code.
- Frontend: on the React side, text is read from a translation file (e.g. `tr.json`); no literal text is written inside JSX.
- Notification templates ([platform/notifications.md](../platform/notifications.md)) are kept in a separate file following the same logic.
- This barely slows down MVP speed (only the habit of "where do I write this text" changes), but when a second language is wanted, the cost drops from scanning dozens of files one by one to extract strings down to simply adding a new language file.
- No revisit condition — this is an early, cheap discipline decision and it is easy to reverse (it costs nothing if unused).

For concrete implementation details (date/number formatting, plural patterns, language switcher) see [platform/localization.md](../platform/localization.md).
