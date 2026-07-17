# Dosya/Görsel Depolama

[tenant-model.md](tenant-model.md)'deki `settings.branding.logo_url` alanı bir tenant logosunun varlığını ima ediyordu ama bu görselin **nereye yükleneceği** hiç tanımlanmamıştı — veritabanına dosya gömülmez, ayrı bir depolama katmanı gerekir.

## Karar: S3 uyumlu object storage

- Görseller (tenant logosu, ileride ürün/menü fotoğrafı gibi modül-özel ihtiyaçlar) veritabanına değil, S3 API'siyle uyumlu bir object storage'a yüklenir.
- Hangi sağlayıcı (AWS S3, Cloudflare R2, ya da seçilecek hosting sağlayıcısının kendi object storage'ı) [open-questions.md](../open-questions.md) madde 8'deki (hosting) kararla birlikte netleşir — S3 API uyumluluğu sayesinde sağlayıcı değişse bile kod tarafı değişmez.
- Veritabanında sadece dosyanın **URL'i** tutulur (`tenant.settings.branding.logo_url`), dosyanın kendisi değil.

## Yükleme akışı

```
[Frontend] → [API: presigned upload URL iste] → [Frontend, dosyayı doğrudan object storage'a yükler] → [API: URL'i tenant.settings'e kaydet]
```

- Dosya, backend sunucusundan geçmeden doğrudan object storage'a yüklenir (presigned URL yöntemi) — backend'i gereksiz yere dosya trafiğiyle meşgul etmemek için standart pratik.

## Güvenlik kuralı

- Yüklenen dosya tipi kısıtlanır (sadece `image/png`, `image/jpeg` vb.) — [security.md](security.md)'deki girdi doğrulama prensibiyle tutarlı.
- Dosya boyutu üst sınırı konur (öneri: 2 MB, logo için fazlasıyla yeterli).
- Her tenant'ın yüklediği dosya, kendi tenant'ına ait bir klasör/prefix altında tutulur (`tenant_id` bazlı ayrım) — bir tenant'ın dosya URL'ini tahmin ederek başka bir tenant'ın dosyasına erişememesi için.

## Kapsam dışı (MVP'de yok)

- Görsel işleme (otomatik boyutlandırma/thumbnail üretimi) — logo genelde küçük bir dosya, MVP'de ham haliyle kullanılır.
- Video/büyük dosya desteği — kapsam dışı, ihtiyaç olursa ayrıca değerlendirilir.

## İlgili dosyalar

- [tenant-model.md](tenant-model.md) — `branding.logo_url` alanı
- [security.md](security.md)
- [../open-questions.md](../open-questions.md) — hosting kararıyla birlikte netleşecek sağlayıcı seçimi
