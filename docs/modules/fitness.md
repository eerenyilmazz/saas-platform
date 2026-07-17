# Modül: fitness

**Paket:** `com.saas.modules.fitness.*`
**Bağımlılık:** `core` (auth, tenant, membership, billing)
**Öncelik:** 2

## Sorumluluklar

- **QR tabanlı giriş/çıkış** — üye QR kodu, kısa aralıklarla yenilenen token (üye_id + zaman damgası + imza)
- **Giriş kontrolü** — token okunduğunda `core.membership` üzerinden aktiflik kontrolü, giriş logu kaydı
- **Turnike/RFID desteği** — opsiyonel alt modül, tenant bazlı feature flag ile açılır
- **PT (personal trainer) randevu takvimi**
- **Ürün/stok satışı** (protein, içecek vb.) — ileri faz, `core.billing` üzerinden

## Akış (giriş kontrolü)

```
[QR Okuyucu/Tablet] → [API: /fitness/checkin] → [core.membership kontrolü] → [onay/red] → [giriş logu]
```

- Token doğrulama offline senaryoyu da düşünmeli: yerel cihaz son senkronize üyelik listesini cache'ler.
- Turnike bağlıysa onay sonrası röle/GPIO tetiklenir; yoksa görevli ekranında onay gösterilir.

## QR token güvenlik detayı

- Token, HMAC-SHA256 ile imzalanır: `token = base64(member_id + timestamp) + "." + HMAC(member_id + timestamp, tenant_secret_key)` — JWT kullanmak da bir alternatif ama bu kadar kısa ömürlü/basit bir token için gereksiz genişlik katar, sade HMAC yeterli. Token kişiyi (`member`) temsil eder, belirli bir üyelik kaydını değil — aktiflik kontrolü doğrulama anında `core.membership`'ten dinamik yapılır (bkz. aşağıdaki iptal senaryosu).
- `tenant_secret_key`, tenant başına ayrı üretilir ve tutulur (bir tenant'ın anahtarı sızsa diğerlerini etkilemez) — [security.md](../platform/security.md)'deki genel secrets yönetimi kuralına tabi.
- Token ömrü kısa (öneri: 30 saniye) — üye ekranındaki QR bu aralıkla otomatik yenilenir, ekran görüntüsü paylaşımını anlamsız kılar (süre dolunca geçersiz).
- Token **tek kullanımlık** değildir (aynı üye art arda birden fazla defa girip çıkabilir), ama aynı token'ın kısa süre içinde farklı bir cihazdan okunması (örn. iki farklı salon şubesinde aynı anda) şüpheli aktivite olarak loglanır.
- İptal/iptal senaryosu: üyelik dondurulur/iptal edilirse, o an geçerli token da (henüz süresi dolmamış olsa bile) bir sonraki doğrulamada `core.membership` kontrolünden geçemeyeceği için otomatik işe yaramaz hale gelir — ayrıca bir "token iptali" mekanizmasına gerek yok.

## Veri modeli (taslak)

- `checkin_log` — id, tenant_id, member_id, membership_id (giriş anında hangi üyelik aktifti — raporlama için), checked_in_at, checked_out_at (opsiyonel)
- `qr_token` — member_id, token, issued_at, expires_at
- `training_appointment` — id, tenant_id, trainer_id (→ `core.user`), member_id, scheduled_at

## Kapsam dışı

- Üyelik paketi tanımı, ödeme, fatura → `core`'da.
- Randevu/hizmet süresi mantığı berber modülüne özgü kavramlar, burada tekrar edilmez.
