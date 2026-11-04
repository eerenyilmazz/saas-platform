# Example Feature: End-to-End "Create Member"

Instead of reading 50 files one by one, this document exists so that a coder agent (or developer) can see all the conventions by looking at **a single realistic example**. Every step below is written according to the rule in the relevant file — the code is not real (there is no project skeleton yet), but the **pattern** is real, and Phase 1 will be written exactly in this shape.

## 0. Issue

Using the template in [issue-management.md](issue-management.md):

```markdown
Title: Create member endpoint
Labels: module:core, type:feature, priority:high

## Context
A tenant_admin must be able to add a new member to their own business — see roadmap Phase 1.

## Expected behavior
POST /api/v1/members creates a new member record; no record is created without KVKK (Turkish personal data protection law) explicit consent.

## Definition of Done
- [ ] Code written
- [ ] Tests written
- [ ] data-model-overview.md updated (member already exists, no change)
```

## 1. Branch

Per [branch-strategy.md](branch-strategy.md): `feature/core-create-member`

## 2. Migration

Per the rules in [database-conventions.md](../platform/database-conventions.md) — the `member` table was already defined in [modules/core.md](../modules/core.md); this migration creates it:

```sql
-- V202607171530__create_member_table.sql (timestamped naming, see database-conventions.md)
CREATE TABLE member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    consent_at TIMESTAMPTZ,
    user_id UUID REFERENCES app_user(id),
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- mandatory tenant_id index (see database-conventions.md)
CREATE INDEX idx_member_tenant_id ON member(tenant_id);
CREATE INDEX idx_member_tenant_phone ON member(tenant_id, phone);
```

## 3. Entity

Per the naming in [code-style.md](../platform/code-style.md) — no suffix, singular name, field names in English (see the "Language rule" in code-style.md):

```java
package com.saas.core.member;

@Entity
@Table(name = "member")
class Member {
    @Id UUID id;
    UUID tenantId;
    String name;
    String phone;
    Instant consentAt;
    UUID userId; // nullable
    Instant deletedAt; // soft delete, see database-conventions.md
}
```

## 4. Repository

```java
interface MemberRepository extends JpaRepository<Member, UUID> {
    // the tenant_id filter is applied centrally in the shared base repository in core (see tenant-model.md)
}
```

## 5. Service — the business rule lives here

Per [architecture-constraints.md](../platform/architecture-constraints.md), the business rule belongs here, not in the Controller:

```java
@Service
class MemberService {

    Member create(CreateMemberRequest request, TenantContext ctx) {
        if (request.consentGiven() != true) {
            // per the hierarchy in error-handling.md
            throw new ValidationException("consentGiven", "A member cannot be created without KVKK explicit consent.");
        }

        Member member = new Member(ctx.tenantId(), request.name(), request.phone(), Instant.now());
        memberRepository.save(member);

        // from the catalog in event-catalog.md, no name was made up
        eventPublisher.publish(new MemberCreated(member.getId(), ctx.tenantId()));

        return member;
    }
}
```

The `MemberCreated` event is fired **after** the transaction commits (see [external-integration-resilience.md](../platform/external-integration-resilience.md)) — `core.audit` listens to this event and writes to [audit-log.md](../platform/audit-log.md); `MemberService` itself does not write to audit_log directly.

## 6. Controller — only delegates

```java
@RestController
@RequestMapping("/api/v1/members")
class MemberController {

    @PostMapping
    MemberResponse create(@Valid @RequestBody CreateMemberRequest request, TenantContext ctx) {
        Member member = memberService.create(request, ctx);
        return MemberResponse.from(member); // an entity is never returned directly, see anti-patterns.md
    }
}
```

## 7. DTOs

The `Request`/`Response` suffix from [code-style.md](../platform/code-style.md):

```java
record CreateMemberRequest(String name, String phone, Boolean consentGiven) {}
record MemberResponse(UUID id, String name, String phone) {
    static MemberResponse from(Member m) { return new MemberResponse(m.getId(), m.getName(), m.getPhone()); }
}
```

## 8. Error case — the envelope in api-conventions.md is produced automatically

If a `ValidationException` is thrown, the central `@ControllerAdvice` in `core` (see [error-handling.md](../platform/error-handling.md)) automatically turns it into this response:

```json
{ "error": { "code": "VALIDATION_ERROR", "message": "A member cannot be created without KVKK explicit consent.", "field": "consentGiven" } }
```

Neither the Controller nor the Service produces this JSON by hand. (Per [localization.md](../platform/localization.md), the `message` field comes from the Turkish message file and no literal string is written in code — it is written as plain text here for readability.)

## 9. Tests

The three levels in [testing-strategy.md](testing-strategy.md) — test method names are also in English (see the "Language rule" in code-style.md):

```java
// Unit test — MemberServiceTest
@Test void throwsWhenConsentNotGiven() { ... }

// Integration test — MemberControllerIntegrationTest (Testcontainers + real PostgreSQL)
@Test void postMembersCreatesNewMember() { ... }

// Tenant isolation test (at core level, mandatory CI gate)
@Test void memberFromAnotherTenantIsNotVisible() { ... }
```

## 10. PR

Opened with the template in [pr-and-definition-of-done.md](pr-and-definition-of-done.md), `Closes #<issue number>`, every box in the Definition of Done is checked, and it goes through the 6 categories of [code-review-checklist.md](code-review-checklist.md).

---

This flow is repeated exactly for every new endpoint — the module changes (`fitness`, `berber`), the layer order and naming do not.
