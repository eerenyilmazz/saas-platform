# Paket İsimlendirme

Spring Boot tarafında Java paket adları klasör yapısını birebir yansıtır — bir dosyanın nerede olduğunu paket adından tahmin edebilmelisin.

| Paket | İçerik | Katman |
|---|---|---|
| `com.saas.platform.tenants` | Tenant provizyonlama, aktif/pasif etme (sadece platform_owner) | platform |
| `com.saas.platform.billing` | Platform'un tenant'tan aldığı abonelik geliri | platform |
| `com.saas.platform.admin` | Platform admin panel API'si | platform |
| `com.saas.core.auth` | Kimlik doğrulama, oturum, rol/yetki | core |
| `com.saas.core.tenant` | Firma/tenant kaydı, izolasyon, feature flag | core |
| `com.saas.core.membership` | Üyelik paketleri, dondurma, yenileme | core |
| `com.saas.core.billing` | Tenant'ın kendi müşterisine kestiği fatura/ödeme | core |
| `com.saas.core.notifications` | SMS/WhatsApp/e-posta bildirim soyutlaması | core |
| `com.saas.modules.fitness.checkin` | Giriş/çıkış kontrolü, token doğrulama | fitness |
| `com.saas.modules.fitness.qr` | QR üretimi, yenileme, okuma API'si | fitness |
| `com.saas.modules.fitness.training` | PT randevu takvimi | fitness |
| `com.saas.modules.berber.booking` | Randevu tipi, hizmet süresi | berber |
| `com.saas.modules.berber.service` | Hizmet/fiyat tanımları | berber |

> `com.saas` yer tutucudur — gerçek şirket/domain adı netleşince tüm paketlerde tek seferde değiştirilecek (bkz. dosya sonundaki açık sorular).

## Kural

- Yeni bir modül eklerken paket adı her zaman `com.saas.modules.<modül_adı>.<alt_alan>` formatında olur.
- `platform.*` paketlerine sadece `platform_owner` rolü erişebilir — bkz. [platform/roles-and-access.md](platform/roles-and-access.md)
- `core` dışındaki hiçbir modül başka bir modülün paketini import edemez, `platform` paketini de import edemez.
- Ortak kullanılan tipler (DTO, enum) `core`'da tanımlanır, modüller oradan tüketir.
- `core.billing` ile `platform.billing` birbirine karıştırılmaz — hangisinin ne olduğu için bkz. [platform/billing-model.md](platform/billing-model.md)
