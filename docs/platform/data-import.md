# Migrating Existing Data (Data Import)

## Why it is needed

The target customer profile is "tradespeople who take orders/appointments over Excel + WhatsApp" — so when a new tenant is opened, they usually already have a customer/member list (Excel, a notebook, the output of another program). [onboarding.md](onboarding.md) explains how the account is opened but did not answer "how do we get the existing 200 members into the system".

## MVP decision: CSV import, one-off, supported by the platform_owner

There is no self-service "upload Excel" interface in the MVP — for the same reason as in [onboarding.md](onboarding.md): in the early stage, this automation is unnecessary engineering load for a small number of tenants.

## MVP flow

1. The tenant sends the Excel/list they have to you (platform_owner).
2. As an extension of the seed script in [onboarding.md](onboarding.md), you convert the data to a standard CSV template and load it into the system with an import script.
3. The import script converts each row into a `member` record; if there is membership/package information, it also opens a `membership` record.
4. KVKK explicit consent — whether explicit consent must be obtained again when existing members' data is transferred, or the tenant's own past consent is sufficient, will be settled with legal approval within the scope of [kvkk-checklist.md](kvkk-checklist.md) (see [open-questions.md](../open-questions.md) item 5).

## CSV template (draft)

Because the CSV the tenant fills in is **a file shown to the user**, its column headers can be localized to the tenant's language (currently Turkish — see [localization.md](localization.md); user-facing content and code are in different languages). The canonical template is:

```
name, phone, email (optional), membership_start, membership_end, plan_name
```

- When the import script reads the headers, it **maps them to the English DB fields** (`name`→`name`, `phone`→`phone`, `membership_start`→`membership.start_at`, etc.) — variable/field names in code are in English per the [code-style.md](code-style.md) rule; only the CSV itself (user input) may carry localized headers.
- The template can differ slightly per module as in [modules/_template.md](../modules/_template.md) (e.g. for barber a `last_visit_date` can be added, mapped to `last_visit_at` in the DB) but the basic fields (`name`, `phone`) are common to all modules.

## Data quality rule

- During import, the phone number format is normalized (to a consistent format such as 05XX XXX XX XX).
- Duplicate check (importing twice with the same phone number) — the script detects this and skips, it does not silently create it again.
- The import returns a summary report: how many records were added, how many records were erroneous/skipped (and why).

## When to move to self-service

The same threshold in [onboarding.md](onboarding.md) (~10 tenants) applies here too — as the number of tenants grows, a "Upload CSV" screen is added to the platform admin panel (see [roadmap/phases.md](../roadmap/phases.md) Phase 6), and the tenant_admin uploads their own data themselves.

## Out of scope (not in the MVP)

- Pulling data directly through an API integration from other software (e.g. a specific accounting program) — only CSV/manual transfer.
- Automatic data cleaning/matching (fuzzy matching) — the duplicate check is kept simple (an exact phone number match).

## Related files

- [onboarding.md](onboarding.md)
- [kvkk-checklist.md](kvkk-checklist.md)
- [../modules/_template.md](../modules/_template.md)
- [tenant-offboarding.md](tenant-offboarding.md) — the symmetric counterpart of this file (data export)
