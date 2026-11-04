# Caching Strategy

## Problem

The `@RequiresModule` interceptor in [tenant-model.md](tenant-model.md) reads the tenant's `settings` field **on every request** (for the feature flag check). As traffic grows, this adds an extra DB query to every API call — becoming an unnecessary load as the number of tenants grows.

## Decision: tenant settings are cached

- Tenant `settings` (feature flags + branding) are held for each tenant in a short-lived (suggestion: 60 seconds) in-memory cache — a separate infrastructure such as Redis is not needed at MVP scale, and Spring's own `@Cacheable` mechanism is enough.
- **Cache invalidation rule:** when the `platform_owner` toggles a tenant's module on/off (see [roles-and-access.md](roles-and-access.md)), that tenant's cache entry is cleared **immediately** — we do not want the old setting to be used during the 60-second window, especially if a module has been turned off.
- **Mandatory key rule:** the cache key **always** starts with `tenant_id` (`@Cacheable(key = "#tenantId")`), and is never kept under a tenant-less key such as `settings` — see the "In-memory tenant isolation" section in [architecture-constraints.md](architecture-constraints.md). Because this cache is exactly the layer RLS cannot protect, a rule violation here wipes out all the benefit of RLS.

## What is cached and what is not

| Data | Cached? | Why |
|---|---|---|
| `tenant.settings` (feature flag) | ✅ Yes, short-lived | Read often, changes rarely |
| `membership` status (active/frozen) | ❌ No | In decisions involving money/access such as QR check-in, current data must always be read — see [concurrency-and-idempotency.md](concurrency-and-idempotency.md) |
| `member` list | ❌ No (in the MVP) | Changes often, the data volume per tenant is small, not worth the cache complexity |

## As scale grows

If we move from a single instance to multiple `api` instances (horizontal scaling), the in-memory cache becomes inconsistent across instances — at that point a move to a shared cache layer such as Redis is considered. A single instance is enough for the MVP, and this decision is not made now.

## Related files

- [tenant-model.md](tenant-model.md) — the data source being cached
- [database-conventions.md](database-conventions.md)
