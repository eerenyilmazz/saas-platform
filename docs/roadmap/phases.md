# Starting Order

Order matters — each phase builds on the previous one and none is skipped.

## Phase 0 — Repo skeleton + CI + infrastructure decisions

- Create the monorepo with the folder structure in [repo-structure.md](../repo-structure.md)
- Bring up an empty Spring Boot project (`apps/api`) + an empty React project (`apps/web`) — with the versions in [tech-stack.md](../platform/tech-stack.md) (Java 21, PostgreSQL, Flyway)
- Get an empty CI pipeline (lint + build) running in GitHub Actions
- Bring up the local development environment (db + api + web) with a single command via `infra/docker`
- Apply the error format and URL scheme from [api-conventions.md](../platform/api-conventions.md) to the skeleton
- Set up basic observability: structured logging + error tracking + a `/health` endpoint (see [observability.md](../infra/observability.md)) — adding it later is more expensive than setting it up from the start
- Set up the backup job from day one (see [backup-recovery.md](../infra/backup-recovery.md)) — backing up an empty DB is easier than setting up backups later on a populated DB
- Verify/update the setup steps according to [dev-setup.md](../dev-setup.md)

## Phase 1 — Core: auth, tenant, roles

- Tenant table + tenant_id-based data isolation
- Login/role management with Spring Security — `tenant_admin`, `tenant_staff` (see [roles-and-access.md](../platform/roles-and-access.md))
- Set up the membership/package model (tables, relationships) — no UI needed yet
- Set up the audit log mechanism (see [audit-log.md](../platform/audit-log.md)) — membership/billing tables must be tracked from the very beginning
- Implement rate limiting and the token lifetime/refresh flow (see [security.md](../platform/security.md))
- A manual seed script for the first tenant ([onboarding.md](../platform/onboarding.md)) — there is no platform admin panel in this phase
- Add the tenant isolation test (see [testing-strategy.md](../workflow/testing-strategy.md)) as a mandatory CI gate
- Details: [modules/core.md](../modules/core.md)

## Phase 2 — Fitness: QR check-in MVP

- QR token generation/refresh API
- A simple web screen: show the QR + a read/approve screen on the staff side
- Check-in log table — no turnstile/hardware integration yet
- Details: [modules/fitness.md](../modules/fitness.md)

## Phase 3 — Admin panel

- Add/edit members, assign membership packages
- Check-in log report (who came when)
- Feature flag screen — which module is enabled for this tenant

## Phase 4 — Barber: appointment MVP

- Appointment calendar + service duration model
- Reuse the membership/billing infrastructure in core, do not copy it
- Details: [modules/berber.md](../modules/berber.md)

## Phase 5 — Mobile wrapping

- Wrap `apps/web` for iOS + Android with Capacitor
- Push notifications (membership expiry reminders) — use the event-based structure in [notifications.md](../platform/notifications.md)
- Apply API versioning/backward-compatibility discipline (see [mobile-compatibility.md](../platform/mobile-compatibility.md)) — after this phase, breaking changes in the backend are no longer allowed freely
- App Store + Play Store submission

## Phase 6 — Platform admin panel (as the number of tenants grows)

- A `/platform` route inside `apps/web`, accessible only with the `platform_owner` role
- List tenants, toggle modules, view platform-billing status
- Support mode (impersonation) interface (see [support-access.md](../platform/support-access.md))
- Self-service onboarding form (see the ~10-tenant threshold in [onboarding.md](../platform/onboarding.md))
- Details: [roles-and-access.md](../platform/roles-and-access.md)
