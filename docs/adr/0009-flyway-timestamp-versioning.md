# ADR 0009: Flyway migrations are versioned by timestamp, not sequential number

**Status:** Accepted
**Date:** 2026-07-17

## Context

[database-conventions.md](../platform/database-conventions.md) initially adopted the classic Flyway convention (`V1`, `V2`, `V3`... sequential numbers). This is fine in an environment where a single developer works sequentially — but this platform is developed, per [agent-pipeline.md](../workflow/agent-pipeline.md), with **multiple coder agents working in parallel**. If two agents write migrations at the same time on different branches (one for the barber module, the other for the fitness module), both see "the next free number" on their own branch and use the same number (e.g. both use `V7`).

## Decision

Migration file names use a **timestamp** instead of a sequential number (`V<yyyyMMddHHmm>__<description>.sql`, minute precision).

## Consequences

- Even if two agents write migrations at the same time, they will almost certainly get different timestamps — version number collisions are structurally eliminated.
- Flyway supports this pattern directly (including the community edition); no extra tooling is needed.
- Downside: file names are not as readable as `V1`, `V2` — but the **order** of migrations is tracked from the file contents/git history anyway, and the file name being a "readable number" is not a requirement.

## Historical context

- **Original incident/finding:** This decision came out of a second external architecture review (Gemini), before any migration had been written — the review showed that sequential Flyway numbering is structurally fragile once the **parallel** working model of the agent pipeline (multiple coder agents at the same time, on different branches) is taken into account. The earlier design had not considered this because the classic Flyway documentation assumes a single-developer scenario.
- **Rejected alternatives:** A central "sequence number reservation" service (an agent obtains the next number from a central place before writing a migration) — rejected because it introduces an extra infrastructure component and a single point of failure; a timestamp gives the same result with zero additional infrastructure.
- **Failure scenario:** If two agents opened separate PRs with migrations using the same `V7` number and merged them, Flyway would stop in CI with a checksum/version conflict — this could pointlessly burn through the "Max Iteration = 3" loop in [pr-and-definition-of-done.md](../workflow/pr-and-definition-of-done.md) on renumbering (agents are weak at resolving ordering conflicts).
