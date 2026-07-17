# Onboarding — Yeni Tenant Nasıl Açılır

## MVP kararı: manuel onboarding

İlk tenant'lar (pilot salonlar) **kendi kendine kayıt olmaz** — sen/ekibin açar. Sebep: erken aşamada müşteri sayısı az, self-servis kayıt formu + doğrulama + ödeme entegrasyonu kurmak, henüz kimsenin kullanmayacağı bir özelliğe erken yatırım olur.

## MVP akışı

1. Satış görüşmesi (sen ↔ salon sahibi) — hangi modüller isteniyor, kaç kullanıcı
2. `platform_owner` bir seed script/SQL ile `tenant` kaydı açar: ad, aktif modüller (`tenant.settings.modules`), platform_subscription planı
3. `tenant_admin` kullanıcısı oluşturulur, giriş bilgileri e-posta ile gönderilir
4. Salon sahibi giriş yapar, kendi işletme bilgilerini (çalışma saati, hizmet/paket tanımları) tamamlar — bu kısım UI üzerinden, `core` ve ilgili modülün ekranlarından yapılır
5. Salon sahibi kendi personelini (`tenant_staff`) ekler

## Ne zaman self-servis'e geçilir

Tenant sayısı arttıkça (öngörülen eşik: ~10 tenant, adım 2-3'ün manuel yapılması operasyonel yük haline geldiğinde) bir kayıt formu + platform admin panelinden onay akışı eklenir — bkz. [roadmap/versions.md](../roadmap/versions.md) v1.2.

## Kapsam dışı (MVP'de yok)

- Kredi kartıyla anlık kayıt/ödeme
- Otomatik deneme süresi (trial) yönetimi
- E-posta doğrulama akışı (manuel onboarding'de gerek yok, sen zaten doğrudan iletişimdesin)

## İlgili dosyalar

- [roles-and-access.md](roles-and-access.md) — platform_owner'ın tenant açma yetkisi
- [billing-model.md](billing-model.md) — platform_subscription kaydı
- [tenant-offboarding.md](tenant-offboarding.md) — tenant ayrılmak istediğinde tersi akış
