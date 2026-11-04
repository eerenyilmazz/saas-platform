# Accessibility

[theming.md](theming.md) only checked the contrast of the brand color the tenant chooses — this document defines a minimum accessibility commitment across the product as a whole.

## MVP decision: WCAG AA minimum, automated checking

A comprehensive accessibility program (screen reader tests, user tests) is overinvestment for the MVP — instead, **minimum rules that can be set up cheaply from the start and can be automated**:

- **Contrast:** In addition to the tenant color check in [theming.md](theming.md), the system's own (light/dark mode) tokens are chosen to meet the WCAG AA threshold (4.5:1 for normal text, 3:1 for large text) from the start.
- **Keyboard navigation:** All interactive elements (buttons, form fields, links) must be reachable/usable with the keyboard alone — not requiring a mouse is a requirement that matters especially in the admin panel (staff use it daily).
- **Semantic HTML:** Frontend components (React) use meaningful HTML tags (a `<div>` with `onClick` is not written instead of a `<button>`) — the basic prerequisite for screen reader support, requiring no extra effort.
- **Form labels:** Every form field has a `<label>`; placeholder text alone is not considered enough.

## Automated checking

- An accessibility linter is added to the frontend CI (suggestion: `eslint-plugin-jsx-a11y`) — an extension of the static analysis layer in [static-analysis.md](static-analysis.md), no separate toolchain needed.
- This is added as an item to category 2 (consistency) of [code-review-checklist.md](../workflow/code-review-checklist.md).

## Who it matters for in particular

- **`tenant_staff`** (fitness reception, barber staff) — daily, fast use, often in moments when their hands are full (see the check-in scenario in [modules/fitness.md](../modules/fitness.md)) — keyboard/fast interaction here is not a "nice to have" but a real usability matter.

## Out of scope (not in the MVP)

- Full WCAG AAA compliance
- Real user tests (testing with real users who use screen readers) — after v1, if real demand arises.
- Multi-language accessibility testing (see [localization.md](localization.md) — not on the agenda now since there is a single language).

## Related files

- [theming.md](theming.md) — color contrast, the tenant brand color constraint
- [static-analysis.md](static-analysis.md) — the automated checking layer
