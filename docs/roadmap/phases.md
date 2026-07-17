# Başlangıç Sırası

Sıra önemli — her faz bir öncekinin üzerine kurulur, atlanmaz.

## Faz 0 — Repo iskeleti + CI + altyapı kararları

- Monorepo'yu [repo-structure.md](../repo-structure.md)'daki klasör yapısıyla oluştur
- Spring Boot boş proje (`apps/api`) + React boş proje (`apps/web`) ayağa kaldır — [tech-stack.md](../platform/tech-stack.md)'deki sürümlerle (Java 21, PostgreSQL, Flyway)
- GitHub Actions'ta boş bir CI pipeline'ı (lint + build) çalışır hale getir
- `infra/docker` ile yerel geliştirme ortamını (db + api + web) tek komutla ayağa kaldır
- [api-conventions.md](../platform/api-conventions.md)'deki hata formatı ve URL şemasını iskelete uygula
- Temel observability'yi kur: yapılandırılmış loglama + hata izleme + `/health` endpoint'i (bkz. [observability.md](../infra/observability.md)) — sonradan eklemek, baştan kurmaktan daha maliyetli
- Yedekleme job'ını ilk günden kur (bkz. [backup-recovery.md](../infra/backup-recovery.md)) — boş bir DB'yi yedeklemek, dolu bir DB'de yedeklemeyi sonradan kurmaktan kolay
- Kurulum adımlarını [dev-setup.md](../dev-setup.md)'ye göre doğrula/güncelle

## Faz 1 — Core: auth, tenant, roller

- Tenant tablosu + tenant_id bazlı veri izolasyonu
- Spring Security ile giriş/rol yönetimi — `tenant_admin`, `tenant_staff` (bkz. [roles-and-access.md](../platform/roles-and-access.md))
- Üyelik/paket modelini (tablolar, ilişkiler) kur — henüz UI gerekmiyor
- Audit log mekanizmasını kur (bkz. [audit-log.md](../platform/audit-log.md)) — üyelik/billing tabloları en başından izlenmeli
- Rate limiting ve token ömrü/yenileme akışını uygula (bkz. [security.md](../platform/security.md))
- İlk tenant için manuel seed script ([onboarding.md](../platform/onboarding.md)) — platform admin paneli bu fazda yok
- Tenant izolasyon testini (bkz. [testing-strategy.md](../workflow/testing-strategy.md)) zorunlu CI gate olarak ekle
- Detay: [modules/core.md](../modules/core.md)

## Faz 2 — Fitness: QR check-in MVP

- QR token üretim/yenileme API'si
- Basit web ekranı: QR göster + görevli tarafında okuma/onay ekranı
- Giriş logu tablosu — henüz turnike/donanım entegrasyonu yok
- Detay: [modules/fitness.md](../modules/fitness.md)

## Faz 3 — Admin panel

- Üye ekleme/düzenleme, üyelik paketi atama
- Giriş logu raporu (kim ne zaman geldi)
- Feature flag ekranı — hangi modül bu tenant'ta açık

## Faz 4 — Berber: randevu MVP

- Randevu takvimi + hizmet süresi modeli
- Core'daki üyelik/billing altyapısını yeniden kullan, kopyalama
- Detay: [modules/berber.md](../modules/berber.md)

## Faz 5 — Mobil sarmalama

- `apps/web`'i Capacitor ile iOS + Android'e sar
- Push notification (üyelik bitiş hatırlatma) — [notifications.md](../platform/notifications.md)'deki olay tabanlı yapıyı kullan
- API versiyonlama/geriye dönük uyumluluk disiplinini uygula (bkz. [mobile-compatibility.md](../platform/mobile-compatibility.md)) — bu fazdan sonra backend'de kırıcı değişiklik artık serbest değil
- App Store + Play Store başvurusu

## Faz 6 — Platform admin paneli (tenant sayısı arttıkça)

- `apps/web` içinde `/platform` route'u, sadece `platform_owner` rolüyle erişilebilir
- Tenant listeleme, modül aç/kapa, platform-billing durumu görüntüleme
- Destek modu (impersonation) arayüzü (bkz. [support-access.md](../platform/support-access.md))
- Self-servis onboarding formu (bkz. [onboarding.md](../platform/onboarding.md)'deki eşik: ~10 tenant)
- Detay: [roles-and-access.md](../platform/roles-and-access.md)
