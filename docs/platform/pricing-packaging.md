# Fiyatlandırma Paket Yapısı

> Bu dosya paket **yapısını** tanımlar, gerçek rakamları değil — rakamlar tamamen senin pazarlama kararın (bkz. [open-questions.md](../open-questions.md) madde 7). Burada sadece `platform_subscription` modelinin (bkz. [billing-model.md](billing-model.md)) hangi kademelere ayrılacağı tasarlanıyor.

## Kademe mantığı: modül bazlı, taban + ek modül

İki yaygın SaaS paketleme modeli var: (a) sabit kademeler (Başlangıç/Büyüme/Kurumsal, her biri sabit özellik seti) veya (b) taban ücret + seçtiğin her modül için ek ücret. Bu platform zaten modüler tasarlandığı için **(b) taban + ek modül** modeli mimariyle daha tutarlı — bir salon sahibi sadece ihtiyacı olan modülü alır, gereksiz özellik için ödeme yapmaz.

## Önerilen yapı

| Bileşen | Ne içerir |
|---|---|
| **Taban ücret** | `core` her zaman dahil — auth, tenant, üyelik motoru, tenant-billing, bildirim (temel SMS kotası), 1 kullanıcı (tenant_admin) |
| **Modül eki** | Her aktif modül (`fitness`, `berber`, ileride yeni dikeyler) ayrı bir ek ücret |
| **Kullanıcı eki** (opsiyonel) | `tenant_staff` sayısı belirli bir eşiği geçerse ek ücret — küçük salonlar için muhtemelen gereksiz, büyüdükçe anlamlı |

## Örnek paket iskeleti (rakamsız)

```
Taban paket:
  - core (auth, üyelik, tenant-billing, temel bildirim)
  - 1 modül dahil (fitness VEYA berber)
  - 3 kullanıcıya kadar

+ Ek modül: her ilave modül için ek ücret
+ Ek kullanıcı: 3'ten sonra her kullanıcı için ek ücret (opsiyonel, MVP'de olmayabilir)
```

## MVP'de basitleştirme

`platform_subscription` tablosu (bkz. [billing-model.md](billing-model.md)) şu an sadece `plan` (serbest metin/JSON, hangi modüller dahil) ve `monthly_fee` alanlarını tutuyor — bu, karmaşık bir "kademe motoru" kurmadan, ilk birkaç tenant'ta fiyatı manuel esnek tutmaya izin veriyor. Kademelerin katı kurallara (örn. otomatik faturalama) bağlanması, otomatik ödemeye geçildiğinde (bkz. [roadmap/versions.md](../roadmap/versions.md) v1.3) anlamlı olur.

## Rakiplerle karşılaştırma — kimin işi

Türkiye'deki benzer SaaS'ların (salon/randevu yönetimi yapan) fiyatlarını araştırıp taban+modül rakamlarını belirlemek [open-questions.md](../open-questions.md) madde 7'de senin kararına bırakıldı — bu doküman sadece yapıyı hazırlıyor, rakamı doldurmak kalan iş.

## İlgili dosyalar

- [billing-model.md](billing-model.md) — `platform_subscription` veri modeli
- [onboarding.md](onboarding.md) — paket seçiminin ne zaman/nasıl yapıldığı
- [open-questions.md](../open-questions.md) — gerçek rakamlar
