# Modüler SaaS — Doküman İndeksi

> **Yeni bir agent/oturumsan önce [kickoff.md](kickoff.md)'u oku.** Orada açık soruları kullanıcıya nasıl soracağın ve Faz 0'a nasıl geçeceğin adım adım yazıyor — bu README'yi taramadan önce oraya bak.

Çekirdek + berber + fitness modülleriyle başlayan, monorepo altında Spring Boot ile geliştirilen, git bağlantılı agent workflow'uyla ilerleyen bir SaaS projesi.

Bu klasördeki her dosya bağımsız bir spec — bir agent (coder/reviewer/tester) göreve başlamadan önce sadece ilgili dosyayı okuyup o kapsamda çalışabilir.

Platform sektörden bağımsız tasarlanmıştır: bugün berber + fitness, yarın inşaat/kuaför/oto yıkama gibi yeni bir dikey eklemek istediğinde mimari yeniden düşünülmez — [workflow/new-module-checklist.md](workflow/new-module-checklist.md)'deki adımlar izlenir.

## İçindekiler

**Başlangıç**
- [kickoff.md](kickoff.md) — yeni bir agent/oturumun ilk okuyacağı dosya: açık soruları nasıl soracağı, cevapları nereye işleyeceği, Faz 0'a nasıl geçeceği

**Terimler ve kod stili**
- [glossary.md](glossary.md) — terim sözlüğü (`tenant`/`user`/`member` gibi kavramların kesin tanımı + yasaklı kelimeler + gerekçe)
- [platform/code-style.md](platform/code-style.md) — sınıf/tablo isimlendirme, commit formatı

**Doküman meta-yönetimi (bilginin zaman içinde tutarlı kalması)**
- [docs-versioning.md](docs-versioning.md) — git commit SHA bazlı spec versiyonlama, context drift tespiti
- [docs-dependency-map.md](docs-dependency-map.md) — hangi temel dosya değişince hangileri etkileniyor

**Mimari**
- [repo-structure.md](repo-structure.md) — monorepo klasör yapısı (`platform` / `core` / `modules`)
- [package-naming.md](package-naming.md) — Java paket isimlendirme şeması
- [data-model-overview.md](data-model-overview.md) — tüm tabloların tek sayfalık referansı + modül yetenek matrisi

**Platform (SaaS'ın kendi işletim katmanı)**
- [platform/security-overview.md](platform/security-overview.md) — güvenliğin 8 dosyaya dağılmış halinin tek sayfalık indeksi + her PR'da kontrol listesi
- [platform/architecture-constraints.md](platform/architecture-constraints.md) — ihlal edilemez mimari kurallar (ASLA listesi), karmaşıklık bütçesi, bellek seviyesinde tenant izolasyonu (RLS'in koruyamadığı katman)
- [platform/tenant-customization.md](platform/tenant-customization.md) — tenant'a özel istekler için 3 seviyeli çözüm (feature flag / config / extension point), `if (tenantId==...)` yasağının doğru karşılığı
- [platform/bounded-context-matrix.md](platform/bounded-context-matrix.md) — hangi modül hangisini görebilir matrisi, paket sahipliği
- [platform/anti-patterns.md](platform/anti-patterns.md) — yasaklı kod kalıpları (God Class, N+1, DTO içinde Entity vb.)
- [platform/decision-tree.md](platform/decision-tree.md) — yeni bir şey core'a mı, modüle mi, feature flag'e mi, yeni modüle mi ait karar ağacı
- [platform/event-catalog.md](platform/event-catalog.md) — standart domain event isimleri (MemberCreated, PaymentReceivedEvent vb.)
- [platform/domain-invariants.md](platform/domain-invariants.md) — iş kurallarının asla ihlal edilemeyecek değişmezleri (INV-1..8)
- [platform/tenant-model.md](platform/tenant-model.md) — tenant, feature flag (`@RequiresModule`/`@RequiresFeature`), üyelik/billing'in modüller arası ortak kullanımı
- [platform/tenant-isolation-enforcement.md](platform/tenant-isolation-enforcement.md) — RLS'in kod/migration/test seviyesinde uygulanması
- [platform/roles-and-access.md](platform/roles-and-access.md) — platform_owner / tenant_admin / tenant_staff hiyerarşisi
- [platform/support-access.md](platform/support-access.md) — platform_owner'ın loglu/zaman sınırlı destek erişimi (impersonation)
- [platform/billing-model.md](platform/billing-model.md) — platform-billing (senin gelirin) vs tenant-billing (tenant'ın geliri) ayrımı
- [platform/pricing-packaging.md](platform/pricing-packaging.md) — abonelik paket yapısı (rakamsız)
- [platform/onboarding.md](platform/onboarding.md) — yeni tenant nasıl açılır (MVP: manuel)
- [platform/tenant-offboarding.md](platform/tenant-offboarding.md) — tenant ayrılırken veri dışa aktarma, grace period, KVKK veri taşınabilirliği
- [platform/accessibility.md](platform/accessibility.md) — WCAG AA asgari taahhüdü, klavye navigasyonu, semantik HTML
- [platform/data-import.md](platform/data-import.md) — mevcut Excel/liste verisinin sisteme aktarılması
- [platform/notifications.md](platform/notifications.md) — SMS/e-posta bildirim altyapısı
- [platform/kvkk-checklist.md](platform/kvkk-checklist.md) — veri koruma checklist'i
- [platform/audit-log.md](platform/audit-log.md) — kritik tablolarda kim ne zaman ne değiştirdi
- [platform/security.md](platform/security.md) — rate limiting, JWT token ömrü, girdi doğrulama
- [platform/mobile-compatibility.md](platform/mobile-compatibility.md) — backend deploy hızı ile mobil store review hızı arasındaki uyumluluk stratejisi
- [platform/tech-stack.md](platform/tech-stack.md) — DB, migration, framework kararları
- [platform/api-conventions.md](platform/api-conventions.md) — REST kuralları, hata formatı
- [platform/error-handling.md](platform/error-handling.md) — exception hiyerarşisi, hata formatının kod içinde üretilmesi
- [platform/database-conventions.md](platform/database-conventions.md) — migration kuralları, zorunlu index'ler, N+1/soft-delete
- [platform/concurrency-and-idempotency.md](platform/concurrency-and-idempotency.md) — çift kayıt, yarış durumu, idempotency key
- [platform/api-documentation.md](platform/api-documentation.md) — OpenAPI otomatik üretimi, frontend tip senkronizasyonu
- [platform/dependency-management.md](platform/dependency-management.md) — üçüncü taraf kütüphane ekleme kuralları + zorunlu merkezi sürüm yönetimi (BOM, jar hell önleme)
- [platform/caching-strategy.md](platform/caching-strategy.md) — tenant/feature-flag cache'leme, invalidation ve zorunlu tenant_id anahtar kuralı
- [platform/external-integration-resilience.md](platform/external-integration-resilience.md) — SMS/ödeme gibi dış servislerde timeout/circuit breaker/retry, ana akışı bloklamama kuralı
- [platform/file-storage.md](platform/file-storage.md) — logo/görsel yükleme (object storage, presigned URL)
- [platform/localization.md](platform/localization.md) — dil desteğinin somut uygulaması (tarih/sayı biçimi, çoğul kalıpları)
- [platform/theming.md](platform/theming.md) — açık/koyu mod + tenant marka rengi/logo sistemi
- [platform/static-analysis.md](platform/static-analysis.md) — kod kalitesi/güvenlik statik analizi (SonarQube/CodeQL), lint'ten ayrı katman

**Modüller**
- [modules/_template.md](modules/_template.md) — yeni modül eklerken kopyalanacak şablon
- [modules/core.md](modules/core.md) — çekirdek modül spec'i (auth, tenant, üyelik)
- [modules/fitness.md](modules/fitness.md) — fitness modül spec'i (QR check-in, PT, QR token güvenliği)
- [modules/berber.md](modules/berber.md) — berber modül spec'i (randevu, hizmet)

**Workflow**
- [workflow/issue-management.md](workflow/issue-management.md) — issue tipleri, etiketler, şablon, issue→branch→PR bağlantısı
- [workflow/branch-strategy.md](workflow/branch-strategy.md) — branch isimlendirme ve PR akışı
- [workflow/pr-and-definition-of-done.md](workflow/pr-and-definition-of-done.md) — PR şablonu, bir işin "bitti" sayılma kriterleri
- [workflow/code-review-checklist.md](workflow/code-review-checklist.md) — reviewer'ın (agent ya da insan) somut 6 kategorilik kontrol listesi
- [workflow/agent-pipeline.md](workflow/agent-pipeline.md) — git bağlantılı agent rolleri
- [workflow/new-module-checklist.md](workflow/new-module-checklist.md) — yeni bir sektör/dikey eklerken izlenecek sabit süreç (9 adım)
- [workflow/testing-strategy.md](workflow/testing-strategy.md) — test piramidi, tester agent'ın çalıştırdığı seviyeler
- [workflow/performance-testing.md](workflow/performance-testing.md) — yük/ölçek testi (doğruluk değil, "kaç eşzamanlı tenant kaldırıyor" sorusu)
- [workflow/example-feature-walkthrough.md](workflow/example-feature-walkthrough.md) — "Üye Oluştur" özelliğinin issue'dan PR'a uçtan uca örneği (agent için tek bakışlık referans)
- [workflow/change-impact-analysis.md](workflow/change-impact-analysis.md) — var olan bir kavramı (yeni ekleme değil) değiştirme süreci
- [workflow/ai-failure-recovery.md](workflow/ai-failure-recovery.md) — hatalı bir AI değişikliği merge olduğunda izlenecek prosedür

**Infra**
- [infra/environments.md](infra/environments.md) — local/staging/production, secrets yönetimi
- [infra/backup-recovery.md](infra/backup-recovery.md) — yedekleme, RPO/RTO hedefleri
- [infra/observability.md](infra/observability.md) — loglama, hata izleme, uptime/alerting

**Başlarken**
- [dev-setup.md](dev-setup.md) — repoyu yerel makinede ayağa kaldırma adımları

**Roadmap**
- [roadmap/versions.md](roadmap/versions.md) — sürüm yol haritası
- [roadmap/phases.md](roadmap/phases.md) — adım adım başlangıç sırası (Faz 0–6)
- [roadmap/pending-work.md](roadmap/pending-work.md) — açık bir iş kararına bağlı olduğu için bekleyen maddeler (hangi open-questions.md maddesine bağlı olduğuyla birlikte)

**Kararların gerekçesi**
- [adr/](adr/) — monorepo, Spring Boot, shared-DB+tenant_id, PostgreSQL+Flyway, Capacitor, i18n, UTC zaman damgası, Row-Level Security, Flyway zaman damgalı versiyonlama kararlarının bağlamı ve gerekçesi (ADR şablonu "geçmiş bağlam/orijinal olay/reddedilen alternatifler" alanları da taşıyor)

**Açık sorular**
- [open-questions.md](open-questions.md) — mühendislik kararı olmayan, senin cevaplaman gereken 10 madde (domain adı, ödeme/SMS sağlayıcısı, onboarding modeli, KVKK/ToS onayı, fiyatlandırma, hosting, e-Fatura)

## Genel kural

- Her modül sadece `core`'a bağımlı, diğer modüllere bağımlı değil; `platform`'a hiç bağımlı değil.
- Ortak koda (`core`) dokunan değişiklikler öncelikli review edilir.
- Branch'ler kısa ömürlü tutulur, modül sınırında kalır.
- Yeni bir modül eklemek asla mevcut mimariyi değiştirmez — sadece yeni klasör/dosya eklemektir.
- Platform-billing (senin gelirin) ve tenant-billing (tenant'ın geliri) hiçbir zaman aynı tabloda/kodda karışmaz.
- Kritik tablolarda (üyelik, ödeme, rol) her değişiklik audit_log'a yazılır; platform_owner'ın tenant verisine erişimi her zaman loglu ve zaman sınırlıdır.
