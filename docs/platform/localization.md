# Localization

[adr/0006-externalize-strings-from-day-one.md](../adr/0006-externalize-strings-from-day-one.md) made the decision not to embed text in code — this document defines **the concrete implementation** of that: how the language is chosen, how dates/numbers are formatted, and what changes when a second language is added.

## MVP: a single language, an architecture ready for multiple languages

- There is **no language switcher** shown to the user — the system uses a fixed `tr-TR`.
- But because all text is kept outside the code as in [adr/0006](../adr/0006-externalize-strings-from-day-one.md) (`tr.json` on the frontend, `messages_tr.properties` on the backend), adding a second language is as simple as **adding a new file + a language switcher UI** and does not require rewriting existing code.

## Date/number formatting — locale-aware from the start

- Date, time, and currency display is done from day one with **locale-aware** libraries (frontend: `Intl.DateTimeFormat`/`Intl.NumberFormat`, backend: Java `java.time` + `NumberFormat.getInstance(Locale)`) — a hand-formatted string like `"17.07.2026"` is never written.
- Why now: even with a single language this distinction is free (the library already takes a locale parameter), but scanning for and changing hand-written date formats afterward is expensive — the same logic as the "cheap now, expensive later" principle in [adr/0006](../adr/0006-externalize-strings-from-day-one.md).
- For timestamps, the UTC→local conversion in [adr/0007-utc-timestamps.md](../adr/0007-utc-timestamps.md) is done in the same layer as this locale-aware formatting.

## Plural forms

Turkish does not have complex plural rules like English (the noun does not change after a number — the word stays the same whether the count is 1 or 5), but message templates are still written in ICU MessageFormat style (`{count, plural, other {# members}}` — shown here in English for readability) — even if it looks unnecessary today, if a language with plural rules like English is added in the future, only a new language file will be needed instead of rewriting the message templates.

## Out of scope (not in the MVP)

- Language switcher UI
- Right-to-left (RTL) language support (Arabic, etc.) — unnecessary for the currently targeted languages (Turkish, probably English in the future).
- Automatic translation — any second language is added with manual/professional translation.

## Related files

- [../adr/0006-externalize-strings-from-day-one.md](../adr/0006-externalize-strings-from-day-one.md)
- [../adr/0007-utc-timestamps.md](../adr/0007-utc-timestamps.md)
- [code-style.md](code-style.md)
