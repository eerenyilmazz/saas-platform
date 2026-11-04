# Open Questions

This file collects the points where I cannot decide on the architecture — those that require a business decision, existing relationships, or legal approval. When each item is decided, the relevant spec file is updated and the item is marked "resolved" in this file.

## 1. Real domain / company name

**Why it matters:** All Java packages are currently written with the placeholder `com.saas.*`. If code is written before the real name is settled, all package names (hundreds of files) will have to be changed in bulk afterward — a costly and error-prone refactor.

**Affected files:** [package-naming.md](package-naming.md), every module spec, all ADRs.

**Decision needed:** What will the company/product name be? (E.g. once a name is settled, the packages are updated in one go to `com.<name>.core`, `com.<name>.modules.fitness`, etc.)

**Status:** Open — it is recommended to settle this before starting to write code (part of Phase 0).

---

## 2. Payment provider for tenant-billing

**Why it matters:** If the tenant wants to take online payments from its own customers (gym member, barber client), it must be determined which provider to integrate with. In the MVP this flow was designed as manual record keeping (see [platform/billing-model.md](platform/billing-model.md)), but it will move to automation in v1.3.

**Affected files:** [platform/billing-model.md](platform/billing-model.md), [roadmap/versions.md](roadmap/versions.md) (v1.3).

**Options (short comparison):**

| Provider | Note |
|---|---|
| iyzico | Most widespread in Turkey, also supports a marketplace model (suitable if sub-accounts are to be opened for tenants) |
| PayTR | Simple integration, common among small businesses |
| Param | Enterprise-leaning, strong bank integrations |

**Decision needed:** Do you already have a merchant/API agreement, or will one be chosen from scratch?

**Status:** Open — not blocking until v1.3.

---

## 3. Notification (SMS) provider

**Why it matters:** [platform/notifications.md](platform/notifications.md) decided that the MVP channel is SMS, and the channel abstraction (`NotificationChannel`) was designed to be provider-independent — but a provider must be chosen for the real integration.

**Affected files:** [platform/notifications.md](platform/notifications.md).

**Options (short comparison):**

| Provider | Note |
|---|---|
| Netgsm | Widespread in Turkey, simple REST API |
| İletimerkezi | Similar, a price/package comparison should be done |

**Decision needed:** Do a price/package comparison and pick one — thanks to the abstraction this can be decided late and does not block the code side.

**Status:** Open — not blocking until before Phase 2 (fitness MVP, reminder notifications).

---

## 4. Onboarding model approval

**Why it matters:** In [platform/onboarding.md](platform/onboarding.md) I made the decision "manual onboarding in the MVP, you/your team open the tenant", because investing in a self-service sign-up form at an early stage is unnecessary engineering load. I need to make sure this assumption matches your business plan.

**Affected files:** [platform/onboarding.md](platform/onboarding.md), [roadmap/phases.md](roadmap/phases.md) (Phase 6).

**Decision needed:** Will you find the first customers yourself and open them manually, or are you aiming from the start at a self-service model where "anyone who wants can sign up"? (If it were self-service, a sign-up form + payment would be added to Phase 0 — the architecture has currently put this outside the MVP.)

**Status:** Open — must be settled before Phase 1 begins, because the tenant seed script is being written according to this assumption.

---

## 5. KVKK — legal approval

**Why it matters:** [platform/kvkk-checklist.md](platform/kvkk-checklist.md) covers the engineering side (explicit consent field, deletion endpoint, data retention period), but this is not legal advice. Before going live with real member data (especially fitness — indirect health/movement data), a lawyer needs to approve the privacy notice and the data processing inventory. (KVKK is Turkey's personal data protection law.)

**Affected files:** [platform/kvkk-checklist.md](platform/kvkk-checklist.md).

**Decision needed:** Who will obtain this approval and when — it must be completed before v1.0 (the first live release, a real tenant); it does not block the MVP development process but must block going live.

**Status:** Open — marked as a mandatory prerequisite for v1.0.

---

## 6. Platform admin panel timing

**Why it matters:** In [platform/roles-and-access.md](platform/roles-and-access.md) and [platform/onboarding.md](platform/onboarding.md) I assumed a "~10 tenants" threshold (the point at which manual onboarding becomes an operational burden). This number is entirely an estimate.

**Affected files:** [platform/onboarding.md](platform/onboarding.md), [roadmap/versions.md](roadmap/versions.md) (v1.2), [roadmap/phases.md](roadmap/phases.md) (Phase 6).

**Decision needed:** Depending on the growth rate you expect, this threshold could be earlier (e.g. 5 tenants) or later (e.g. 20 tenants).

**Status:** Open — not blocking, reviewed as the number of tenants grows.

---

## 7. Per-module pricing figures

**Why it matters:** The `platform_subscription` table in [platform/billing-model.md](platform/billing-model.md) holds "plan (which modules are included), monthly_fee" fields, but the real figures are in no file — these are entirely a marketing/pricing decision, not an engineering one.

**Affected files:** [platform/billing-model.md](platform/billing-model.md).

**Decision needed:** What will the price be of the starter package (e.g. only fitness or only barber) and of the package with all modules included? Will you do the competitor analysis (the prices of similar SaaS products in Turkey) yourself, or shall we evaluate it together?

**Status:** Open — does not block the code side (the price field can be kept as free text/number), but should be settled before v1.0 because it is needed when talking to the first tenant.

---

## 8. Hosting / deploy target

**Why it matters:** [infra/environments.md](infra/environments.md) defined the local/staging/production separation but did not specify **where** it will run (which cloud provider).

**Affected files:** [infra/environments.md](infra/environments.md).

**Options (short comparison):**

| Provider | Note |
|---|---|
| Hetzner / DigitalOcean | Cheap, simple VPS, enough for small scale, you set up your own Docker infrastructure |
| AWS/GCP/Azure | More managed services (managed Postgres, auto-scaling) but more complex and expensive, may be unnecessary at an early stage |
| Railway / Render | PaaS, the least operational load, cost may rise at medium scale |

**Decision needed:** One must be chosen based on budget and operational preference — in Phase 0 we need to know where CI/CD will deploy.

**Status:** Open — required to complete Phase 0 (without a staging deploy target the CI/CD step is left half-finished).

---

## 9. ToS / SLA (tenant agreement)

**Why it matters:** [platform/kvkk-checklist.md](platform/kvkk-checklist.md) covers the data protection side, but the commercial agreement between you and the tenant (Terms of Service, uptime commitment/SLA if any, liability limits, what happens on late payment) is a separate legal matter and has not been addressed at all.

**Affected files:** [platform/onboarding.md](platform/onboarding.md) (this agreement should be shown at sign-up), [infra/backup-recovery.md](infra/backup-recovery.md) (the RPO/RTO targets turn into commitments here).

**Decision needed:** The ToS and, if any, the SLA text should be prepared with a lawyer — this is entirely legal work; the engineering side only prepares the technical implementation such as "does a module shut down automatically on late payment".

**Status:** Open — must be completed before v1.0 (the first real tenant), and can be handled together with item 5 (KVKK) in the same legal consultation process.

---

## 10. e-Invoice / e-Archive obligation

**Why it matters:** In Turkey, businesses exceeding a certain turnover threshold are required to switch to the GİB (Revenue Administration) e-Invoice/e-Archive system. This can come up in two different places: (a) you may need to issue legal invoices to tenants for platform-billing, (b) the tenant may need to issue a legal invoice to its own customer (membership/service fee). So far `tenant_payment`/`platform_subscription` have been designed only for internal record keeping (see [billing-model.md](platform/billing-model.md)); legal invoice generation has not been addressed at all.

**Affected files:** [platform/billing-model.md](platform/billing-model.md), [platform/pricing-packaging.md](platform/pricing-packaging.md).

**Decision needed:**
- How do you make the platform-billing invoice you issue to tenants legal (platform_owner) — through your accountant, or is an e-Invoice API integrated into the system needed?
- Does the tenants' need to issue legal invoices to their own customers exist in the MVP, or is "recording the amount" enough (many small tradespeople do not issue receipts/invoices anyway)?

**Status:** Open — does not block the engineering side (the existing `tenant_payment` schema can later be extended with an `e_invoice_reference_no` field, the migration is cheap), but it is a topic that must be settled quickly once real revenue starts to arise (after v1.0); it needs to be discussed with your accountant or the tenants' accountants.

---

## Summary — which ones should be settled before starting to write code

| # | Question | Blocking? |
|---|---|---|
| 1 | Domain/company name | Recommended before Phase 0 |
| 4 | Onboarding model | Mandatory before Phase 1 |
| 8 | Hosting target | Mandatory before Phase 0 |
| 7 | Pricing figures | Before v1.0 (for sales, not code) |
| 5 | KVKK legal approval | Mandatory before v1.0 (going live) |
| 9 | ToS/SLA legal approval | Mandatory before v1.0 (going live) — together with item 5 |
| 2, 3, 6, 10 | Payment/SMS provider, panel threshold, e-Invoice | Not blocking, can be decided in a later phase |
