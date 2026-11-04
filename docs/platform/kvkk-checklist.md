# KVKK / Data Protection Checklist

> This document is an engineering checklist, not legal advice. Before a real launch, a lawyer needs to approve the KVKK privacy notice and the data processing inventory — see the open questions list at the end of the conversation. (KVKK is Turkey's Personal Data Protection Law.)

The platform holds personal data such as member name/phone/check-in-check-out records; on the fitness side, the check-in log can also indirectly reveal a person's movement habits. For this reason it falls within the scope of KVKK.

## What needs to be done on the engineering side

- **Explicit consent text** — when adding a new member, the tenant_admin must show the member the KVKK privacy notice and obtain consent; the consent status must be kept as a field on the `member` record (`consent_at`, see [modules/core.md](../modules/core.md) — the field name is in English; "KVKK" appears only in documentation as a legal reference, not in code).
- **Data retention period** — how long personal data will be kept after a membership ends must be defined (suggestion: anonymization after 2 years, except for legal obligations); this can be automated with a scheduled job.
- **Deletion/right-to-be-forgotten request** — when a member says "delete my data", an endpoint to handle this request must be defined in `core` (`DELETE /api/v1/members/{id}/personal-data`); the related check-in/appointment records are anonymized, not deleted entirely (because of financial record-keeping requirements).
- **Third-party sharing** — a separate data processing agreement (DPA) is needed for data transfer to third parties such as the SMS provider and payment provider; these agreements are the legal side's job, but the system must keep a list of "which third parties this tenant's data goes to".
- **Data processing inventory** — a list of which personal data is held in which table (this inventory can be derived from [tenant-model.md](tenant-model.md) and the "Data model" sections in the module specs).

## Out of scope (not in the MVP, but should be noted)

- Automatic anonymization job — after v1.
- Multi-language privacy notice — Turkish only for now.

## Related files

- [tenant-model.md](tenant-model.md) — which tables hold personal data
