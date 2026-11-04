# Mobile-Backend Compatibility Strategy

## Problem

The backend can be deployed to production on every PR merge (see [workflow/branch-strategy.md](../workflow/branch-strategy.md)), but an update to the mobile app released to the store with Capacitor goes through the App Store/Play Store review process — which can take **days**. If the speed difference between the two is not managed, a change made in the backend can cause an "the app doesn't work" error for members using an old mobile app version.

## Decision

The API is developed with a **backward compatible** change discipline; breaking changes are opened as a separate API version (`/api/v2/...`), and the old version lives in parallel for a while.

## Rules

- **Non-breaking change** (adding a new field, adding a new endpoint) → made directly within `v1`, without waiting for the mobile app to be updated.
- **Breaking change** (removing/renaming a field, changing an endpoint's behavior) → `v2` is opened. `v1` continues to live until the existing mobile app versions are upgraded to the latest version in the store (suggestion: a minimum of 60 days).
- The mobile app sends its own version number to the backend as a header on every request (`X-App-Version`). The backend logs this information — which versions are still in use can be seen, and from this it is decided when a `v1` endpoint can be safely shut down.

## Forced update mechanism

In the case of a critical security vulnerability or incompatibility it may be necessary to force the mobile app to update:

- The backend returns the "minimum supported version" information from an endpoint such as `/api/v1/app-config`.
- The mobile app checks this on launch, and if its own version is below the minimum, shows a screen directing the user to update (before locking the app completely, it first gives an "update recommended" warning).

## Out of scope (not in the MVP)

- Automatic/silent updates (for native apps this is not possible anyway under App Store/Play Store policies, user approval is required).

## Related files

- [api-conventions.md](api-conventions.md) — the versioning scheme
- [../adr/0005-capacitor-mobile-wrapping.md](../adr/0005-capacitor-mobile-wrapping.md)
- [../roadmap/phases.md](../roadmap/phases.md) — Phase 5
