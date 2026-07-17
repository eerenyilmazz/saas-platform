# Cache Stratejisi

## Problem

[tenant-model.md](tenant-model.md)'deki `@RequiresModule` interceptor'ı, **her istekte** tenant'ın `settings` alanını okuyor (feature flag kontrolü için). Trafik arttıkça bu, her API çağrısına bir ekstra DB sorgusu ekler — tenant sayısı büyüdükçe gereksiz bir yük haline gelir.

## Karar: tenant ayarları önbelleğe alınır

- Tenant `settings` (feature flag'ler + branding), her tenant için kısa süreli (öneri: 60 saniye) bellek içi (in-memory) cache'de tutulur — Redis gibi ayrı bir altyapıya MVP ölçeğinde gerek yok, Spring'in kendi `@Cacheable` mekanizması yeterli.
- **Cache invalidation kuralı:** `platform_owner` bir tenant'ın modülünü aç/kapa yaptığında (bkz. [roles-and-access.md](roles-and-access.md)), o tenant'ın cache kaydı **anında** temizlenir — 60 saniyelik pencerede eski ayarın kullanılmasını istemiyoruz, özellikle bir modül kapatılmışsa.
- **Zorunlu anahtar kuralı:** Cache anahtarı **her zaman** `tenant_id` ile başlar (`@Cacheable(key = "#tenantId")`), asla `settings` gibi tenant'sız bir anahtarla tutulmaz — bkz. [architecture-constraints.md](architecture-constraints.md)'deki "Bellek seviyesinde tenant izolasyonu" bölümü. Bu cache, RLS'in koruyamadığı tam da o katman olduğu için burada bir kural ihlali, RLS'in tüm faydasını sıfırlar.

## Ne cache'lenir, ne cache'lenmez

| Veri | Cache'lenir mi | Neden |
|---|---|---|
| `tenant.settings` (feature flag) | ✅ Evet, kısa süreli | Sık okunur, nadiren değişir |
| `membership` durumu (aktif/dondurulmuş) | ❌ Hayır | QR check-in gibi para/erişimle ilgili kararlarda her zaman güncel veri okunmalı — bkz. [concurrency-and-idempotency.md](concurrency-and-idempotency.md) |
| `member` listesi | ❌ Hayır (MVP'de) | Sık değişen, tenant başına veri hacmi küçük, cache karmaşıklığına değmez |

## Ölçek büyüdükçe

Tek instance'tan birden fazla `api` instance'ına geçilirse (yatay ölçekleme), bellek içi cache instance'lar arasında tutarsız olur — o noktada Redis gibi paylaşılan bir cache katmanına geçiş değerlendirilir. MVP'de tek instance yeterli, bu karar şimdiden verilmiyor.

## İlgili dosyalar

- [tenant-model.md](tenant-model.md) — cache'lenen veri kaynağı
- [database-conventions.md](database-conventions.md)
