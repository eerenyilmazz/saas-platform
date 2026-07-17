# Kod Stili & İsimlendirme Kuralları

[dev-setup.md](../dev-setup.md) projeyi nasıl çalıştıracağını anlatıyor, bu dosya **nasıl yazacağını** anlatıyor. Amaç: farklı modüller farklı agent'lar/kişiler tarafından yazılsa bile kod tabanı tek bir elden çıkmış gibi görünsün.

## Java / Spring Boot katman isimlendirmesi

| Katman | Sonek | Örnek |
|---|---|---|
| REST giriş noktası | `Controller` | `FitnessCheckinController` |
| İş mantığı (arayüz) | `Service` | `MembershipService` |
| İş mantığı (uygulama) | `ServiceImpl` | `MembershipServiceImpl` |
| Veri erişimi | `Repository` | `MemberRepository` |
| Veritabanı varlığı | (sonek yok, tekil isim) | `Member`, `Membership` |
| İstek gövdesi | `Request` | `CreateMemberRequest` |
| Yanıt gövdesi | `Response` | `MemberResponse` |
| Entity ↔ DTO dönüşümü | `Mapper` | `MemberMapper` |
| Domain olayı | `Event` | `MembershipExpiringEvent` (bkz. [notifications.md](notifications.md)) |

Sınıf adları [glossary.md](../glossary.md)'deki terimleri birebir kullanır — `Customer` değil `Member`, `Business` değil `Tenant`.

`Request`/`Response` her zaman Java record'u olarak yazılır (immutable, alan listesi = constructor) — `LoginRequest`, `LoginResponse` gibi. `Request`'lerde alan doğrulaması Bean Validation (`@Valid`, `@NotBlank`, `@Email` vb.) ile yapılır, controller/servis içinde elle `if` kontrolü yazılmaz.

## Controller — asla iş mantığı yok

Bir controller sadece HTTP isteğini bağlar (deserialize/validate) ve ilgili servis metoduna devreder — key üretimi, koşul kontrolü, try/catch ile durum kaydı gibi hiçbir mantık controller'da yazılmaz. Bunun testi basit: bir controller metodu MockMvc olmadan, düz bir servis çağrısı olarak okunabilmeli (genelde tek satır `return xService.y(request);`).

## Servis katmanı — interface + impl ayrımı

Her iş mantığı servisi bir **arayüz** (`MembershipService`) ve onu uygulayan bir **sınıf** (`MembershipServiceImpl`, `@Service` burada) olarak yazılır. Controller (ve servisi çağıran her yer) sadece arayüze bağımlı olur — Spring hangi implementasyonu enjekte edeceğini bilir, çağıran taraf bunu bilmek zorunda değildir.

- Bu ayrım her `@Component`/`@Configuration` sınıfı için değil, **iş akışı taşıyan servisler** için geçerlidir (`AuthenticationService`, `MembershipService` gibi). Altyapı sınıfları (`JwtAuthenticationFilter`, `TenantAwareDataSource`, bir `@ConfigurationProperties` sınıfı) tek bir somut sınıf olarak kalır — bunlara interface eklemek gereksiz bir katman olur.
- İmplementasyon şişmeye başlarsa (tek bir sınıf birden fazla sorumluluk taşımaya başlarsa) ayrı, odaklı yardımcı sınıflara bölünür (bkz. [architecture-constraints.md](architecture-constraints.md)) — `ServiceImpl` her şeyi kendi içinde çözen bir "tanrı sınıf" olmaz.
- Gerekirse repository doğrudan `ServiceImpl` içinden çağrılır, ayrı bir soyutlamaya gerek yoktur.

## Lombok — constructor/getter boilerplate'i azaltır

Bağımlılık enjeksiyonu yapan sınıflarda (servis impl'leri, controller'lar, config sınıfları) constructor elle yazılmaz, `@RequiredArgsConstructor` (Lombok) kullanılır; alanlar `private final` kalır. Getter'lar için `@Getter` kullanılır.

```java
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final AuditWriter auditWriter;

    ...
}
```

`@RequiredArgsConstructor` sadece constructor'ın **tek işi** alanları atamaksa kullanılır. Constructor'ın ek bir işi varsa (bir statik köprü alanını doldurmak, `super(...)` çağırmak, bir doğrulama yapmak) elle yazılır — Lombok bunu üretemez, zorlamak yerine constructor elle kalır (bkz. `AuditWriter`, `TenantAwareDataSource`).

- JPA entity'lerinde `@Getter` kullanılır, ama **iş anlamı taşıyan özel constructor'lar elle yazılır** (`new User(tenantId, email, passwordHash, role)` gibi) — Lombok'un `@AllArgsConstructor`/`@RequiredArgsConstructor`'ı bunları üretemez, çünkü genelde tüm alanları değil anlamlı bir alt kümesini alırlar. JPA'nın istediği boş constructor için `@NoArgsConstructor(access = AccessLevel.PROTECTED)` kullanılır.
- Entity'lerde `@Setter` kullanılmaz — durum değişiklikleri (`freeze()`, `changeRole()` gibi) adı iş kuralını anlatan metodlarla yapılır, generic bir setter ile değil.

## Config değerleri — asla kod içine gömülmez

Ortam/dağıtıma göre değişebilecek ya da ayarlanabilir olması makul her değer (süre, eşik, URL, secret) bir `@ConfigurationProperties` sınıfına (`JwtProperties`, `LoginRateLimitProperties` gibi) yazılır, `application*.properties`'ten okunur — koda `static final` bir sabit olarak veya `@Value` ile dağınık şekilde gömülmez. İhtiyacı olan sınıf bu properties sınıfını enjekte eder.

- Her mantıksal grup (JWT, rate limiting, ...) kendi properties sınıfını alır — tek bir dev "AppConfig" sınıfında toplamak, [decision-tree.md](decision-tree.md)'nin "erken soyutlama" uyarısına ters düşer ve gruplar arası ilgisiz alanları bir araya getirir.
- Gerçekten sabit olan, ortamdan asla etkilenmeyen değerler (örn. bir şifre üretim algoritmasının karakter kümesi) bu kuralın dışındadır — ayrım [decision-tree.md](decision-tree.md)'deki "gerçek kolon mu, esnek alan mı" mantığına benzer: **"biri bunu tenant/ortam bazında değiştirmek ister mi?"** sorusu evetse config'e gider, hayırsa kod sabiti kalır.

## Mapper — Entity ↔ DTO dönüşümü elle yazılmaz

Bir controller/servis bir entity'yi doğrudan bir `Response`'a çeviriyorsa (alan alan kopyalama) bu kod bir `XMapper` arayüzüne çıkarılır, MapStruct ile üretilir — Lombok constructor/getter'ı nasıl üretiyorsa, MapStruct de bu dönüşüm kodunu aynı mantıkla (annotation processor, derleme zamanında) üretir.

```java
@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
}
```

- `componentModel = "spring"` zorunlu — bu olmadan üretilen sınıf bir Spring bean'i olmaz, `@Autowired` edilemez.
- Mapper, ilgili modülün `mapper/` klasöründe yaşar (`com.saas.core.membership.mapper.MemberMapper` gibi) — [code-style.md](code-style.md)'nin paket içi klasörleme deseninin bir uzantısı.
- Basit, tek yönlü (entity → response) dönüşümler için yeterlidir; `Request` → entity gibi request tarafında da ihtiyaç çıkarsa aynı arayüze `toEntity(...)` metodu eklenir, ayrı bir sınıfa çıkarılmaz.
- MapStruct, Lombok'un ürettiği getter'ları okuyabilmek için `lombok-mapstruct-binding` bağımlılığına (build.gradle) ihtiyaç duyar — bu olmadan iki annotation processor'ın çalışma sırası garanti edilmez.

## Dil kuralı — kod her zaman İngilizce

**Kod (sınıf adı, alan adı, kolon adı, değişken adı, endpoint path'i) her zaman İngilizce yazılır — istisnasız.** Dokümanların Türkçe olması (bu proje Türkiye pazarı için yazılıyor) koda yansımaz; kod ile doküman **farklı dillerdedir**, bu bilinçli bir ayrım:

- **Türkçe:** Bu dosyalar, PR açıklamaları, issue'lar, code review yorumları, kullanıcıya gösterilen metin ([localization.md](localization.md)'deki `tr.json`/`messages_tr.properties` içeriği).
- **İngilizce:** Sınıf/metod/değişken adı, veritabanı tablo/kolon adı, API endpoint path'i, commit mesajı, kod içi (varsa) yorum.

| Yanlış (Türkçe kod) | Doğru (İngilizce kod) |
|---|---|
| `ad`, `telefon` | `name`, `phone` |
| `süre`, `tarih` | `duration`, `date` / `scheduled_at` |
| `başlangıç`/`bitiş` | `start_at`/`end_at` |
| `renk`, `yöntem`, `tutar` | `color`, `method`, `amount` |
| `durum`, `sebep` | `status`, `reason` |

**Retrofit notu:** Bu dosya seti daha önce birkaç yerde örnek SQL/JSON'da Türkçe kolon adı (`ad`, `telefon`, `kaynak_tipi` vb.) kullanmıştı — bunlar bu kuralla tutarlı hale getirilmek üzere güncelleniyor (bkz. [modules/core.md](../modules/core.md), [data-model-overview.md](../data-model-overview.md)). `kaynak_tipi`/`kaynak_id` artık `source_type`/`source_id`.

## Loglama disiplini — gereksiz log yok

[../infra/observability.md](../infra/observability.md) yapılandırılmış loglamayı tanımlıyordu ama "ne zaman log ATILMAZ" hiç söylenmemişti. Kural: **her satır bir maliyettir** (disk, arama gürültüsü, PII sızıntı riski artışı) — bir log satırı ancak şu durumlarda yazılır:

- Bir hata/beklenmeyen durum oluştuğunda (`WARN`/`ERROR`)
- Bir iş kararı sonradan izlenmesi gereken bir yan etki ürettiğinde (örn. ödeme durumu değişti) — bu zaten çoğunlukla [audit-log.md](audit-log.md)'un işi, ayrıca `INFO` logu **tekrar** yazılmaz
- Bir dış servis çağrısının başlangıcı/sonucu (bkz. [external-integration-resilience.md](external-integration-resilience.md))

**Yasak:** "buraya geldi" tarzı debug amaçlı `INFO`/`DEBUG` logları production koduna commit edilmez (geliştirme sırasında kullanılıp PR'a girmeden temizlenir); bir döngü/loop içinde her iterasyonda log atılmaz; zaten `audit_log` tablosuna yazılan bir olay ayrıca application log'una da tekrar yazılmaz (aynı bilginin iki farklı yerde tutulması, hangisinin doğru olduğu sorusunu doğurur).

## Yorum disiplini — varsayılan yorumsuz

**Varsayılan: yorum yazılmaz.** İyi isimlendirilmiş bir sınıf/metod/değişken zaten "ne yaptığını" anlatır — bunu tekrar eden bir yorum (`// üyeyi kaydet` diye `saveMember()` çağrısının üstüne) gürültüdür, silinmesi gerekir.

Bir yorum **sadece** şu durumlarda yazılır — hiçbiri "ne yapıyor" değil, hepsi "neden böyle" sorusuna cevap verir:

- Kodun **neden** böyle yazıldığını açıklamak gerekiyorsa (örn. "burada `@Lazy` kullanılmıyor, bkz. [architecture-constraints.md](architecture-constraints.md)")
- Sezgiye aykırı bir iş kuralı/kısıt uygulanıyorsa (örn. neden bir `ConflictException` yerine sessizce mevcut kayıt dönüldüğü, bkz. [concurrency-and-idempotency.md](concurrency-and-idempotency.md) idempotency mantığı)
- Bilinen bir sınırlamanın/geçici çözümün (workaround) gerekçesi belirtiliyorsa

Ne yaptığını anlatan, çağrıyı/dosyayı/PR'ı referans veren ya da "TODO" bırakıp asla dönülmeyen yorumlar [code-review-checklist.md](../workflow/code-review-checklist.md)'de bulgu olarak işaretlenir.

## Paket içi klasörleme

Her modül kendi içinde aynı alt yapıyı tekrarlar:

```
com.saas.modules.fitness/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

Bu, bir agent'ın "fitness modülünde nereye kod yazacağını" başka bir modülün yapısına bakarak tahmin edebilmesini sağlar.

## Commit mesaj formatı

```
<modül>: <kısa özet, emir kipi>

örn:
fitness: QR token üretim endpoint'i eklendi
core: membership dondurma mantığı düzeltildi
```

- İlk kelime her zaman etkilenen modül (`core`, `fitness`, `berber`, `platform`, `infra`, `docs`).
- Özet emir kipinde ("eklendi" değil "ekle" de olur, ekip içinde hangisi seçilirse tutarlı kullanılsın — öneri: geçmiş zaman, çünkü "bu commit ne yaptı"yı anlatır).

## Frontend (React/TypeScript)

- Bileşen dosyaları `PascalCase.tsx` (`MemberList.tsx`), hook'lar `useXxx.ts` (`useMembership.ts`).
- API çağrıları için tek bir `api/` klasörü, her modülün kendi dosyası (`api/fitness.ts`, `api/berber.ts`) — [api-conventions.md](api-conventions.md)'deki URL şemasını birebir yansıtır.
- Renk, tarih, metin doğrudan yazılmaz — bkz. [theming.md](theming.md) (renk token'ları) ve [localization.md](localization.md) (metin/tarih dosyaları).

## Lint/format (biçim katmanı)

- Backend: Spring Boot standart Checkstyle/Spotless kuralları (proje iskeletiyle birlikte Faz 0'da kurulur).
- Frontend: ESLint + Prettier, varsayılan kurallar — özelleştirme ihtiyacı çıkarsa buraya eklenir.
- CI, lint hatası olan PR'ı reddeder (bkz. [workflow/agent-pipeline.md](../workflow/agent-pipeline.md) — reviewer agent bunu otomatik yakalar).
- Biçimin ötesinde (karmaşıklık, güvenlik) statik analiz katmanı ayrı: bkz. [static-analysis.md](static-analysis.md).

## İlgili dosyalar

- [glossary.md](../glossary.md) — isimlendirmede kullanılacak terimler
- [dev-setup.md](../dev-setup.md) — yerel ortamda lint/format çalıştırma
- [static-analysis.md](static-analysis.md) — kod kalitesi/güvenlik statik analizi
- [theming.md](theming.md), [localization.md](localization.md)
