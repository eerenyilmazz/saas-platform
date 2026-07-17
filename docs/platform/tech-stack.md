# Teknoloji Yığını Kararları

Bu doküman daha önce konuşulan ama yazılı karara bağlanmamış teknik seçimleri netleştirir. Gerekçeleriyle birlikte — detaylı gerekçe için ilgili [adr/](../adr/) dosyasına bakılabilir.

| Katman | Seçim | Kısa gerekçe |
|---|---|---|
| Backend | Spring Boot 4.1.0, Java 21 (LTS) | Modüler monolit + multi-tenant için olgun ekosistem (bkz. [adr/0002](../adr/0002-spring-boot-modular-monolith.md)) |
| Build aracı | Gradle 9.x (wrapper ile) | Spring Initializr varsayılanı, `./gradlew` sayesinde global Gradle kurulumu gerekmez |
| Veritabanı | PostgreSQL 16 | JSONB desteği `tenant.settings` için ideal, Spring Data JPA ile sorunsuz entegrasyon, açık kaynak (bkz. [adr/0004](../adr/0004-postgresql-flyway.md)) |
| Migration | Flyway (zaman damgalı versiyonlama) | SQL tabanlı, Liquibase'e göre daha az soyutlama/daha az öğrenme eğrisi, Spring Boot ile otomatik entegre — isimlendirme detayı [adr/0009](../adr/0009-flyway-timestamp-versioning.md) |
| Frontend | React + TypeScript (Vite) | Geniş ekosistem, Capacitor ile mobil sarmalamaya doğrudan uyumlu |
| Mobil | Capacitor | Aynı web kod tabanından iOS + Android (bkz. [adr/0005](../adr/0005-capacitor-mobil-sarmalama.md)) |
| CI/CD | GitHub Actions (`.github/workflows/`) | Repo zaten GitHub'da olacağı için ek araç gerekmiyor, agent pipeline'ı da aynı yerden tetikleniyor |
| Konteynerleştirme | Docker (yerel geliştirme) | `infra/docker` altında tek komutla PostgreSQL ayağa kalkar |

## Neden bu kadar erken karar veriliyor

Modül geliştirmeye başlamadan önce bu tabakanın sabit olması gerekiyor — her modül aynı DB/migration/CI altyapısını paylaşıyor, ortada karar değişirse tüm modüller etkilenir. Bu yüzden Faz 0'ın bir parçası (bkz. [roadmap/phases.md](../roadmap/phases.md)).

## Faz 0'da gerçek ortamda doğrulanan düzeltmeler

- **Spring Boot sürümü:** Spring Initializr'dan istenen `3.4.0` artık desteklenmiyordu, güncel `4.1.0`'a çözüldü — doküman buna göre güncellendi (bkz. [adr/0002](../adr/0002-spring-boot-modular-monolith.md) gözden geçirilmedi, framework seçimi değil sadece sürüm numarası değişti).
- **CI dosya konumu:** `repo-structure.md` başlangıçta `infra/github-actions/` diyordu — GitHub sadece `.github/workflows/`'u tarar, bu bir dokümantasyon hatasıydı, düzeltildi.
- **`packages/*` fiziksel klasör değil:** `platform`/`core`/`modules` ayrı Gradle projeleri değil, tek `apps/api` Gradle projesi içindeki Java paketleri olarak kuruldu — detay [repo-structure.md](../repo-structure.md).
