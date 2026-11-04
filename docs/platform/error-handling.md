# Error Handling (Exception Strategy)

[api-conventions.md](api-conventions.md) defines the error format **on the wire** (`error.code`, `error.message`, `error.field`) but how an error is thrown in code and converted to this format had not been defined — if each module throws exceptions in its own style, format consistency cannot be guaranteed.

## Exception hierarchy

```
DomainException (abstract, defined in core)
 ├─ NotFoundException           → HTTP 404
 ├─ ValidationException         → HTTP 400 (if field is set)
 ├─ UnauthorizedException       → HTTP 401
 ├─ ForbiddenException          → HTTP 403 (e.g. an attempted tenant isolation violation)
 ├─ ConflictException           → HTTP 409 (e.g. an appointment conflict)
 └─ BusinessRuleException       → HTTP 422 (e.g. membership expired → MEMBERSHIP_EXPIRED)
```

- All modules throw **only** types in this hierarchy and do not define their own custom exception types — for a new error case one of the existing types is used and distinguished by the `code` field (e.g. `BusinessRuleException("MEMBERSHIP_EXPIRED", "The membership has expired.")`).
- `code` values carry a module prefix (`FITNESS_QR_EXPIRED`, `BERBER_SLOT_TAKEN`) — named consistently with the terms in [glossary.md](../glossary.md).

## Central conversion

A single `@ControllerAdvice` (global exception handler) in `core` catches every type in this hierarchy and converts it to the JSON envelope in [api-conventions.md](api-conventions.md). No controller formats errors with its own try/catch — this centrally guarantees that every endpoint returns errors in the same format.

## Unexpected errors

Every exception that does not fall into the hierarchy above (an unforeseen one) is converted by the `@ControllerAdvice` to HTTP 500 with the `INTERNAL_ERROR` code and is automatically reported to the error tracking system (Sentry) in [observability.md](../infra/observability.md) — stack traces or internal details are never leaked to the user.

## Related files

- [api-conventions.md](api-conventions.md) — the wire format
- [../infra/observability.md](../infra/observability.md) — tracking unexpected errors
- [code-review-checklist.md](../workflow/code-review-checklist.md) — checking during review whether this hierarchy is followed
