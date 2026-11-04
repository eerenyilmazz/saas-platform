# API Documentation & Frontend Type Sharing

[api-conventions.md](api-conventions.md) defines the rules, but how these rules are documented **in real time** (updating automatically as code changes) and how the frontend stays in sync with the backend DTOs had not been defined. Without this, the frontend agent (or developer) has to track backend changes by hand — drift becomes inevitable.

## Decision: automatic OpenAPI generation

- Spring Boot generates an OpenAPI (Swagger) schema automatically from every endpoint with `springdoc-openapi` — this schema is not written by hand, it updates automatically as the code (controllers + DTOs) changes.
- The schema can be viewed live in the development environment under `/api/v1/docs` (Swagger UI) — both human developers and the coder agent refer to it before using an endpoint.

## Frontend type synchronization

- The frontend (React/TypeScript) uses a tool that does **automatic TypeScript type generation** from the backend's OpenAPI schema (suggestion: `openapi-typescript`) — as a step in CI, when the backend changes the frontend's type files are regenerated too.
- This way, if a DTO field changes in the backend (e.g. `name` → `fullName`), the frontend gives an error at compile time — there is no inconsistency that goes unnoticed until runtime.

## Relationship with the agent workflow

When a coder agent works on the frontend side, it uses the backend's real DTO schema (not an assumption, but the generated type file) — the same logic as the "coder reads the relevant module's spec" principle in [agent-pipeline.md](../workflow/agent-pipeline.md): the agent does not guess, it reads from the generated source.

## Out of scope (not in the MVP)

- A general API documentation portal for external (third-party) API consumers — in the MVP the API is consumed only by our own frontend/mobile app and is not open to external developers.

## Related files

- [api-conventions.md](api-conventions.md)
- [../platform/code-style.md](code-style.md) — DTO naming rules (directly affects the readability of this schema)
