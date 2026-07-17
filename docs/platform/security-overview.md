# Güvenlik — Tek Sayfalık Genel Bakış

> Bu dosya bir **kaynak değil, indeks**tir (bkz. [data-model-overview.md](../data-model-overview.md)'nin aynı deseni). Güvenlik 8 ayrı dosyaya dağılmış durumda — burada hepsi tek yerde listeleniyor ki bir agent/insan "güvenlik açısından neyi kontrol etmeliyim" sorusuna tek dosyadan cevap bulsun.

## Katman katman güvenlik

| Katman | Risk | Çözüm | Dosya |
|---|---|---|---|
| Veritabanı | Bir tenant'ın verisi başka tenant'a sızar | Row-Level Security (RLS) | [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md), [ADR 0008](../adr/0008-row-level-security.md) |
| Uygulama belleği (RAM) | RLS'in koruyamadığı katman — static cache/singleton/ThreadLocal üzerinden tenant verisi sızması | Static mutable state yasağı, tenant_id'li cache anahtarı, ThreadLocal temizliği | [architecture-constraints.md](architecture-constraints.md) "Bellek seviyesinde tenant izolasyonu" |
| API | Brute-force, kötüye kullanım, DoS | Rate limiting, JWT token ömrü/yenileme | [security.md](security.md) |
| Kod (kendi yazdığımız) | SQL injection, hardcoded secret, güvensiz deserialization | Statik analiz (CodeQL/Semgrep) | [static-analysis.md](static-analysis.md) |
| Bağımlılıklar (üçüncü taraf) | Bilinen CVE'li kütüphane | Dependabot taraması + merkezi BOM (versiyon şişmesi/jar hell önleme) | [dependency-management.md](dependency-management.md) |
| Loglama | PII'nin düz metin loglanması | Telefon/ad maskeleme | [../infra/observability.md](../infra/observability.md) "PII maskeleme" |
| Hata mesajları | Stack trace/iç detay sızıntısı | Merkezi `@ControllerAdvice`, kullanıcıya asla iç detay dönülmez | [error-handling.md](error-handling.md) |
| Erişim/yetki | Yetkisiz tenant/rol erişimi | RLS + rol hiyerarşisi + loglu destek modu | [roles-and-access.md](roles-and-access.md), [support-access.md](support-access.md) |
| Kişisel veri (KVKK) | Rıza olmadan veri işleme/bildirim | Açık rıza alanı, silme endpoint'i, saklama süresi | [kvkk-checklist.md](kvkk-checklist.md) |
| İzlenebilirlik | "Kim ne yaptı" sorusuna cevap yok | Audit log (kritik tablolarda zorunlu) | [audit-log.md](audit-log.md) |

## Her yeni PR'da kontrol edilecekler (özet)

Bu liste [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 1. ve 3. kategorilerinin (sınır ihlali, güvenlik) somut bir kısayolu:

- [ ] Yeni tablo → `tenant_id` + RLS aktif mi?
- [ ] Yeni cache/static alan → anahtar `tenant_id` ile mi başlıyor, mutable singleton field var mı?
- [ ] Yeni endpoint → rate limiting gerektiren bir kategoriye giriyor mu (public/login benzeri)?
- [ ] Yeni bağımlılık → insan onaylı mı, BOM'a mı eklendi?
- [ ] Yeni log satırı → PII düz metin mi yazılıyor?
- [ ] Yeni kişisel veri alanı → KVKK kapsamına giriyor mu, `consent_at` kontrolü var mı?

## İlgili dosyalar

Yukarıdaki tablodaki her satır zaten kendi dosyasına link veriyor — bu dosya sadece bir harita, detay her zaman kaynak dosyada.
