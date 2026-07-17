# Tenant'a Özel İstekler — Genişletme Noktası (Extension Point)

## Problem

Gerçek bir SaaS'ta kaçınılmaz bir senaryo var: ilk müşterilerden biri "bizim için randevu ekranına özel bir buton/kural eklensin" diyecek. Bir coder agent'a "müşteri X için özel bir akış yaz" dendiğinde, en kestirme (ve en yıkıcı) çözüm iş mantığının içine `if (tenantId.equals("x-uuid")) { ... }` yazmaktır — bu [architecture-constraints.md](architecture-constraints.md)'de artık açıkça yasak, çünkü codebase birkaç ay içinde her tenant için bir dal taşıyan, bakımı imkansız bir spagettiye döner.

## Karar: özelleştirme her zaman config'ten geçer, koddan değil

Tenant'a özel davranış **üç seviyeli** bir mekanizmadan geçer, hiçbiri iş mantığına doğrudan `if (tenantId == ...)` yazmaz:

### Seviye 1 — Feature flag (zaten var)

En yaygın durum: "bu özellik bazı tenant'larda açık, bazılarında kapalı." Bu zaten [tenant-model.md](tenant-model.md)'deki `@RequiresModule`/`@RequiresFeature` ile çözülüyor — yeni bir mekanizma gerekmez.

### Seviye 2 — Konfigürasyon parametresi

Davranış aynı ama **parametreleri** tenant'a göre değişiyorsa (örn. "randevu hatırlatması 24 saat önce değil, bu tenant için 48 saat önce gitsin"), bu [tenant-model.md](tenant-model.md)'deki `settings` JSON'una bir parametre olarak eklenir:

```json
"fitness": { "enabled": true, "hatirlatma_saat_once": 48 }
```

Kod, bu parametreyi okur (`tenant.getSetting("hatirlatma_saat_once", defaultValue: 24)`), hiçbir `if (tenantId)` yazılmaz.

### Seviye 3 — Extension point (interface)

Davranışın **kendisi** (parametre değil, mantığın şekli) tenant'a göre farklıysa (örn. "bu tenant için üyelik yenileme onayı iki adımlı olsun"), bu bir **strateji arayüzü** ile çözülür:

```java
interface MembershipRenewalStrategy {
    RenewalResult renew(Membership membership);
}

// core, varsayılan implementasyonu sağlar
class DefaultRenewalStrategy implements MembershipRenewalStrategy { ... }

// tenant'a özel davranış gerekiyorsa, ayrı bir implementasyon eklenir
// hangi implementasyonun kullanılacağı tenant.settings'ten okunur, kodda hardcode edilmez
```

- Bu implementasyonlar `core` içinde, iş mantığından **ayrı bir paket**te (`com.saas.core.membership.strategy`) yaşar — asıl `MembershipService` hangi strateji olduğunu bilmez, sadece arayüzü çağırır.
- Yeni bir strateji eklemek [new-module-checklist.md](../workflow/new-module-checklist.md) gibi bir süreçle, ayrı bir issue/PR olarak yapılır — asla mevcut `Service` sınıfının içine gömülmez.

## Ne zaman hangi seviye

| Durum | Seviye |
|---|---|
| Bazı tenant'larda açık/kapalı | 1 — feature flag |
| Aynı davranış, farklı sayı/eşik | 2 — config parametresi |
| Davranışın akışı/adımları farklı | 3 — extension point (nadir, dikkatli kullanılır) |

Seviye 3 **istisnadır**, kural değil — her tenant isteği otomatik olarak bir strateji arayüzü doğurmaz; önce 1 ve 2 zorlanır, 3'e sadece gerçekten mantığın şekli değişiyorsa geçilir (bu karar [decision-tree.md](decision-tree.md)'ye benzer şekilde bilinçli verilir, agent'ın kendi kendine seçmesine bırakılmaz — belirsizse [ADR](../adr/_template.md) konusu olur).

## MVP'de kapsam

Seviye 3 (extension point) mekanizması Faz 0-1'de **kod olarak kurulmaz** — sadece bu doküman, ihtiyaç doğduğunda hangi deseni izleyeceğimizi tarif eder. İlk gerçek tenant'a özel istek geldiğinde bu desen uygulanır, önceden spekülatif olarak inşa edilmez (erken soyutlama riski, bkz. [modules/_template.md](../modules/_template.md)).

## İlgili dosyalar

- [architecture-constraints.md](architecture-constraints.md) — `if (tenantId == ...)` yasağı
- [tenant-model.md](tenant-model.md) — Seviye 1 ve 2'nin dayandığı `settings` mekanizması
- [decision-tree.md](decision-tree.md)
