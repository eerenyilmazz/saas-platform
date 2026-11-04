# ADR 0001: Monorepo structure

**Status:** Accepted
**Date:** 2026-07-17

## Context

A modular SaaS (core + barber + fitness + new verticals later) is going to be built. We had to settle whether each module gets its own repository (multi-repo) or whether modules are separated by folder inside a single repository (monorepo).

## Decision

Use a monorepo with folder-based module separation (`packages/core`, `packages/modules/*`).

## Consequences

- Shared code (`core`) is not duplicated and there is no version-synchronization overhead.
- A single CI/CD pipeline can tell which module changed through path-based triggering.
- Risk: branches that touch `core` can leave other open branches "behind" — this is mitigated by the short-lived branch rule in [workflow/branch-strategy.md](../workflow/branch-strategy.md).
- Revisit condition: if a module starts being sold as a fully independent product (i.e. it needs a different deploy cycle), it can be split out into its own repository.
