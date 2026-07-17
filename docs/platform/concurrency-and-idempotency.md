# Eşzamanlılık & Idempotency

Sistemin en yoğun uç noktaları (QR check-in, ödeme kaydı) aynı anda birden fazla istekle karşılaşabilir — bu doküman "aynı işlem iki kez mi kaydedildi" ve "yarış durumu (race condition)" risklerini kapatır.

## QR check-in — çift kayıt riski

Bir üye QR'ı çok hızlı art arda okutursa (ya da okuyucu cihaz aynı isteği ağ hatası yüzünden iki kez gönderirse), aynı giriş iki kez `checkin_log`'a yazılabilir.

**Kural:** `checkin_log` kaydı oluşturulmadan önce, aynı `member_id` için son N saniye (öneri: 5 saniye) içinde bir kayıt olup olmadığı kontrol edilir — varsa yeni kayıt açılmaz, mevcut kayıt yanıt olarak döner (idempotent davranış). Bu kontrol veritabanı seviyesinde bir unique constraint ile de desteklenebilir (`member_id` + zaman penceresi).

## Üyelik dondurma/güncelleme — yarış durumu

İki farklı personel aynı anda aynı üyeliği güncellerse (örn. biri dondurma işlemi yaparken diğeri yenileme yapıyorsa), son yazan diğerinin değişikliğini sessizce ezebilir.

**Kural:** `membership` tablosunda **optimistic locking** kullanılır (JPA `@Version` alanı) — bir güncelleme, kayıt son okunduğundan beri değişmişse reddedilir (HTTP 409, `error.code = CONFLICT`, bkz. [error-handling.md](error-handling.md)), kullanıcıya "bu kayıt başkası tarafından güncellendi, sayfayı yenileyin" mesajı gösterilir.

## Ödeme kaydı — idempotency key

`tenant_payment` kaydı oluşturan her istek (özellikle ileride otomatik ödeme entegrasyonu geldiğinde, bkz. [billing-model.md](billing-model.md) v1.3), bir **idempotency key** taşır — istemci (frontend ya da ödeme sağlayıcısının webhook'u) aynı isteği ağ hatası nedeniyle tekrar gönderirse, aynı key ile gelen ikinci istek yeni bir kayıt açmaz, ilk sonucu döner.

- Şema: `tenant_payment.idempotency_key` (unique, nullable — sadece bu riski taşıyan akışlarda zorunlu).
- Bu, özellikle ödeme sağlayıcısı entegrasyonlarında (webhook'lar aynı olayı birden fazla kez gönderebilir) sektör standardıdır.

## Randevu çakışması (berber) — düzeltme: basit unique constraint yetersiz

**Önceki hata:** Bu bölüm daha önce `(staff_id, tarih/saat)` üzerinde basit bir unique constraint öneriyordu — bu **yanlış/eksik**. Bir unique constraint sadece birebir aynı başlangıç saatini yakalar; 10:00-10:30 ile 10:15-10:45 gibi **örtüşen ama farklı başlangıçlı** iki randevu bu kontrolden sorunsuzca geçer, aynı personelin aynı anda iki yerde olması gerekirdi.

**Doğrusu:** PostgreSQL'in **range exclusion constraint**'i (`EXCLUDE USING gist`) kullanılır — bu, iki zaman aralığının (start, end) **örtüştüğü her durumu**, örtüşme birebir eşleşme olmasa bile veritabanı seviyesinde engeller:

```sql
ALTER TABLE appointment ADD COLUMN duration_range tstzrange GENERATED ALWAYS AS (tstzrange(start_at, end_at)) STORED;
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE appointment ADD CONSTRAINT no_overlapping_appointments
    EXCLUDE USING gist (staff_id WITH =, duration_range WITH &&);
```

> Bu constraint'in çalışması için `appointment` tablosunun `start_at`/`end_at` (tek bir `scheduled_at` değil) taşıması gerekir — bkz. [modules/berber.md](../modules/berber.md)'deki güncel şema.

- Uygulama kodu bu constraint ihlalini yakalayıp `ConflictException("BERBER_SLOT_TAKEN", ...)` olarak kullanıcıya döner (bkz. [error-handling.md](error-handling.md)) — aynen önceki tasarımdaki gibi, sadece veritabanı kısıtının kendisi düzeltildi.
- Bu desen fitness'teki `training_appointment` için de aynı şekilde uygulanır (bir PT'nin aynı saatte iki öğrencisi olamaz).

## İlgili dosyalar

- [error-handling.md](error-handling.md) — `ConflictException` → HTTP 409 eşlemesi
- [database-conventions.md](database-conventions.md) — index/constraint kuralları
- [../modules/fitness.md](../modules/fitness.md), [../modules/berber.md](../modules/berber.md)
