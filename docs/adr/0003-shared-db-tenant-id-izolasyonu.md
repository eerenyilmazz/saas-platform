# ADR 0003: Shared DB + tenant_id izolasyonu

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

Multi-tenancy için üç seçenek değerlendirildi: (1) shared DB + tenant_id kolonu, (2) schema-per-tenant, (3) database-per-tenant.

## Karar

Shared DB + her tabloda `tenant_id` kolonu ile veri izolasyonu.

## Sonuç

- En düşük maliyetli ve en kolay yönetilen seçenek, küçük/orta ölçekli erken aşama için uygun.
- Risk: veri izolasyonu kod seviyesinde sıkı tutulmalı — bu risk, [core.md](../modules/core.md)'daki ortak repository/base sınıfı ile merkezi olarak `tenant_id` filtresi uygulanarak ve [testing-strategy.md](../workflow/testing-strategy.md)'deki zorunlu tenant izolasyon testiyle azaltılıyor.
- Gözden geçirme koşulu: büyük/kurumsal bir müşteri "verim kendi sunucumda dursun" derse, o tenant için database-per-tenant'a geçiş değerlendirilir — mimari buna kapalı değil, sadece MVP için gerekli değil.
