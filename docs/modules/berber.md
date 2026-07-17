# Modül: berber

**Paket:** `com.saas.modules.berber.*`
**Bağımlılık:** `core` (auth, tenant, membership, billing)
**Öncelik:** 3

## Sorumluluklar

- **Randevu takvimi** — tarih/saat, personel bazlı müsaitlik
- **Hizmet tanımları** — hizmet süresi, fiyat
- **Hizmet + ürün satışı** — `core.billing` üzerinden, ayrı bir ödeme mantığı yazılmaz

## Veri modeli (taslak)

- `service` — id, tenant_id, name, duration (dakika), price
- `appointment` — id, tenant_id, staff_id (→ `core.user`), member_id (→ `core.member`), service_id, start_at, end_at, status
- `staff_availability` — staff_id, day_of_week, start_time, end_time

> Terim notu: bu tablo daha önce `customer_id` kullanıyordu — `core`'daki `member` tablosuyla tutarlı olması için `member_id`'ye çevrildi (bkz. [glossary.md](../glossary.md)). `appointment` tek bir `scheduled_at` yerine `start_at`/`end_at` taşır — randevu çakışma kısıtının (bkz. [concurrency-and-idempotency.md](../platform/concurrency-and-idempotency.md)) bir zaman aralığı üzerinden çalışabilmesi için gerekli.

## Fitness ile ortak nokta

Fitness ve berber ikisi de "randevu/üyelik + yan ürün satışı" modeline oturur. `core.membership` ve `core.billing` ortak kullanılır; farklı olan sadece hizmetin şekli (QR check-in vs randevu rezervasyonu).

## Kapsam dışı

- QR/giriş-çıkış mantığı fitness modülüne özgü, burada yok.
- Üyelik paketi/ödeme tanımı → `core`'da.
