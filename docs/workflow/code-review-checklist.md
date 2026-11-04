# Code Review Checklist

The reviewer agent's job was defined in [agent-pipeline.md](agent-pipeline.md) as "code quality, security, logic checks" — this is too general; a concrete list was needed so that both an agent and a human check against the **same** criteria.

## Order matters — architecture first, then details

### 1. Is there a boundary violation (most critical, checked first)

- [ ] Does the PR touch anything outside its own module's folder? (see the dependency rule in [repo-structure.md](../repo-structure.md)) — if so, it is **rejected** and a separate core PR is requested
- [ ] Is there code in `modules/*` that imports another module's package? — if so, it is **rejected**
- [ ] Is there a new table that does not carry `tenant_id`? — if so, it is **rejected**, risk of an isolation violation

### 2. Terminology and naming

- [ ] Are the terms in [glossary.md](../glossary.md) used (no new name invented such as `customer` instead of `member`)?
- [ ] Does class/table naming comply with [code-style.md](../platform/code-style.md)?
- [ ] Does every comment fall under one of the 3 reasons allowed by [code-style.md](../platform/code-style.md) (why, a counter-intuitive rule, a workaround)? A comment that repeats what the code does, summarizes the call/file, or repeats something already explained elsewhere is **removed** — this is not a "nice to have", it is an item checked in every PR.

### 3. Security

- [ ] Is user input validated (see [security.md](../platform/security.md))?
- [ ] If a new endpoint falls into a category that requires rate limiting (public/login-like), has a limit been added?
- [ ] If there is raw SQL, is it parameterized (SQL injection risk)?
- [ ] Are secrets (API keys, passwords) embedded in the code? — if embedded, it is **rejected**

### 4. API contract

- [ ] Does the URL structure match the `/api/v1/<layer>/<resource>` scheme in [api-conventions.md](../platform/api-conventions.md)?
- [ ] Are error responses in the standard envelope format (`error.code`, `error.message`, `error.field`)?
- [ ] If it is a breaking change, was the versioning rule in [mobile-compatibility.md](../platform/mobile-compatibility.md) followed (was a new `v2` opened, or was `v1` silently broken)?

### 5. Business logic

- [ ] Is the "Expected behavior" in the issue the PR closes actually met?
- [ ] Is there a change that goes beyond the scope (see the "small PR rule" in [pr-and-definition-of-done.md](pr-and-definition-of-done.md))?
- [ ] If it touches a critical table (membership, payment, role), is the audit log triggered (see [audit-log.md](../platform/audit-log.md))?

### 6. Tests

- [ ] Are there unit + integration tests for the new endpoint/logic (see [testing-strategy.md](testing-strategy.md))?
- [ ] Has the tenant isolation test been broken (CI already checks this automatically, but the reviewer should also see the test result in the PR description)?

## Rejecting vs. leaving a comment

- Findings in **categories 1 and 3** (boundary violation, security) always block the PR — they are rejected automatically and a rework is requested.
- Findings in **categories 2, 4, 5, 6** are left as comments, and the coder agent (or a human) fixes and resubmits — it is not an automatic rejection but is expected to be resolved before merge.

## Related files

- [agent-pipeline.md](agent-pipeline.md) — how the reviewer agent uses this list
- [pr-and-definition-of-done.md](pr-and-definition-of-done.md)
