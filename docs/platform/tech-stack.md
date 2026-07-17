# Teknoloji Yığını Kararları

Bu doküman daha önce konuşulan ama yazılı karara bağlanmamış teknik seçimleri netleştirir. Gerekçeleriyle birlikte — detaylı gerekçe için ilgili [adr/](../adr/) dosyasına bakılabilir.

| Katman | Seçim | Kısa gerekçe |
|---|---|---|
| Backend | Spring Boot 3.x, Java 21 (LTS) | Modüler monolit + multi-tenant için olgun ekosistem (bkz. [adr/0002](../adr/0002-spring-boot-modular-monolith.md)) |
| Veritabanı | PostgreSQL | JSONB desteği `tenant.settings` için ideal, Spring Data JPA ile sorunsuz entegrasyon, açık kaynak (bkz. [adr/0004](../adr/0004-postgresql-flyway.md)) |
| Migration | Flyway | SQL tabanlı, Liquibase'e göre daha az soyutlama/daha az öğrenme eğrisi, Spring Boot ile otomatik entegre |
| Frontend | React + TypeScript | Geniş ekosistem, Capacitor ile mobil sarmalamaya doğrudan uyumlu |
| Mobil | Capacitor | Aynı web kod tabanından iOS + Android (bkz. [adr/0005](../adr/0005-capacitor-mobil-sarmalama.md)) |
| CI/CD | GitHub Actions | Repo zaten GitHub'da olacağı için ek araç gerekmiyor, agent pipeline'ı da aynı yerden tetikleniyor |
| Konteynerleştirme | Docker (yerel geliştirme) | `infra/docker` altında tek komutla db + api + web ayağa kalkar |

## Neden bu kadar erken karar veriliyor

Modül geliştirmeye başlamadan önce bu tabakanın sabit olması gerekiyor — her modül aynı DB/migration/CI altyapısını paylaşıyor, ortada karar değişirse tüm modüller etkilenir. Bu yüzden Faz 0'ın bir parçası (bkz. [roadmap/phases.md](../roadmap/phases.md)).
