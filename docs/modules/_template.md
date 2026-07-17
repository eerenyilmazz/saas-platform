# Modül: &lt;modül_adı&gt;

> Bu dosya bir şablondur. Yeni bir dikey (örn. inşaat, kuaför, oto yıkama) eklerken bu dosyayı `docs/modules/<modül_adı>.md` olarak kopyala, başlıkları doldur, bu satırı sil.

**Paket:** `com.saas.modules.<modül_adı>.*`
**Bağımlılık:** `core` (auth, tenant, membership, billing) — başka modüle bağımlılık yasak
**Öncelik:** &lt;bu modül hangi fazda geliştirilecek — bkz. [roadmap/phases.md](../roadmap/phases.md)&gt;

## Sorumluluklar

- Bu modülün çözdüğü **spesifik** iş problemi ne? (genel değil, bu sektöre özgü olanı yaz)
- Hangi işlemler bu modüle özel, `core`'da olmayan?

## Akış (varsa)

Modülün ana işlem akışını diyagram/adım listesi olarak yaz. Örnek format:

```
[Girdi] → [İşlem] → [core'daki hangi kontrol] → [Sonuç]
```

## Veri modeli (taslak)

- `<tablo_adı>` — id, tenant_id, ... (her tabloda tenant_id zorunlu)

## core ile ilişki

- Bu modül `core.membership`'i nasıl kullanıyor? (üyelik/paket kavramı bu sektörde neye karşılık geliyor) — **eğer bu sektörde üyelik kavramı hiç yoksa** (örn. sipariş bazlı çalışan bir dikey), `membership`'i zorlama, modülün kendi domain nesnesini (`order`, `reservation` vb.) tanımla.
- Bu modül `core.billing`'i nasıl kullanıyor? `tenant_payment` tablosu `source_type`/`source_id` alanlarıyla genel tutulmuştur (bkz. [core.md](core.md)) — üyelik/randevu olmasa bile modülün kendi domain nesnesi buraya `source_type` olarak eklenebilir, `core`'da migration gerekmez.
- Yeni bir `core` değişikliği gerekiyor mu? Gerekiyorsa bunu ayrı bir görev/PR olarak core ekibine/agent'ına aç, bu modülün PR'ına karıştırma.

## Diğer modüllerle ortak nokta (varsa)

Benzer iş modeline sahip başka modül var mı (örn. randevu+üyelik modeli fitness ve berber'de ortak)? Kod tekrarından kaçınmak için bu ortaklığı burada not düş, gerekirse ortak mantığı `core`'a taşımayı değerlendir.

## Kapsam dışı

- Başka modüle ait olan ve burada tekrar edilmeyecek şeyleri açıkça listele.

## Doldurmadan önce oku

- [glossary.md](../glossary.md) — terim isimleri burada sabit, yeni bir isim icat etme (örn. "müşteri" değil "member" kullan)
- [platform/code-style.md](../platform/code-style.md) — sınıf/tablo isimlendirme kuralları
- Doldurduktan sonra [data-model-overview.md](../data-model-overview.md)'ye bu modülün tablolarını ekle (bkz. [workflow/new-module-checklist.md](../workflow/new-module-checklist.md))
