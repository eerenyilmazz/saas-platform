# Dış Entegrasyon Dayanıklılığı

Platform, kendi kontrolünde olmayan dış servislere bağımlı: SMS sağlayıcısı ([notifications.md](notifications.md)), ileride ödeme sağlayıcısı ([billing-model.md](billing-model.md) v1.3). Bu servislerden biri yavaşlar/çökerse, bunun tüm API'yi aşağı çekmemesi gerekir.

## Kural: zaman aşımı + devre kesici (circuit breaker) + yeniden deneme

- **Zaman aşımı (timeout):** Her dış servis çağrısına kısa bir üst süre konur (öneri: 3-5 saniye) — SMS sağlayıcısı yanıt vermiyorsa, kullanıcının API isteği sonsuza kadar beklemez.
- **Devre kesici:** Bir dış servis art arda başarısız olmaya başlarsa (öneri: 5 ardışık hata), o servise yapılan çağrılar kısa bir süre (öneri: 1 dakika) otomatik olarak durdurulur ("devre açılır") — her isteğin boşuna zaman aşımına uğramasını önler. Süre dolunca tek bir deneme yapılır, başarılıysa devre kapanır.
- **Yeniden deneme (retry):** Geçici hatalarda (ağ kopması gibi) 1-2 kez otomatik tekrar denenir, kalıcı hatalarda (geçersiz telefon numarası gibi) tekrar denenmez — bu ayrım [error-handling.md](error-handling.md)'deki exception hiyerarşisine dayanır (`ValidationException` tekrar denenmez, ağ kaynaklı olanlar denenir).

## Bu, ana akışı bloklamamalı

**En önemli kural:** SMS gönderimi başarısız olursa, tetikleyen asıl işlem (örn. bir üyelik kaydı oluşturma) **başarısız olmaz**. Bildirim, asıl işlemden ayrı, asenkron bir kuyruk/iş üzerinden gönderilir — `core.notifications`'ın kendi hata durumu, `core.membership`'in işlemini geri almaz (rollback etmez).

- Teknik olarak: bildirim gönderimi, ana transaction commit olduktan **sonra** tetiklenir (Spring `@TransactionalEventListener(phase = AFTER_COMMIT)` gibi bir mekanizma).
- Bildirim gönderimi kalıcı olarak başarısız olursa, bu [observability.md](../infra/observability.md)'deki hata izlemeye düşer ama kullanıcıya gösterilen asıl işlem (üyelik oluşturuldu, randevu alındı) hatasız tamamlanmış olarak kalır.

## Kapsam dışı (MVP'de yok)

- Dış servisler için ayrı bir mesaj kuyruğu altyapısı (RabbitMQ/Kafka) — trafik hacmi bunu gerektirmiyor, uygulama içi asenkron event yeterli.

## İlgili dosyalar

- [notifications.md](notifications.md)
- [error-handling.md](error-handling.md)
- [../infra/observability.md](../infra/observability.md)
