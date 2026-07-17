# Bildirim Altyapısı

`com.saas.core.notifications` — birden fazla modülün ihtiyaç duyduğu (üyelik bitiyor, randevu hatırlatma, giriş onayı) bildirim gönderimi tek bir yerde toplanır. Her modül kendi SMS/e-posta mantığını yazmaz.

## Tasarım

```
[Modül olayı] → [core.notifications.NotificationEvent] → [kanal seçimi] → [sağlayıcı API]
```

- Modüller doğrudan SMS/WhatsApp göndermez, sadece bir olay yayınlar: `MembershipExpiringEvent`, `AppointmentReminderEvent`, `CheckinFailedEvent` gibi.
- `core.notifications` bu olayı dinler, tenant'ın tercih ettiği kanala (SMS/e-posta/WhatsApp) göre gönderir.
- Kanal soyutlaması (`NotificationChannel` arayüzü) sayesinde ileride yeni bir kanal eklemek modülleri değiştirmeyi gerektirmez.

## MVP kararı: sadece SMS

- **WhatsApp Business API** entegrasyonu Meta onay süreci + maliyet gerektirir, MVP'ye eklenmez.
- **SMS** ilk kanal olarak yeterli — Türkiye'de yaygın toplu SMS sağlayıcıları (Netgsm, İletimerkezi vb.) basit REST API sunar, entegrasyonu hızlı.
- E-posta, ikinci öncelik (SMS'e göre daha az acil bildirimler için — örn. fatura bilgisi).

## Örnek olay akışı

```
core.membership → "üyelik 3 gün sonra bitiyor" → MembershipExpiringEvent
        → core.notifications dinler
        → tenant.settings.notifications.channel = "sms" ise → SmsChannel.send(...)
```

## SMS kotası (maliyet koruması)

SMS ücretlidir ve `core.notifications` her tetiklenen olayda otomatik gönderim yapar — bir kod hatası (örn. bir döngünün yanlışlıkla aynı olayı 1000 kez tetiklemesi) gerçek parasal maliyete dönüşebilir. Bu, [security.md](security.md)'deki API rate limiting'den farklı bir konu — kötüye kullanım değil, **maliyet koruması**.

- Her tenant için günlük/aylık SMS gönderim limiti tanımlanır (öneri: pakete göre, bkz. [pricing-packaging.md](pricing-packaging.md)).
- Limit aşılırsa gönderim durur, `platform_owner`'a (SMS değil, e-posta ile — döngüye girmesin diye) bilgi verilir.
- Bu limit `tenant.settings.notifications.daily_sms_limit` altında tutulur, [tenant-model.md](tenant-model.md)'deki esnek `settings` şemasına ek migration gerektirmeden oturur.

## Kapsam dışı

- Bildirim şablonlarının tenant bazlı özelleştirilmesi (marka diliyle mesaj yazma) — v1 sonrası.

## Açık soru

Hangi SMS sağlayıcısıyla anlaşılacağı (Netgsm, İletimerkezi, Twilio vb.) senin tercihine bağlı — bkz. konuşmanın sonundaki açık sorular listesi.
