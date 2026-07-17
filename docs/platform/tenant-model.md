# Tenant & Feature Flag Modeli

Bu doküman `core`'un sağladığı, **tüm modüllerin** üzerine inşa edildiği temel mekanizmayı tanımlar. Yeni bir modül eklerken buradaki kurallara uyman yeterli, tekrar yazmana gerek yok.

## Tenant

Her firma (berber salonu, fitness salonu, ileride inşaat firması vb.) bir `tenant` kaydıdır.

```
tenant
├── id
├── name
├── settings (JSON)   → aktif modüller + modül bazlı konfigürasyon burada
└── created_at
```

### settings JSON örneği

```json
{
  "modules": {
    "fitness":      { "enabled": true, "qr_token_ttl_seconds": 30 },
    "berber":       { "enabled": false },
    "construction": { "enabled": true, "project_based_inventory": true }
  },
  "branding": { "logo_url": "...", "color": "#2C3E6B" }
}
```

Yeni bir modül eklendiğinde `settings.modules.<modül_adı>` altına kendi konfigürasyon alanlarını ekleyebilirsin — şema esnek, migration gerektirmez.

`branding` alanının frontend'de nasıl uygulandığı (renk token'ı, kontrast kontrolü) için bkz. [theming.md](theming.md); `logo_url`'ün nasıl yükleneceği için bkz. [file-storage.md](file-storage.md).

## Veri izolasyonu kuralı

- Her tablo `tenant_id` kolonu taşır.
- Her sorgu `tenant_id` filtresiyle çalışır — bu kontrol `core`'daki ortak repository/base sınıfında merkezi olarak uygulanır, her modül bunu kendi kodunda tekrar yazmaz.
- Bir modül, kendi tenant_id'si dışındaki veriye asla erişemez; bu kural test suite'inde (core seviyesinde) doğrulanır.

## Feature flag kontrolü — kod seviyesinde uygulama

Her modülün controller sınıfı, sınıf düzeyinde bir `@RequiresModule("fitness")` annotation'ı taşır. Bu, `core`'da tanımlı tek bir Spring interceptor tarafından okunur — istek geldiğinde interceptor, ilgili tenant'ın `settings.modules.fitness.enabled` alanını kontrol eder, kapalıysa isteği controller'a hiç ulaştırmadan HTTP 404 döner.

```java
@RestController
@RequiresModule("fitness")
class FitnessCheckinController { ... }
```

- Bu yaklaşımın avantajı: her controller metodunda tekrar tekrar `if (tenant.hasModule(...))` yazılmaz, kontrol tek bir yerde (interceptor) merkezi kalır — [code-style.md](code-style.md)'deki "ortak mantık tekrar yazılmaz" prensibiyle tutarlı.
- Yeni bir modül eklerken tek yapılması gereken, o modülün controller'larına doğru `@RequiresModule` değerini vermek — [new-module-checklist.md](../workflow/new-module-checklist.md)'ye bu adım zaten örtük olarak dahildir (paket adı = modül adı = flag adı).

### İnce taneli flag — modül içinde paket bazlı özellik kısıtı

`@RequiresModule` bir modülü tamamen açar/kapar ama [pricing-packaging.md](pricing-packaging.md)'deki kademeler bazen **modül içinde** bir alt özelliği kısıtlar — örn. "QR check-in temel pakette yok, sadece Premium Fitness'te var". Bunun için ayrı, daha ince taneli bir annotation kullanılır:

```java
@PostMapping("/checkin")
@RequiresFeature(Feature.FITNESS_QR)
ResponseEntity<?> checkin(...) { ... }
```

- `@RequiresModule`, sınıf düzeyinde "bu modül açık mı" sorar; `@RequiresFeature`, metod düzeyinde "bu **spesifik özellik**, tenant'ın paketinde var mı" sorar — ikisi aynı interceptor mekanizmasını paylaşır, sadece kontrol ettikleri `settings` alanı farklı derinlikte (`settings.modules.fitness.enabled` vs `settings.modules.fitness.features.qr_checkin`).
- Bu ayrımın amacı: bir agent yeni bir premium özellik eklerken bu annotation'ı **unutamaz** — kontrolü koda gömmek, "unutma" ihtimalini insana/agent'a bırakmaktan daha güvenilirdir (bkz. [architecture-constraints.md](architecture-constraints.md)'deki "kısıtlar prosa değil kod/test ile denetlenir" prensibi).
- Yeni bir `Feature` eklerken [event-catalog.md](event-catalog.md)'deki gibi bir merkezi liste (`Feature` enum, `core`'da tanımlı) kullanılır, her modül kendi string'ini icat etmez.

## Üyelik (membership) — modüller arası ortak kavram

`membership_plan` ve `membership` tabloları `core`'da yaşar, sektöre bakılmaksızın "bir üyenin bir pakete kayıtlı olması" kavramını temsil eder:

| Sektör | "Üyelik" neye karşılık geliyor |
|---|---|
| fitness | Aylık/yıllık spor salonu üyeliği |
| berber | Randevu paketi / sadakat kartı (opsiyonel) |
| inşaat (örnek) | Proje bazlı hizmet sözleşmesi |

Yeni bir modül eklerken önce şunu sor: **bu sektörde "üyelik" kavramı neye karşılık geliyor?** Cevap varsa `core.membership`'i kullan, yoksa modülün kendi domain nesnesini tanımla (örn. inşaatta "proje" ayrı bir kavram olabilir, üyelik modeline zorlanmaz).

## Billing — ortak ödeme/fatura

Tüm modüller ödeme/fatura işlemleri için `core.billing`'i çağırır. Hiçbir modül kendi ödeme/fatura mantığını yazmaz.
