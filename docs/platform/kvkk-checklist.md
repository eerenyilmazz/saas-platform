# KVKK / Veri Koruma Checklist'i

> Bu doküman bir mühendislik checklist'idir, hukuki danışmanlık değildir. Gerçek yayın öncesi bir avukata KVKK aydınlatma metni ve veri işleme envanterini onaylatmak gerekir — bkz. konuşmanın sonundaki açık sorular listesi.

Platform, üye adı/telefon/giriş-çıkış kayıtları gibi kişisel veri tutuyor; fitness tarafında check-in logu da dolaylı olarak kişinin hareket alışkanlığını gösterebilir. Bu nedenle KVKK kapsamına giriyor.

## Mühendislik tarafında yapılması gerekenler

- **Açık rıza metni** — tenant_admin, yeni üye eklerken KVKK aydınlatma metnini üyeye gösterip onay almalı; onay durumu `member` kaydında bir alan olarak tutulmalı (`consent_at`, bkz. [modules/core.md](../modules/core.md) — alan adı İngilizce, hukuki referans olarak "KVKK" sadece dokümanda geçer, kod içinde geçmez).
- **Veri saklama süresi** — üyelik sona erdikten sonra kişisel verinin ne kadar süre tutulacağı tanımlanmalı (öneri: yasal zorunluluklar hariç 2 yıl sonra anonimleştirme), bu bir zamanlanmış görev (scheduled job) ile otomatikleştirilebilir.
- **Silme/unutulma talebi** — bir üye "verimi sil" dediğinde, `core`'da bu talebi işleyecek bir endpoint (`DELETE /api/v1/members/{id}/personal-data`) tanımlanmalı; ilişkili check-in/randevu kayıtları anonimleştirilir, tamamen silinmez (mali kayıt zorunluluğu nedeniyle).
- **Üçüncü taraf paylaşımı** — SMS sağlayıcısı, ödeme sağlayıcısı gibi üçüncü taraflara veri aktarımı için ayrı bir veri işleme sözleşmesi (VİS) gerekir; bu sözleşmeler hukuki tarafın işi, ama sistemde "bu tenant'ın verisi hangi üçüncü taraflara gidiyor" listesi tutulmalı.
- **Veri işleme envanteri** — hangi tabloda hangi kişisel veri tutulduğunun listesi (bu envanter [tenant-model.md](tenant-model.md) ve modül spec'lerindeki "Veri modeli" bölümlerinden türetilebilir).

## Kapsam dışı (MVP'de yok, ama not düşülmeli)

- Otomatik anonimleştirme job'ı — v1 sonrası.
- Çok dilli aydınlatma metni — şimdilik sadece Türkçe.

## İlgili dosyalar

- [tenant-model.md](tenant-model.md) — hangi tablolarda kişisel veri tutulduğu
