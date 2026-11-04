# Glossary

This file fixes the terms to ensure consistency across multiple modules/agents. When a new term is needed, it is defined here first and then used in code — never the other way around.

> While this glossary was being written, an inconsistency was found and fixed: `berber.md` previously used `customer_id`, and `core.md` linked the member to `user` (the staff/login table) without a separate table. Both were corrected according to the `member` definition below.

## Person/identity terms

| Term | Definition | Not to be confused with |
|---|---|---|
| **tenant** (business) | A company that uses the platform (a barber salon, a gym). In the documentation it may also appear as "business"/"company", but the table/code name is always `tenant`. | Not confused with `platform` — the platform is your company, the tenant is your customer. |
| **platform_owner** | You/your team, who operate the platform. The only role that can manage all tenants. | Not confused with `tenant_admin` — that role manages only its own tenant. |
| **user** (system user) | A person inside the business who **logs into the system** — holds the `tenant_admin` or `tenant_staff` role. Password/session information is kept in the `user` table. | Not confused with **member** — every `user` is a business employee, not a customer of the business. |
| **member** | The business's **own customer** — a gym member, a barber's client. `membership` (if any) is linked to this. May or may not log in (the `user_id` field is nullable) — e.g. a fitness member logs in for the QR app, a barber's client usually does not. | Not confused with **user**. `customer` or any other name is never used in code/tables, always `member`. |
| **staff** | A person with a `user` record in the `tenant_staff` role — e.g. a hairdresser working at the barber, a receptionist at the gym. It is not a separate table, it is a subset of the `user` table. | — |

## System/architecture terms

| Term | Definition |
|---|---|
| **module** | An independent package carrying the business logic of a vertical (industry) (`packages/modules/<name>`) — e.g. fitness, barber. |
| **core** | The core layer shared by all modules (auth, tenant, membership, tenant-billing, notifications, audit). |
| **platform** | The layer you (the SaaS owner) use to manage tenants — tenants do not see/know about it. |
| **feature flag** | A setting that indicates a module is on/off for a specific tenant, `tenant.settings.modules.<name>.enabled`. |
| **source_type / source_id** | A generic reference in the `tenant_payment` table showing what the payment corresponds to (`membership`, `appointment`, `order`, etc.) — see [modules/core.md](modules/core.md). |
| **platform-billing** | The SaaS subscription the tenant pays **to you**. |
| **tenant-billing** | The payment the tenant receives **from its own customer**. |
| **support session** | The `platform_owner`'s temporary, logged access to a tenant's data — see [platform/support-access.md](platform/support-access.md). |
| **ADR (Architecture Decision Record)** | A file that permanently records the context/rationale of an architecture decision, under `docs/adr/`. |

## Banned words

The words below are **never** used in code/tables — if the incorrect word on the right is written in place of the correct term on the left, it is flagged as a finding in category 2 (terminology) of [code-review-checklist.md](workflow/code-review-checklist.md); the PR is not blocked but a fix is requested.

| Correct term | Banned alternatives | Rationale |
|---|---|---|
| `member` | `customer`, `client`, `musteri` | This was not a coincidence but a deliberate choice — it was fixed after a real inconsistency between `core.md`/`berber.md` (see the note at the top of the file) |
| `tenant` | `salon`, `business`, `firma`, `company` (the word "business" may appear in documentation prose, but in code/tables it is always `tenant`) | An industry-independent name — "salon" suggests only barber/fitness and would be misleading once restaurants/construction are added later |
| `tenant_admin` | `owner`, `admin` (on its own — it stays unclear at which level the admin is) | The level must be explicit in the name so it is not confused with `platform_owner` |
| `platform_owner` | `superadmin`, `root`, `system_admin` | This is not a technical permission level but a business role — a generic name like "system admin" does not carry the meaning of "the SaaS owner who manages all tenants" |
| `staff` | `employee`, `worker`, `personel` (in code/tables) | Because it is a subset of the `user` table, a consistent term from the same root as `user` is used |
| `source_type`/`source_id` | `reference_type`, `polymorphic_type`, `kaynak_tipi`/`kaynak_id` (Turkish field names — see the "code is always English" rule in [code-style.md](platform/code-style.md)) | This is a generalization found through the restaurant stress test of `billing-model.md`; if it is called by a different name, that context is lost |

## Writing rule

- Table/field names are always in English, singular, snake_case (`member`, `tenant_payment`, `member_id`).
- Explanations inside documentation are written in English, and when a term first appears its technical equivalent is given in parentheses (e.g. "member (`member`)").
- A module author (human or agent) who needs a new term first looks at this file; if there is no equivalent here, they define the new term here and then use it.

## Related files

- [modules/core.md](modules/core.md) — the data model where the `user`/`member` distinction is applied
- [platform/code-style.md](platform/code-style.md) — how these terms are reflected in code naming
