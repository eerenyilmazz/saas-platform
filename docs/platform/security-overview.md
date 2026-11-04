# Security — One-Page Overview

> This file is an **index, not a source** (the same pattern as [data-model-overview.md](../data-model-overview.md)). Security is spread across 8 separate files — here they are all listed in one place so that an agent/human can find the answer to "what should I check from a security standpoint" in a single file.

## Security, layer by layer

| Layer | Risk | Solution | File |
|---|---|---|---|
| Database | One tenant's data leaks to another tenant | Row-Level Security (RLS) | [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md), [ADR 0008](../adr/0008-row-level-security.md) |
| Application memory (RAM) | The layer RLS cannot protect — tenant data leaking through a static cache/singleton/ThreadLocal | Ban on static mutable state, tenant_id-keyed cache, ThreadLocal cleanup | [architecture-constraints.md](architecture-constraints.md) "In-memory tenant isolation" |
| API | Brute-force, abuse, DoS | Rate limiting, JWT token lifetime/refresh | [security.md](security.md) |
| Code (what we write ourselves) | SQL injection, hardcoded secrets, insecure deserialization | Static analysis (CodeQL/Semgrep) | [static-analysis.md](static-analysis.md) |
| Dependencies (third-party) | Library with a known CVE | Dependabot scanning + centralized BOM (preventing version bloat/jar hell) | [dependency-management.md](dependency-management.md) |
| Logging | PII logged in plain text | Phone/name masking | [../infra/observability.md](../infra/observability.md) "PII masking" |
| Error messages | Stack trace/internal detail leakage | Central `@ControllerAdvice`, internal details are never returned to the user | [error-handling.md](error-handling.md) |
| Access/permissions | Unauthorized tenant/role access | RLS + role hierarchy + logged support mode | [roles-and-access.md](roles-and-access.md), [support-access.md](support-access.md) |
| Personal data (KVKK) | Data processing/notification without consent | Explicit consent field, deletion endpoint, retention period | [kvkk-checklist.md](kvkk-checklist.md) |
| Traceability | No answer to "who did what" | Audit log (mandatory on critical tables) | [audit-log.md](audit-log.md) |

## To check on every new PR (summary)

This list is a concrete shortcut to categories 1 and 3 (boundary violation, security) of [code-review-checklist.md](../workflow/code-review-checklist.md):

- [ ] New table → are `tenant_id` + RLS enabled?
- [ ] New cache/static field → does the key start with `tenant_id`, is there a mutable singleton field?
- [ ] New endpoint → does it fall into a category that requires rate limiting (public/login-like)?
- [ ] New dependency → is it human-approved, was it added to the BOM?
- [ ] New log line → is PII written in plain text?
- [ ] New personal data field → does it fall within the scope of KVKK, is there a `consent_at` check?

## Related files

Each row in the table above already links to its own file — this file is only a map, the details are always in the source file.
