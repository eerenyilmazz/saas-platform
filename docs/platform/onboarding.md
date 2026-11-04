# Onboarding — How a New Tenant Is Opened

## MVP decision: manual onboarding

The first tenants (pilot salons) **do not sign up on their own** — you/your team open them. Reason: in the early stage the number of customers is small, and building a self-service sign-up form + verification + payment integration would be early investment in a feature nobody will use yet.

## MVP flow

1. Sales conversation (you ↔ salon owner) — which modules are wanted, how many users
2. The `platform_owner` opens a `tenant` record with a seed script/SQL: name, active modules (`tenant.settings.modules`), platform_subscription plan
3. A `tenant_admin` user is created, and the login details are sent by email
4. The salon owner logs in and completes their business information (working hours, service/package definitions) — this part is done through the UI, from the screens of `core` and the relevant module
5. The salon owner adds their own staff (`tenant_staff`)

## When to move to self-service

As the number of tenants grows (the anticipated threshold: ~10 tenants, when doing steps 2-3 manually becomes an operational burden), a sign-up form + an approval flow from the platform admin panel is added — see [roadmap/versions.md](../roadmap/versions.md) v1.2.

## Out of scope (not in the MVP)

- Instant sign-up/payment by credit card
- Automatic trial period management
- Email verification flow (not needed in manual onboarding, since you are already in direct contact)

## Related files

- [roles-and-access.md](roles-and-access.md) — the platform_owner's authority to open tenants
- [billing-model.md](billing-model.md) — the platform_subscription record
- [tenant-offboarding.md](tenant-offboarding.md) — the reverse flow when a tenant wants to leave
