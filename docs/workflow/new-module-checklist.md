# Yeni Modül Ekleme Checklist'i

Bu doküman, platforma yeni bir dikey (sektör) eklerken izlenecek sabit süreci tanımlar. Amaç: yarın "inşaat app'i yapacağım" dendiğinde mimariyi yeniden düşünmeden, sadece bu adımları takip ederek entegre olabilmek.

## Adımlar

1. **Spec dosyasını oluştur**
   [modules/_template.md](../modules/_template.md) dosyasını `docs/modules/<modül_adı>.md` olarak kopyala, doldur.
   Örnek: `docs/modules/insaat.md`

2. **Paket adını belirle**
   `com.saas.modules.<modül_adı>.*` — bkz. [package-naming.md](../package-naming.md), yeni satırı oraya ekle.

3. **Klasörü aç**
   `packages/modules/<modül_adı>/` — bkz. [repo-structure.md](../repo-structure.md)

4. **Tenant modeline kaydet**
   `tenant.settings.modules.<modül_adı>` altına feature flag ekle — bkz. [platform/tenant-model.md](../platform/tenant-model.md)

5. **core ile ilişkiyi netleştir**
   Bu modül `membership`/`billing`'i nasıl kullanacak? `_template.md`'deki "core ile ilişki" bölümünü bu soruya göre doldur. Eğer core'da eksik bir şey varsa (örn. yeni bir alan gerekiyor), bunu ayrı bir core görevi olarak aç — yeni modülün ilk PR'ında core'a dokunma.

6. **Roadmap'e ekle**
   [roadmap/versions.md](../roadmap/versions.md) tablosuna yeni satır: hangi sürümde geleceği.
   [roadmap/phases.md](../roadmap/phases.md)'e yeni bir faz olarak ekle (mevcut fazları bozmadan sona ekle).

7. **README indeksini güncelle**
   [README.md](../README.md)'deki İçindekiler listesine yeni dosyayı ekle.

8. **Veri modeli genel bakışını güncelle**
   [data-model-overview.md](../data-model-overview.md)'ye yeni modülün tablolarını kendi bölümü altında ekle.

9. **İlk branch'i aç**
   `feature/<modül_adı>-mvp` — bkz. [branch-strategy.md](branch-strategy.md)

## Örnek: "inşaat" modülü eklenirse

| Adım | Sonuç |
|---|---|
| Spec | `docs/modules/insaat.md` |
| Paket | `com.saas.modules.insaat.*` |
| Klasör | `packages/modules/insaat/` |
| Feature flag | `tenant.settings.modules.insaat.enabled` |
| core ilişkisi | Muhtemelen "üyelik" değil "proje" kavramı — `_template.md`'deki soruya göre karar verilir |
| Roadmap | `versions.md`'ye yeni satır (örn. v1.4), `phases.md`'ye sona yeni bir faz (mevcut Faz 0–6'yı bozmadan) |
| Branch | `feature/insaat-proje-takip-mvp` |

Bu tablo örnek amaçlıdır — gerçek modül eklendiğinde `insaat.md` dosyası `_template.md` baz alınarak ayrıca doldurulur, mimari değişmez.

## Değişmeyen kurallar (her yeni modülde geçerli)

- Modül `core` dışında hiçbir modüle bağımlı olamaz.
- Her tablo `tenant_id` taşır.
- Ödeme/fatura `core.billing` üzerinden yürür, tekrar yazılmaz.
- Modülün ilk PR'ı sadece kendi klasörüne dokunur, `core`'a dokunmaz.
