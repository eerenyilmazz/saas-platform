# ADR 0002: Modular monolith with Spring Boot

**Status:** Accepted
**Date:** 2026-07-17

## Context

We needed to decide on the backend language/framework. Alternatives: Node/NestJS, Python/Django, Spring Boot. There was also the question of microservices versus a modular monolith.

## Decision

Build on Spring Boot (Java 21) as a modular monolith (not microservices).

## Consequences

- Role/permission management with Spring Security and the DB layer with Spring Data JPA are ready-made and mature.
- A modular monolith avoids the complexity of microservices at this early stage (distributed systems, network failures, inter-service communication) — the package boundaries (`core`/`modules/*`) already provide the logical separation.
- Downside: the first MVP may ship slightly slower than with Node/Python; this was judged an acceptable price for long-term robustness.
- Revisit condition: if one module's traffic/scale needs diverge sharply from the others, that module can be extracted into a separate service (the package boundaries already make this easier).
