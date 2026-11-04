# ADR 0010: Reflection in the audit mechanism — a deliberate exception

**Status:** Accepted
**Date:** 2026-07-18

## Context

[architecture-constraints.md](../platform/architecture-constraints.md) states as an absolute rule: *"NEVER build a hand-written mechanism outside the framework using Java reflection (Spring already uses reflection internally; application code does not repeat it)."*

`com.saas.core.audit.AuditListener`/`AuditWriter` (the implementation of [audit-log.md](../platform/audit-log.md)) use exactly this pattern: `AuditListener.snapshot()` walks the private fields of each `@Audited` entity by hand with `Field.setAccessible(true)` + `field.get(entity)`; `AuditWriter.requireFieldValue()` likewise reads the `tenantId`/`id` fields by hand through reflection. In a full Phase 1 review (see this ADR's "Historical context" section) this was flagged as a direct violation of the rule.

The alternative considered: rewriting it to use Jackson's own reflection (`ObjectMapper` + a custom `@AuditSensitive` redaction `BeanSerializerModifier`) — this would have fit the "the framework's reflection is fine" distinction in the rule's parenthetical.

## Decision

The existing mechanism is **not changed**; it is accepted as a deliberate exception.

Rationale: the whole point of `audit-log.md`'s design is that a new `@Audited` entity can be audited **without needing any code of its own** (just by adding an annotation) — this requires a fully generic mechanism that does not know the entities' field sets at compile time. Switching to Jackson would meet the same generic need, but:

- Rewriting an existing, tested, working mechanism (create/update/delete snapshots, `@AuditSensitive` redaction) so as to preserve `AuditListenerTest`'s behavior one-to-one carries real risk — the refactor itself is outside the scope of this review and calls for separate, careful work.
- The use of reflection itself was already reviewed in SonarCloud from a security/encapsulation standpoint and closed as Won't Fix with the same rationale ("generic access, no getter per entity") — this ADR formalizes the same rationale at the architecture-rule level.

## Consequences

- The "NEVER" list in `architecture-constraints.md` is considered **exempted** for this single mechanism — this exception is not a new precedent and cannot be extended elsewhere by saying "we need reflection."
- If this mechanism goes through a major revision in the future (e.g. if an `@Audited` entity carrying lazy associations is added in Phase 2 — `AuditListener`'s own Javadoc already states this is not supported today), a Jackson-based rewrite will be evaluated then, alongside a real need.

## Historical context

- **Original incident/finding:** During a review of all of Phase 1 by an independent agent, it was found that `AuditListener`/`AuditWriter` directly contradicted the reflection ban in `architecture-constraints.md`.
- **Rejected alternatives:** Rewrite with Jackson `ObjectMapper` + a custom `BeanSerializerModifier` — fully compliant with the architecture rule, but deferred beyond this ADR because it risks breaking the existing audit trail behavior.
- **Failure scenario:** Had this ADR not been written, an agent/human reading this code in the future might take the reflection usage for a "rule violation that must be fixed" and start a refactor unawares — this ADR records that the decision is deliberate and reasoned, not a random oversight.
