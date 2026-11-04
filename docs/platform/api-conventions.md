# API Contract

All modules follow the same REST rules — otherwise each module writes in its own style, creating inconsistency for the frontend and agents.

## URL structure

```
/api/v1/<layer>/<resource>
```

- Platform layer: `/api/v1/platform/tenants`, `/api/v1/platform/subscriptions`
- Core: `/api/v1/members`, `/api/v1/memberships`, `/api/v1/auth`
- Module: `/api/v1/fitness/checkins`, `/api/v1/berber/appointments`

The version (`v1`) is mandatory from the start — if a breaking change is needed later, `v2` is opened and `v1` lives in parallel for a while.

## HTTP methods

Standard REST: `GET` (read), `POST` (create), `PUT`/`PATCH` (update), `DELETE` (delete — mostly soft-delete; for the code-level `deleted_at` rule see [database-conventions.md](database-conventions.md), for the KVKK scope see [kvkk-checklist.md](kvkk-checklist.md)).

## Error format

All errors are returned in a single envelope format — for how this format is produced in code (the exception hierarchy) see [error-handling.md](error-handling.md):

```json
{
  "error": {
    "code": "MEMBERSHIP_EXPIRED",
    "message": "The membership has expired.",
    "field": null
  }
}
```

- `code` — a fixed, machine-readable error code (the frontend changes behavior based on this code, not on the message text)
- `message` — the text shown to the user (Turkish, taken from the message bundle — see [localization.md](localization.md); shown in English here for readability)
- `field` — if it is a validation error, which field it relates to; otherwise `null`

## Pagination

Endpoints that return lists take `page` and `size` query parameters, and the response is:

```json
{ "content": [...], "page": 0, "size": 20, "totalElements": 134 }
```

## Authentication

The `Authorization: Bearer <JWT>` header is mandatory (except for auth endpoints). The JWT carries `tenant_id` and `role` claims — on every request, tenant isolation and permission checks are done according to these claims (see [tenant-model.md](tenant-model.md)). Token lifetime/refresh details: [security.md](security.md).

## Related files

- [security.md](security.md) — rate limiting, token lifetime, input validation
- [mobile-compatibility.md](mobile-compatibility.md) — how breaking changes affect the mobile app, the `v1`/`v2` lifecycle
- [error-handling.md](error-handling.md) — the exception hierarchy, producing the error format in code
- [database-conventions.md](database-conventions.md) — migration rules, index strategy, soft-delete
- [api-documentation.md](api-documentation.md) — how this contract is reflected in OpenAPI/Swagger
