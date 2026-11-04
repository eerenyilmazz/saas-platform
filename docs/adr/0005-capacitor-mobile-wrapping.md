# ADR 0005: Mobile wrapping with Capacitor

**Status:** Accepted
**Date:** 2026-07-17

## Context

For the member-facing side (fitness QR check-in, barber appointments later) the question was whether to build a native app, a PWA, or wrap the web app in a native shell (hybrid). The admin panel had already been settled as a web app.

## Decision

Write a single web app codebase (React); the admin panel stays on the web as-is, and the member side is wrapped with Capacitor for iOS + Android.

## Consequences

- A single codebase ships to the web and both mobile platforms, with no separate native development.
- Being publishable on the App Store/Play Store gives stronger discoverability and trust than a PWA; push notification support (especially on iOS) is more reliable than with a PWA.
- To mitigate Apple's risk of rejecting a "bare WebView", a few Capacitor plugins such as a native splash screen and native push will be used (see [roadmap/phases.md](../roadmap/phases.md) Phase 5).
- A Mac is required for iOS builds/testing — this is not a blocker, as both a Mac and a Windows machine are available.
