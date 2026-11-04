# Change Impact Analysis (Changing an Existing Concept)

[new-module-checklist.md](new-module-checklist.md) describes **adding something new**. This document covers a different scenario: changing **an existing concept that is already in use** (e.g. renaming the `membership` table to `subscription`, changing the meaning of a field). This is riskier than the new-module checklist because it touches something that code has already been written against.

## When to follow this process

- When a table/field/class is being **renamed**
- When the **meaning** of a concept changes (e.g. not adding a new value to `membership.status`, but changing what an existing value means)
- When an invariant in [domain-invariants.md](../platform/domain-invariants.md) is being **deliberately** relaxed/changed

Simple additions (a new field, a new endpoint) do not need this process — the normal [issue-management.md](issue-management.md) flow is enough.

## Steps

1. **Impact scan** — find which files the concept to be changed appears in from [docs-dependency-map.md](../docs-dependency-map.md); if the map is incomplete, scan the concept name across the entire documentation and code base with `grep`/search.
2. **Produce the impact list** — which files, which tables, which API fields are affected; this is written into an issue description (see the example below).
3. **Is an ADR needed?** — if the concept affects an item in [domain-invariants.md](../platform/domain-invariants.md) or a decision made in an [ADR](../adr/_template.md), the change is first justified with a new ADR (the old one is marked "abandoned", not modified — see the ADR immutability principle in [tech-stack.md](../platform/tech-stack.md)).
4. **Migration + code + documentation in a single PR chain** — a rename is never done in code only or in documentation only; both are updated together (see the migration rule in [database-conventions.md](../platform/database-conventions.md) — the old column is not dropped; a new column is added and data moved, then the old one is removed in a separate migration).
5. **Update the glossary** — the old term is added to the banned words table in [glossary.md](../glossary.md), and the new term is defined.

## Example: `Membership` → `Subscription` rename

```markdown
## Impact list (change-impact-analysis output)
- modules/core.md — membership data model section
- modules/fitness.md, modules/berber.md — core.membership references
- platform/tenant-model.md — "Membership" section
- platform/billing-model.md — tenant_payment.source_type enum value
- platform/domain-invariants.md — INV-1, INV-3, INV-7
- platform/event-catalog.md — MembershipExpiringEvent, MembershipFrozenEvent
- glossary.md — "membership" is added to the banned words table
- Migration: membership table → subscription (rename, no data loss)
```

Seeing this list, it is immediately clear that this **cannot be a single small PR** — the "small PR rule" in [pr-and-definition-of-done.md](pr-and-definition-of-done.md) does not apply here; concept changes like this are exceptionally split into multiple coordinated PRs, each with its own issue but all tied to a single parent issue (`epic`).

## Related files

- [../docs-dependency-map.md](../docs-dependency-map.md)
- [../platform/domain-invariants.md](../platform/domain-invariants.md)
- [new-module-checklist.md](new-module-checklist.md) — the opposite scenario (adding, not changing)
