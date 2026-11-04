# Pricing Package Structure

> This file defines the package **structure**, not the real figures — the figures are entirely your marketing decision (see [open-questions.md](../open-questions.md) item 7). Here only the tiers that the `platform_subscription` model (see [billing-model.md](billing-model.md)) will be divided into are designed.

## Tier logic: module-based, base + add-on module

There are two common SaaS packaging models: (a) fixed tiers (Starter/Growth/Enterprise, each with a fixed feature set) or (b) a base fee + an additional fee for each module you choose. Because this platform is already designed to be modular, the **(b) base + add-on module** model is more consistent with the architecture — a salon owner takes only the module they need and does not pay for unnecessary features.

## Proposed structure

| Component | What it includes |
|---|---|
| **Base fee** | `core` is always included — auth, tenant, membership engine, tenant-billing, notifications (basic SMS quota), 1 user (tenant_admin) |
| **Module add-on** | Each active module (`fitness`, `berber`, new verticals in the future) has a separate add-on fee |
| **User add-on** (optional) | An extra fee if the number of `tenant_staff` exceeds a certain threshold — probably unnecessary for small salons, meaningful as they grow |

## Example package skeleton (without figures)

```
Base package:
  - core (auth, membership, tenant-billing, basic notifications)
  - 1 module included (fitness OR berber)
  - up to 3 users

+ Add-on module: an extra fee for each additional module
+ Add-on user: an extra fee for each user after the 3rd (optional, may not exist in the MVP)
```

## Simplification in the MVP

The `platform_subscription` table (see [billing-model.md](billing-model.md)) currently holds only the `plan` (free text/JSON, which modules are included) and `monthly_fee` fields — this allows keeping the price manually flexible for the first few tenants without building a complex "tier engine". Tying the tiers to strict rules (e.g. automatic invoicing) becomes meaningful when moving to automatic payment (see [roadmap/versions.md](../roadmap/versions.md) v1.3).

## Comparison with competitors — whose job

Researching the prices of similar SaaS products in Turkey (those doing salon/appointment management) and determining the base+module figures was left to your decision in [open-questions.md](../open-questions.md) item 7 — this document only prepares the structure; filling in the figures is the remaining work.

## Related files

- [billing-model.md](billing-model.md) — the `platform_subscription` data model
- [onboarding.md](onboarding.md) — when/how the package selection is made
- [open-questions.md](../open-questions.md) — the real figures
