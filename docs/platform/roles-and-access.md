# Roller & Erişim Hiyerarşisi

Önceki dokümanlarda sadece tenant-seviyesi roller (admin, personel) vardı. Eksik olan: **platformun sahibi olan sen**'in tüm tenant'ları yönetebileceğin bir rol/katman. Üç seviyeli hiyerarşi:

```
platform_owner   → senin ekibin, tüm tenant'ları görür/yönetir
   └── tenant_admin    → salon sahibi, sadece kendi tenant'ını yönetir
         └── tenant_staff  → salon çalışanı, sınırlı işlem yetkisi
```

## Yetki tablosu

| İşlem | platform_owner | tenant_admin | tenant_staff |
|---|---|---|---|
| Yeni tenant oluşturma | ✅ | ❌ | ❌ |
| Tenant'ın modüllerini aç/kapa | ✅ | ❌ (talep eder, platform_owner uygular) | ❌ |
| Platform aboneliği / fatura görme | ✅ (tüm tenant'lar) | ✅ (sadece kendi ödemesi) | ❌ |
| Üye ekleme/düzenleme (tenant içi) | ✅ | ✅ | ✅ (sadece ekleme, silme yok) |
| Üyelik paketi/fiyat tanımlama | ❌ | ✅ | ❌ |
| Giriş/randevu onayı (günlük operasyon) | ❌ | ✅ | ✅ |
| Başka tenant'ın verisini görme | ✅ (sadece loglu "destek modu" ile, bkz. [support-access.md](support-access.md)) | ❌ | ❌ |

## Teknik uygulama

- `platform_owner` rolü `com.saas.platform.*` paketlerindeki endpoint'lere erişebilen tek roldür.
- `tenant_admin` ve `tenant_staff`, `com.saas.core.*` ve `com.saas.modules.*` altındaki endpoint'lere **sadece kendi `tenant_id`'leri kapsamında** erişir — bu kontrol her istekte zorunlu (bkz. [tenant-model.md](tenant-model.md)).
- `platform_owner`'ın `tenant_id`'si yoktur (ya da özel bir sistem tenant'ına bağlıdır) — cross-tenant sorgu yapabilen tek rol budur, bu yüzden bu rolün endpoint'leri ayrı bir pakette (`platform`) tutulur, core ile karışmaz.

## Platform admin paneli — MVP'de var mı?

**MVP kararı: hayır.** İlk pilot tenant'lar (1-2 salon) veritabanına doğrudan seed script ile eklenir, ayrı bir arayüz gerekmez. Tenant sayısı arttıkça (bkz. [onboarding.md](onboarding.md)) bir web arayüzü (`apps/web` içinde `/platform` route'u, `platform_owner` rolüyle korunan) eklenir — bkz. [roadmap/versions.md](../roadmap/versions.md) v1.2.

## İlgili dosyalar

- [tenant-model.md](tenant-model.md) — tenant izolasyonu
- [billing-model.md](billing-model.md) — platform_owner'ın gördüğü platform-billing
- [onboarding.md](onboarding.md) — yeni tenant nasıl açılır
- [support-access.md](support-access.md) — platform_owner'ın destek amaçlı tenant verisine erişim mekanizması
- [audit-log.md](audit-log.md) — bu tablodaki her yetkili işlemin nasıl izlendiği
