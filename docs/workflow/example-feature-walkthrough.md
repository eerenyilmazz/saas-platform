# Örnek Özellik: Uçtan Uca "Üye Oluştur" (Create Member)

50 dosyayı tek tek okumak yerine, bir coder agent'ın (ya da geliştiricinin) **tek bir gerçekçi örneğe** bakarak tüm konvansiyonları görebilmesi için bu doküman var. Aşağıdaki her adım, ilgili dosyadaki kurala göre yazıldı — kod gerçek değil (henüz proje iskeleti yok) ama **desen** gerçek, Faz 1'de birebir bu şekle göre yazılacak.

## 0. Issue

[issue-management.md](issue-management.md)'deki şablona göre:

```markdown
Başlık: Üye oluşturma endpoint'i
Etiketler: module:core, type:feature, priority:high

## Bağlam
tenant_admin, kendi işletmesine yeni bir üye (member) ekleyebilmeli — bkz. roadmap Faz 1.

## Beklenen davranış
POST /api/v1/members ile yeni bir member kaydı oluşturulur, KVKK açık rızası alınmadan kayıt oluşmaz.

## Definition of Done
- [ ] Kod yazıldı
- [ ] Test yazıldı
- [ ] data-model-overview.md güncellendi (member zaten var, değişiklik yok)
```

## 1. Branch

[branch-strategy.md](branch-strategy.md)'ye göre: `feature/core-create-member`

## 2. Migration

[database-conventions.md](../platform/database-conventions.md) kurallarına göre — `member` tablosu zaten [modules/core.md](../modules/core.md)'da tanımlıydı, bu migration onu oluşturur:

```sql
-- V202607171530__create_member_table.sql (zaman damgalı isimlendirme, bkz. database-conventions.md)
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

-- tenant_id zorunlu index (bkz. database-conventions.md)
CREATE INDEX idx_member_tenant_id ON member(tenant_id);
CREATE INDEX idx_member_tenant_phone ON member(tenant_id, phone);
```

## 3. Entity

[code-style.md](../platform/code-style.md)'deki isimlendirmeye göre — sonek yok, tekil isim, alan adları İngilizce (bkz. code-style.md "Dil kuralı"):

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
    Instant deletedAt; // soft delete, bkz. database-conventions.md
}
```

## 4. Repository

```java
interface MemberRepository extends JpaRepository<Member, UUID> {
    // tenant_id filtresi core'daki ortak base repository'de merkezi uygulanır (bkz. tenant-model.md)
}
```

## 5. Service — iş kuralı burada

[architecture-constraints.md](../platform/architecture-constraints.md) gereği iş kuralı Controller'da değil, burada:

```java
@Service
class MemberService {

    Member create(CreateMemberRequest request, TenantContext ctx) {
        if (request.consentGiven() != true) {
            // error-handling.md'deki hiyerarşiye göre
            throw new ValidationException("consentGiven", "KVKK açık rızası olmadan üye oluşturulamaz.");
        }

        Member member = new Member(ctx.tenantId(), request.name(), request.phone(), Instant.now());
        memberRepository.save(member);

        // event-catalog.md'deki katalogdan, isim uydurulmadı
        eventPublisher.publish(new MemberCreated(member.getId(), ctx.tenantId()));

        return member;
    }
}
```

`MemberCreated` olayı, transaction commit olduktan **sonra** tetiklenir (bkz. [external-integration-resilience.md](../platform/external-integration-resilience.md)) — `core.audit` bu olayı dinleyip [audit-log.md](../platform/audit-log.md)'a yazar, `MemberService`'in kendisi audit_log'a doğrudan yazmaz.

## 6. Controller — sadece devreder

```java
@RestController
@RequestMapping("/api/v1/members")
class MemberController {

    @PostMapping
    MemberResponse create(@Valid @RequestBody CreateMemberRequest request, TenantContext ctx) {
        Member member = memberService.create(request, ctx);
        return MemberResponse.from(member); // entity asla doğrudan dönülmez, bkz. anti-patterns.md
    }
}
```

## 7. DTO'lar

[code-style.md](../platform/code-style.md)'deki `Request`/`Response` soneki:

```java
record CreateMemberRequest(String name, String phone, Boolean consentGiven) {}
record MemberResponse(UUID id, String name, String phone) {
    static MemberResponse from(Member m) { return new MemberResponse(m.getId(), m.getName(), m.getPhone()); }
}
```

## 8. Hata durumu — api-conventions.md'deki zarf otomatik üretilir

`ValidationException` fırlatılırsa, `core`'daki merkezi `@ControllerAdvice` (bkz. [error-handling.md](../platform/error-handling.md)) bunu otomatik şu yanıta çevirir:

```json
{ "error": { "code": "VALIDATION_ERROR", "message": "KVKK açık rızası olmadan üye oluşturulamaz.", "field": "consentGiven" } }
```

Controller ya da Service bu JSON'ı elle üretmez. (`message` alanı [localization.md](../platform/localization.md) gereği Türkçe mesaj dosyasından gelir, kodda literal string yazılmaz — burada okunabilirlik için düz yazıldı.)

## 9. Testler

[testing-strategy.md](testing-strategy.md)'deki üç seviye — test metod adları da İngilizce (bkz. code-style.md "Dil kuralı"):

```java
// Birim testi — MemberServiceTest
@Test void throwsWhenConsentNotGiven() { ... }

// Entegrasyon testi — MemberControllerIntegrationTest (Testcontainers + gerçek PostgreSQL)
@Test void postMembersCreatesNewMember() { ... }

// Tenant izolasyon testi (core seviyesinde, zorunlu CI gate)
@Test void memberFromAnotherTenantIsNotVisible() { ... }
```

## 10. PR

[pr-and-definition-of-done.md](pr-and-definition-of-done.md)'daki şablonla açılır, `Closes #<issue numarası>`, Definition of Done'daki tüm kutular işaretlenir, [code-review-checklist.md](code-review-checklist.md)'nin 6 kategorisinden geçer.

---

Bu akış her yeni endpoint için birebir tekrarlanır — modül değişir (`fitness`, `berber`), katman sırası ve isimlendirme değişmez.
