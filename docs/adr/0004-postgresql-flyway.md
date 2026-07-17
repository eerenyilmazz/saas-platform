# ADR 0004: PostgreSQL + Flyway

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

Veritabanı motoru ve migration aracı kararı gerekiyordu. Alternatifler: MySQL, PostgreSQL; migration için Flyway, Liquibase.

## Karar

PostgreSQL + Flyway.

## Sonuç

- PostgreSQL'in JSONB desteği, `tenant.settings` gibi esnek/şemasız alanlar için (bkz. [tenant-model.md](../platform/tenant-model.md)) doğrudan uygun — MySQL'de bu daha zayıf.
- Spring Data JPA ile PostgreSQL entegrasyonu olgun ve yaygın.
- Flyway, SQL tabanlı migration dosyaları kullanır — Liquibase'in XML/YAML soyutlamasına göre ekip için daha az öğrenme eğrisi, Spring Boot ile otomatik entegre çalışır.
