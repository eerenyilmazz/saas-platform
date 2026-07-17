# Kod Stili & İsimlendirme Kuralları

[dev-setup.md](../dev-setup.md) projeyi nasıl çalıştıracağını anlatıyor, bu dosya **nasıl yazacağını** anlatıyor. Amaç: farklı modüller farklı agent'lar/kişiler tarafından yazılsa bile kod tabanı tek bir elden çıkmış gibi görünsün.

## Java / Spring Boot katman isimlendirmesi

| Katman | Sonek | Örnek |
|---|---|---|
| REST giriş noktası | `Controller` | `FitnessCheckinController` |
| İş mantığı | `Service` | `MembershipService` |
| Veri erişimi | `Repository` | `MemberRepository` |
| Veritabanı varlığı | (sonek yok, tekil isim) | `Member`, `Membership` |
| İstek gövdesi | `Request` | `CreateMemberRequest` |
| Yanıt gövdesi | `Response` | `MemberResponse` |
| Domain olayı | `Event` | `MembershipExpiringEvent` (bkz. [notifications.md](notifications.md)) |

Sınıf adları [glossary.md](../glossary.md)'deki terimleri birebir kullanır — `Customer` değil `Member`, `Business` değil `Tenant`.

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
