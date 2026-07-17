# Mimari Kısıtlar (İhlal Edilemez Kurallar)

Şu ana kadarki dosyalar mimariyi **anlatıyordu**; bu dosya mimariyi **zorluyor**. Aşağıdaki kurallar öneri değil, ihlal edilemez sınırlardır — bir coder agent bunlardan birini ihlal ederse, [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 1. kategorisi (sınır ihlali) gereği PR otomatik reddedilir, tartışmaya açılmaz.

## ASLA yapılmayacaklar

- **ASLA** bir `Controller`, `Repository`'yi doğrudan çağırmaz — her zaman `Service` katmanından geçer (bkz. [code-style.md](code-style.md) katman yapısı).
- **ASLA** bir modül (`modules/fitness`), başka bir modülün (`modules/berber`) entity/repository/service sınıfını import etmez (bkz. [repo-structure.md](../repo-structure.md) bağımlılık kuralı, [bounded-context-matrix.md](bounded-context-matrix.md)).
- **ASLA** ortak bir "util" sınıfı iş kuralı (business logic) içermez — util sadece saf, durumsuz yardımcı fonksiyon barındırır (örn. tarih biçimlendirme evet, üyelik süresi hesaplama hayır — ikincisi `MembershipService`'e ait).
- **ASLA** circular dependency oluşturulmaz (A modülü B'ye, B de A'ya bağımlı olamaz) — bu zaten [repo-structure.md](../repo-structure.md)'deki tek yönlü bağımlılık grafiğiyle yapısal olarak engellenir; bir circular dependency **ihtiyacı doğduysa**, bu bir tasarım hatasının belirtisidir, `@Lazy` ile "çözülmez", modül sınırı yeniden düşünülür.
- **ASLA** Java reflection ile framework dışı, elle yazılmış bir mekanizma kurulmaz (Spring zaten reflection'ı kendi içinde kullanır, uygulama kodu bunu tekrar etmez).
- **ASLA** bir `Controller` iş kuralı (if/else zinciriyle karar verme, hesaplama) içermez — controller sadece istek/yanıtı `Service`'e devreder (bkz. [anti-patterns.md](anti-patterns.md) "Business Logic Controller").
- **ASLA** bir sınıfta (özellikle Spring'in varsayılan **singleton scope**'undaki `@Service`/`@Component`) mutable, istek/tenant'a özel durum tutan bir `field` bulunmaz — bkz. aşağıdaki "Bellek seviyesinde tenant izolasyonu".
- **ASLA** bir modül kendi `pom.xml`/`build.gradle` dosyasında bağımsız bir kütüphane sürümü tanımlamaz — sürümler sadece root projedeki BOM'dan gelir (bkz. [dependency-management.md](dependency-management.md)).
- **ASLA** iş mantığı (`Service` katmanı) içinde `if (tenantId.equals("..."))` gibi belirli bir tenant'a özel dallanma yazılmaz — bkz. [tenant-customization.md](tenant-customization.md).

## Bellek seviyesinde tenant izolasyonu (RLS'in koruyamadığı katman)

[ADR 0008](../adr/0008-row-level-security.md)'deki RLS, veritabanından okunan veriyi korur — ama veri bir kere DB'den **yasal olarak** çekilip uygulama belleğine (RAM) alındıktan sonra, RLS'in hiçbir etkisi kalmaz. Bir sonraki tenant'ın isteğini işleyen aynı thread/instance, önceki tenant'ın verisine bellek üzerinden erişebilir hale gelebilir — bu, RLS'in **kapsamadığı**, tamamen uygulama koduna bağlı bir risktir.

- **Static/global mutable state yasak:** `static` bir `Map`/cache/singleton alanı, arka planda tüm tenant'lar arasında paylaşılır. Böyle bir yapı **kesinlikle gerekiyorsa** (örn. bir cache), anahtar her zaman `tenant_id` ile başlamak zorundadır (`tenant_id + ":" + key`) — bkz. [caching-strategy.md](caching-strategy.md).
- **Singleton bean'lerde mutable field yasak:** Spring'in varsayılan `@Service` scope'u singleton'dır — bir istek işlenirken bu bean'e mutable bir alana (örn. "şu anki tenant" gibi) değer atamak, o değerin başka bir eşzamanlı isteğe (başka bir tenant'a) sızmasına yol açar. Tenant/istek bazlı veri **her zaman** metod parametresi ya da `ThreadLocal` üzerinden taşınır, asla bean field'ında değil.
- **ThreadLocal kullanılıyorsa zorunlu temizlik:** Uygulama sunucusu thread'leri **yeniden kullanır** (thread pool) — bir istek bitince `ThreadLocal` içeriği elle temizlenmezse, aynı thread'i sonraki isteğinde alan **farklı bir tenant** önceki tenant'ın verisini görebilir. Her `ThreadLocal.set(...)` çağrısının karşılığında bir `finally` bloğunda `ThreadLocal.remove()` **zorunludur** — bu, [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 1. kategorisinde (sınır ihlali, en ciddi seviye) aranır.
- **Denetim:** Bu üç kural da [static-analysis.md](static-analysis.md)'ye özel bir kural seti olarak eklenir (örn. Checkstyle/ArchUnit ile "singleton bean'de mutable field yasak" kontrolü) — sadece code review'a güvenilmez, çünkü bu tür bir hata testlerde genelde görünmez (tek tenant'lı test senaryosunda sızıntı tetiklenmez), sadece production'da eşzamanlı çoklu tenant trafiğinde ortaya çıkar.

## Karmaşıklık bütçesi (rehber sayılar)

Sayısal bir üst sınır, agent'ların büyümesine izin verdiği "God Class" riskini erken keser:

| Birim | Üst sınır (rehber) |
|---|---|
| Method | ~30 satır |
| Sınıf | ~500 satır |
| Service sınıfı (tek bir modülün ana iş mantığı) | ~200 satır — aşılıyorsa muhtemelen birden fazla sorumluluk taşıyor, bölünmeli |

Bu sayılar [static-analysis.md](static-analysis.md)'deki cyclomatic complexity uyarısıyla aynı mantıkta: mekanik bir yasa değil, "muhtemelen bölünmeli" sinyali — ama bir agent net bir sayı olmadan "çok mu uzun" sorusuna kendi kendine karar veremez, bu yüzden burada somutlaştırıldı.

## Bu kısıtların denetlenmesi

Mümkün olduğunca bu kurallar **prosa değil, otomatik test** ile denetlenir — bir ArchUnit test suite'i (`core`'da, tüm modülleri tarayan) yukarıdaki "modül A, modül B'yi import edemez" ve "Controller, Repository'yi çağıramaz" kurallarını CI'da otomatik doğrular, insan/reviewer agent'ın gözden kaçırma riski ortadan kalkar. Bu test suite'i Faz 0'da kurulur (bkz. [roadmap/phases.md](../roadmap/phases.md)).

## İlgili dosyalar

- [bounded-context-matrix.md](bounded-context-matrix.md) — hangi modülün hangisini görebileceği
- [anti-patterns.md](anti-patterns.md) — yasaklı kod kalıpları
- [static-analysis.md](static-analysis.md) — otomatik denetleme katmanı
- [caching-strategy.md](caching-strategy.md) — cache anahtarlarının tenant_id kuralı
- [dependency-management.md](dependency-management.md) — BOM/merkezi sürüm zorunluluğu
- [tenant-customization.md](tenant-customization.md) — tenant'a özel isteklerin doğru çözüm yeri
- [../adr/0008-row-level-security.md](../adr/0008-row-level-security.md), [../adr/0009-flyway-timestamp-versioning.md](../adr/0009-flyway-timestamp-versioning.md)
