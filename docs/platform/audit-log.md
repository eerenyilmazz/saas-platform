# Audit Log — İşlem Geçmişi

`com.saas.core.audit`

Kim, ne zaman, hangi kaydı değiştirdi sorusuna cevap veren merkezi mekanizma. Özellikle üyelik ve ödeme kayıtlarında "bana yanlış ücret yansıtıldı" gibi bir itiraz geldiğinde, ya da bir personelin hatalı işlem yapıp yapmadığını kontrol etmek gerektiğinde bu log tek kaynak olur.

## Kapsam

Aşağıdaki tablolarda değişiklik (create/update/delete) otomatik loglanır:

- `membership`, `membership_plan` — üyelik kaydı/plan değişiklikleri
- `tenant_payment` — tenant-billing kayıtları
- `platform_subscription` — platform-billing kayıtları
- `user` — rol değişiklikleri, hesap aktif/pasif etme
- `tenant.settings` — feature flag (modül aç/kapa) değişiklikleri

Diğer tablolar (örn. `checkin_log`, `appointment`) zaten kendi başına bir olay kaydı olduğu için ayrıca audit'lenmez — asıl risk "geri dönüşü olan/parayı etkileyen" kayıtlarda.

## Veri modeli

- `audit_log` — id, tenant_id, table_name, record_id, action (`create`/`update`/`delete`), old_value (JSON), new_value (JSON), actor_user_id, actor_role, occurred_at

## Teknik yaklaşım

Her modülde ayrı ayrı audit kodu yazılmaz — `core`'da ortak bir mekanizma (örn. JPA entity listener / AOP interceptor) yukarıdaki tabloları izler, `audit_log`'a otomatik yazar. Yeni bir modül eklenirken audit gerektiren bir tablo varsa, o tablo bu ortak mekanizmaya kaydedilir (bkz. [workflow/new-module-checklist.md](../workflow/new-module-checklist.md)).

## Kim görebilir

- `platform_owner` — tüm tenant'ların audit log'unu görebilir
- `tenant_admin` — sadece kendi tenant'ının audit log'unu görebilir
- `tenant_staff` — göremez

## Kapsam dışı (MVP'de yok)

- Audit log üzerinde arama/filtreleme arayüzü — ilk sürümde sadece kayıt tutulur, inceleme gerektiğinde DB'den sorgulanır. Arayüz, platform admin paneliyle (bkz. [roadmap/phases.md](../roadmap/phases.md) Faz 6) birlikte eklenir.

## İlgili dosyalar

- [tenant-model.md](tenant-model.md) — tenant izolasyonu, audit_log da tenant_id taşır
- [support-access.md](support-access.md) — destek amaçlı erişimin de audit_log'a yazılması
