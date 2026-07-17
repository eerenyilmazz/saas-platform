# Destek Erişimi (Impersonation)

## Problem

[roles-and-access.md](roles-and-access.md)'de `platform_owner`'ın "tüm tenant'ları görebildiği" yazıyor ama bu tanım tek başına yetersiz — pratikte şu senaryo ortaya çıkıyor: bir salon sahibi "üyemi sistemde bulamıyorum" diye seni arıyor. Sen (platform_owner) onun sorununu çözmek için o tenant'ın verisine bakman gerekiyor. Bunu nasıl, hangi izlerle yapacağın tanımlı değildi.

## Karar

Platform_owner, **açıkça loglanan, zaman sınırlı bir "destek modu"** ile bir tenant'ın verisine geçici olarak bakabilir. Sınırsız/sessiz erişim yok.

## Akış

```
platform_owner → "bu tenant için destek modu başlat" (sebep girilir, örn. "üye kaydı bulunamıyor şikayeti")
        → sistem bir support_session açar (süre sınırlı, örn. 30 dakika)
        → bu süre boyunca platform_owner o tenant'ın verisini görebilir (salon sahibi gibi)
        → her görüntüleme/işlem audit_log'a "destek modu" etiketiyle yazılır
        → süre dolunca ya da platform_owner kapatınca erişim otomatik sona erer
```

## Kurallar

- Destek modu başlatılırken **sebep zorunlu** — sebepsiz erişim açılamaz.
- Destek modundayken yapılan her işlem [audit-log.md](audit-log.md)'a normal işlemlerden ayrı bir işaretle (`via_support_session: true`) yazılır — böylece bir tenant "hesabıma kim baktı" diye sorduğunda net cevap verilebilir.
- Destek modu **salt okunur** başlar; veri değiştirme (örn. üyelik düzeltme) gerekiyorsa bu ayrı, daha görünür bir onay adımı ister (MVP'de: tenant_admin'e bilgi verilerek yapılır).
- Bir tenant, kendi ayarlarından destek erişimini tamamen kapatamaz (KVKK/güvenlik gereği platform_owner'ın teknik destek verebilmesi gerekir) ama her erişim audit_log'da görünür olduğu için şeffaflık korunur.

## Veri modeli

- `support_session` — id, tenant_id, platform_owner_user_id, reason, started_at, ended_at, status (`active`/`closed`)

## Kapsam dışı (MVP'de yok)

- Tenant'a "şu an destek modundasınız" bildirimi göstermek — ileri fazda eklenebilir, MVP'de audit_log yeterli.

## İlgili dosyalar

- [roles-and-access.md](roles-and-access.md) — platform_owner'ın genel yetkisi
- [audit-log.md](audit-log.md) — destek erişiminin loglanması
- [kvkk-checklist.md](kvkk-checklist.md) — üçüncü kişi (platform ekibi) veri erişimi KVKK kapsamında
