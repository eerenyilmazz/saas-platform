# Modül: core

**Paket:** `com.saas.core.*`
**Bağımlılık:** yok (diğer her modül buna bağımlı)
**Öncelik:** 1 — ilk geliştirilecek modül

## Sorumluluklar

- **Tenant modeli ve veri izolasyonu** — her tabloda `tenant_id`, her sorguda zorunlu filtre
- **Auth + rol yönetimi** — Spring Security ile giriş, roller: `tenant_admin`, `tenant_staff` (platform_owner ayrı bir katman, bkz. [platform/roles-and-access.md](../platform/roles-and-access.md))
- **Üyelik/paket motoru** — tüm modüller (fitness, berber) bu motoru kullanır; modüle özel değil
- **Tenant-billing** — tenant'ın kendi müşterisinden aldığı ödeme/fatura takibi (platformun kendi geliriyle karıştırılmaz, bkz. [platform/billing-model.md](../platform/billing-model.md))
- **Bildirim** — SMS/e-posta gönderimi için ortak arayüz (bkz. [platform/notifications.md](../platform/notifications.md))
- **Feature flag sistemi** — hangi modül hangi tenant'ta açık, tenant `settings` alanında tutulur

## Veri modeli (taslak)

- `tenant` — id, name, active_modules (JSON)
- `user` — id, tenant_id, role (`tenant_admin`/`tenant_staff`), login bilgisi — **sisteme giriş yapan, işletme içi kişi** (bkz. [glossary.md](../glossary.md))
- `member` — id, tenant_id, name, phone, consent_at, user_id (nullable) — **işletmenin kendi müşterisi**; `user_id` sadece üyenin kendi hesabıyla giriş yaptığı senaryoda dolu (örn. fitness QR app'i), berber gibi personelin randevuyu adına açtığı senaryoda `user_id` boş kalabilir
- `membership_plan` — id, tenant_id, name, duration, entry_allowance
- `membership` — id, tenant_id, member_id, plan_id, start_at, end_at, status (`active`/`frozen`/`expired`)
- `tenant_payment` — id, tenant_id, source_type (`membership`/`appointment`/`order`/...), source_id, amount, method, paid_at (tenant-billing)

> Alan adları her zaman İngilizce (bkz. [code-style.md](../platform/code-style.md) "Dil kuralı") — `consent_at` alanı KVKK açık rıza tarihini tutar, kısaltma olarak "KVKK" kod içinde geçmez, hukuki referansı [kvkk-checklist.md](../platform/kvkk-checklist.md)'dedir.

## Diğer modüllerle ilişki

Fitness ve berber modülleri `membership` ve `tenant-billing` tablolarını doğrudan kullanır, kendi üyelik/ödeme mantığını tekrar yazmaz. Modüle özel olan şey sadece "hizmetin ne olduğu" (QR check-in vs randevu) — üyelik/ödeme çekirdekte kalır.

`tenant_payment`'in `source_type`/`source_id` alanları bilinçli olarak genel tutuldu: üyelik yerine geçen bir kavramı olmayan modüller (örn. sipariş bazlı çalışan bir dikey) da tenant-billing'i kullanabilsin diye. Yeni bir modül, kendi domain nesnesini (`order`, `reservation` vb.) `source_type` olarak ekler, `core`'da yeni bir tablo/migration gerekmez.

## Kapsam dışı

- QR üretimi, randevu takvimi gibi modüle özel mantık burada olmaz — bunlar `modules/*` altındadır.
- Platformun kendi tenant'lardan aldığı abonelik geliri (platform-billing) burada değil, `packages/platform` altındadır.

## İlgili dosyalar

- Terimlerin kesin tanımı (`user` vs `member` farkı dahil): [glossary.md](../glossary.md)
- Tenant/feature flag mekanizmasının detayı: [platform/tenant-model.md](../platform/tenant-model.md)
- Rol hiyerarşisi: [platform/roles-and-access.md](../platform/roles-and-access.md)
- Billing ayrımı: [platform/billing-model.md](../platform/billing-model.md)
- KVKK ile ilgili alanlar: [platform/kvkk-checklist.md](../platform/kvkk-checklist.md)
- İşlem geçmişi (kim ne değiştirdi): [platform/audit-log.md](../platform/audit-log.md)
- Güvenlik (rate limiting, token ömrü): [platform/security.md](../platform/security.md)
- Yeni modül eklerken bu dosyanın nasıl kullanılacağı: [workflow/new-module-checklist.md](../workflow/new-module-checklist.md)
