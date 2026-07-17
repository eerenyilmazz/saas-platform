# Billing Modeli — İki Ayrı Para Akışı

Önceki dokümanlarda tek bir "billing" kavramı vardı, bu yanıltıcı. Aslında **birbirinden tamamen bağımsız iki akış** var — karıştırılırsa senin cironla tenant'ın cirosu aynı tabloda karışır.

## 1. Platform-billing — senin gelirin

`com.saas.platform.billing`

Salon sahibi (tenant), **senin SaaS'ını kullandığı için** sana öder — modül bazlı abonelik.

```
tenant → platform (sana)   [aylık abonelik: aktif modül sayısı × modül fiyatı]
```

- **MVP karar:** Otomatik ödeme entegrasyonu yok. Manuel takip — `platform_subscription` tablosunda tenant, plan, ödeme durumu (ödendi/gecikti) tutulur, sen banka havalesi/manuel fatura ile tahsil edersin.
- **Neden:** İlk 1-2 tenant için otomasyon kurmak gereksiz mühendislik yükü. Tenant sayısı 10+ olunca (bkz. [roadmap/versions.md](../roadmap/versions.md) v1.2) otomatik tekrarlayan ödeme (iyzico/PayTR abonelik API'si) eklenir.

### Veri modeli (taslak)

- `platform_subscription` — id, tenant_id, plan (hangi modüller dahil), monthly_fee, status (`active`/`overdue`/`cancelled`), next_payment_at

## 2. Tenant-billing — tenant'ın kendi geliri

`com.saas.core.billing`

Tenant'ın **kendi müşterisi** (spor salonu üyesi, berber müşterisi), tenant'a öder. Bu akışta sen sadece altyapıyı sağlıyorsun, para senden geçmiyor (senin hesabına girmiyor) — tenant kendi ödeme sağlayıcısını (POS, banka, nakit) kullanır.

```
tenant'ın müşterisi → tenant   [üyelik ücreti, hizmet ücreti]
```

- Bu akış `core.membership` ile birlikte çalışır: bir üyelik satışı hem `membership` kaydı açar hem `core.billing`'de bir tahsilat kaydı oluşturur.
- Her tenant'ta "üyelik" karşılığı olmayabilir (örn. sipariş bazlı çalışan bir dikey) — bu yüzden `tenant_payment` bir üyeliğe değil, **genel bir kaynağa** bağlanır (aşağıya bkz.).
- MVP'de bu da **kayıt tutma** amaçlı — gerçek ödeme tahsilatı (online ödeme linki, kart okuma) v1'den sonraya bırakılabilir; ilk sürümde tenant ödemeyi kendi yöntemiyle alır, sisteme sadece "ödendi" bilgisini girer.

### Veri modeli (taslak)

- `tenant_payment` — id, tenant_id, source_type (`membership`/`appointment`/`order`/...), source_id, amount, method (`cash`/`card`/`transfer`), paid_at

> Neden `membership_id` sabit bir kolon değil: bir modülün "satış birimi" her zaman üyelik ya da randevu olmayabilir (örn. sipariş). `source_type`/`source_id` ile her modül kendi domain nesnesini tenant-billing'e bağlayabilir, `core`'da yeni migration gerekmez. Bu genelleme [modules/core.md](../modules/core.md)'daki veri modeliyle birebir aynı.

## Ayrım tablosu

| | Platform-billing | Tenant-billing |
|---|---|---|
| Kim kime öder | tenant → sen | tenant'ın müşterisi → tenant |
| Paket | `platform.billing` | `core.billing` |
| Kimin görebileceği | sadece `platform_owner` | `tenant_admin`, kendi verisi |
| MVP'de otomasyon | hayır, manuel takip | hayır, kayıt tutma |
| İleride | iyzico/PayTR abonelik | tenant kendi ödeme sağlayıcısını bağlayabilir (v2+) |

## Açık soru

Tenant-billing'de tenant'a hangi ödeme sağlayıcısını önereceğimiz (iyzico, PayTR, Param) senin mevcut iş ilişkilerine bağlı — bkz. konuşmanın sonundaki açık sorular listesi.

## İlgili dosyalar

- [pricing-packaging.md](pricing-packaging.md) — `platform_subscription`'ın kademe yapısı (rakamsız)
