# Terim Sözlüğü

Bu dosya, birden fazla modül/agent üzerinde tutarlılık sağlamak için terimleri sabitler. Yeni bir terim gerektiğinde önce burada tanımlanır, sonra kodda kullanılır — tersi olmaz.

> Bu sözlük yazılırken bir tutarsızlık bulundu ve düzeltildi: `berber.md` daha önce `customer_id` kullanıyordu, `core.md` ise üyeyi ayrı bir tablo olmadan `user`'a (personel/giriş tablosu) bağlıyordu. İkisi de aşağıdaki `member` tanımına göre düzeltildi.

## Kişi/kimlik terimleri

| Terim | Tanım | Karıştırılmaması gereken |
|---|---|---|
| **tenant** (işletme) | Platformu kullanan bir firma (bir berber salonu, bir spor salonu). Türkçe karşılığı dokümanlarda "işletme"/"firma" olarak da geçebilir ama tablo/kod adı her zaman `tenant`. | `platform` ile karıştırılmaz — platform senin şirketin, tenant senin müşterin. |
| **platform_owner** | Platformu işleten sen/ekibin. Tüm tenant'ları yönetebilen tek rol. | `tenant_admin` ile karıştırılmaz — o sadece kendi tenant'ını yönetir. |
| **user** (sistem kullanıcısı) | Sisteme **giriş yapan**, işletme içi kişi — `tenant_admin` ya da `tenant_staff` rolüne sahip. Şifre/oturum bilgisi `user` tablosunda tutulur. | **member** ile karıştırılmaz — her `user` işletme çalışanıdır, işletmenin müşterisi değildir. |
| **member** (üye) | İşletmenin **kendi müşterisi** — spor salonu üyesi, berber müşterisi. `membership` (varsa) buna bağlanır. Giriş yapabilir de yapmayamaz da (`user_id` alanı nullable) — örn. fitness üyesi QR app'i için giriş yapar, berber müşterisi çoğu zaman yapmaz. | **user** ile karıştırılmaz. Kodda/tabloda asla `customer` ya da başka bir isim kullanılmaz, her zaman `member`. |
| **staff** (personel) | Bir `user` kaydına sahip, `tenant_staff` rolündeki kişi — örn. berberde çalışan kuaför, fitness'te resepsiyon görevlisi. Ayrı bir tablo değildir, `user` tablosunun bir alt kümesidir. | — |

## Sistem/mimari terimleri

| Terim | Tanım |
|---|---|
| **modül** | Bir dikeyin (sektörün) iş mantığını taşıyan bağımsız paket (`packages/modules/<ad>`) — örn. fitness, berber. |
| **core** | Tüm modüllerin ortak kullandığı çekirdek katman (auth, tenant, membership, tenant-billing, bildirim, audit). |
| **platform** | Senin (SaaS sahibinin) tenant'ları yönetmek için kullandığı katman — tenant'lar bunu görmez/bilmez. |
| **feature flag** | Bir modülün belirli bir tenant'ta açık/kapalı olduğunu belirten ayar, `tenant.settings.modules.<ad>.enabled`. |
| **source_type / source_id** | `tenant_payment` tablosunda ödemenin neye karşılık geldiğini gösteren genel referans (`membership`, `appointment`, `order` vb.) — bkz. [modules/core.md](modules/core.md). |
| **platform-billing** | Tenant'ın **sana** ödediği SaaS aboneliği. | 
| **tenant-billing** | Tenant'ın **kendi müşterisinden** aldığı ödeme. |
| **destek modu (support session)** | `platform_owner`'ın bir tenant'ın verisine geçici, loglu erişimi — bkz. [platform/support-access.md](platform/support-access.md). |
| **ADR (Architecture Decision Record)** | Bir mimari kararın bağlamını/gerekçesini kalıcı olarak kaydeden dosya, `docs/adr/` altında. |

## Yasaklı kelimeler

Aşağıdaki kelimeler kodda/tabloda **asla** kullanılmaz — solundaki doğru terim yerine sağdaki yanlışlıkla yazılırsa, [code-review-checklist.md](workflow/code-review-checklist.md)'nin 2. kategorisinde (terminoloji) bulgu olarak işaretlenir ve PR bloklanmaz ama düzeltilmesi istenir.

| Doğru terim | Yasaklı alternatifler | Gerekçe |
|---|---|---|
| `member` | `customer`, `client`, `musteri` | Bu tesadüfen değil bilinçli seçildi — `core.md`/`berber.md` arasındaki gerçek bir tutarsızlıktan (bkz. dosyanın başındaki not) sonra sabitlendi |
| `tenant` | `salon`, `business`, `firma`, `company` (doküman içi Türkçe açıklamada "işletme" geçebilir, ama kod/tabloda hep `tenant`) | Sektörden bağımsız isim — "salon" sadece berber/fitness'e özgü çağrışım yapar, ileride restoran/inşaat eklenince yanıltıcı olur |
| `tenant_admin` | `owner`, `admin` (tek başına — hangi seviyede admin belirsiz kalır) | `platform_owner` ile karışmaması için seviyenin isimde açık olması gerekir |
| `platform_owner` | `superadmin`, `root`, `system_admin` | Bu bir teknik yetki seviyesi değil, iş rolü — "sistem admini" gibi genel bir isim, rolün "tüm tenant'ları yöneten SaaS sahibi" anlamını taşımaz |
| `staff` | `employee`, `worker`, `personel` (kod/tabloda) | `user` tablosunun bir alt kümesi olduğu için `user`'la aynı köke ait tutarlı bir terim kullanılır |
| `source_type`/`source_id` | `reference_type`, `polymorphic_type`, `kaynak_tipi`/`kaynak_id` (Türkçe alan adı — bkz. [code-style.md](platform/code-style.md) "kod her zaman İngilizce" kuralı) | `billing-model.md`'nin restoran stres testiyle bulunan genelleme, farklı isimle anılırsa o bağlam kaybolur |

## Yazım kuralı

- Tablo/alan adları her zaman İngilizce, tekil, snake_case (`member`, `tenant_payment`, `member_id`).
- Doküman içi açıklamalar Türkçe yazılır, ama terim ilk geçtiğinde parantez içinde İngilizce/teknik karşılığı da verilir (örn. "üye (`member`)").
- Yeni bir terime ihtiyaç duyan modül yazarı (insan ya da agent), önce bu dosyaya bakar; burada karşılığı yoksa yeni terimi burada tanımlar, sonra kullanır.

## İlgili dosyalar

- [modules/core.md](modules/core.md) — `user`/`member` ayrımının uygulandığı veri modeli
- [platform/code-style.md](platform/code-style.md) — bu terimlerin kod isimlendirmesine nasıl yansıdığı
