# Theme & Branding System

The `settings.branding` field (`logo_url`, `color`) in [tenant-model.md](tenant-model.md) implied that a tenant could reflect its own brand, but **how this would be applied on the frontend** had never been defined. The system's own light/dark theme support had not been addressed either.

## Two separate theme concepts — must not be confused

| Concept | What | Who controls it |
|---|---|---|
| **Light/dark mode** | The system's own visual mode (light/dark) | The user's device preference |
| **Tenant branding** | A salon's own logo/accent color | The tenant (`tenant_admin`) |

These work independently of each other — a tenant also sees its own brand color in dark mode.

## Light/dark mode — supported from the start

- The frontend uses a color system based on CSS custom properties (design tokens) — such as `--color-background`, `--color-text`, `--color-accent`. Components reference these tokens, not color codes directly.
- Dark mode turns on automatically according to the user's operating system preference (`prefers-color-scheme`), and a manual light/dark toggle is also included in the MVP (a small addition, low cost).
- This is not a feature that is hard to add later, but if it is not set up **from the start with a token system** (if colors are embedded in components) all components later have to be scanned one by one — the same logic as the "cheap now, expensive later" principle in [adr/0006](../adr/0006-externalize-strings-from-day-one.md).

## Tenant branding — limited customization

A tenant is not allowed to change its **entire** color palette (this gets out of control and creates accessibility/readability risk) — only a single **accent color** (`--color-accent`) and the logo can be customized.

```
tenant.settings.branding = { logo_url: "...", color: "#2C3E6B" }
```

- This accent color is used in a small number of specific components such as buttons/links; basic readability areas such as body text and background come from the system's own (light/dark mode) tokens, and the tenant cannot change these.
- **Contrast check:** When a tenant enters its own color, the system automatically checks whether this color provides sufficient contrast with the background (the WCAG AA threshold) — if it does not, a warning is shown in the admin panel (they can still use the color but would be choosing it knowingly).

## Out of scope (not in the MVP)

- Advanced customization such as the tenant's own font/logo placement — only color + logo.
- A theme preview editor — the color code is entered as plain text in the admin panel; live preview is after v1.

## Related files

- [tenant-model.md](tenant-model.md) — the data model of the `branding` field
- [file-storage.md](file-storage.md) — the logo upload flow
- [code-style.md](code-style.md) — frontend component/token naming
- [accessibility.md](accessibility.md) — how the contrast check relates to the general WCAG commitment
