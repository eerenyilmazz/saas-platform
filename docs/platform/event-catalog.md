# Domain Event Kataloğu

[notifications.md](notifications.md) ve [external-integration-resilience.md](external-integration-resilience.md) olay tabanlı bir tasarımı (`MembershipExpiringEvent` gibi) varsayıyordu ama olay isimlerinin **hepsinin toplandığı tek bir yer** yoktu — bir agent yeni bir olaya ihtiyaç duyduğunda kendi ismini uydurmasın diye, önce burası kontrol edilir.

## İsimlendirme kuralı

`<Konu><GeçmişZamanFiil>` — her zaman geçmiş zaman (olay zaten olmuş bir şeyi temsil eder), İngilizce, [glossary.md](../glossary.md)'deki terimlerle tutarlı.

## Mevcut olaylar

| Olay | Ne zaman yayınlanır | Kim dinler |
|---|---|---|
| `MemberCreated` | Yeni bir `member` kaydı oluştuğunda | `core.audit` |
| `MembershipExpiringEvent` | Üyelik bitişine N gün kala (bkz. [notifications.md](notifications.md)) | `core.notifications` |
| `MembershipExpiredEvent` | Üyelik süresi dolduğunda | `core.notifications`, ilgili modülün erişim kontrolü |
| `MembershipFrozenEvent` | Üyelik donduruldu | `core.audit` |
| `AppointmentCreatedEvent` | Yeni randevu (berber) | `core.notifications` (onay mesajı) |
| `AppointmentReminderEvent` | Randevuya N saat kala | `core.notifications` |
| `AppointmentCancelledEvent` | Randevu iptal edildi | `core.notifications`, `core.audit` |
| `CheckinFailedEvent` | QR check-in reddedildi (fitness) | `core.audit`, [observability.md](../infra/observability.md) (anormal oran izleme) |
| `PaymentReceivedEvent` | `tenant_payment` kaydı oluştu | `core.audit` |
| `SubscriptionExpiredEvent` | `platform_subscription` gecikti/iptal oldu | `platform` (tenant erişimini kısıtlama tetikleyicisi) |

## Yeni bir olay eklerken kural

1. Önce bu tabloda benzer bir olay olup olmadığına bakılır — varsa o kullanılır, isim tekrar icat edilmez.
2. Yoksa yukarıdaki isimlendirme kuralına göre yeni bir isim türetilir ve **bu tabloya eklenir** (kodla aynı PR'da).
3. Bir olay, sadece onu yayınlayan modülün paketinde tanımlanır (`fitness` bir olay yayınlıyorsa `com.saas.modules.fitness.event` altında) ama bu katalogdaki kayıt sayesinde başka modüller/agent'lar o olayın var olduğunu bilir — [bounded-context-matrix.md](bounded-context-matrix.md)'deki "modüller birbirinin sınıfını import edemez" kuralı burada da geçerli: bir modül başka bir modülün olayını **dinleyemez**, olaylar sadece `core` tarafından dinlenir ve gerekirse `core` üzerinden başka bir modüle yansıtılır.

## İlgili dosyalar

- [notifications.md](notifications.md)
- [audit-log.md](audit-log.md)
- [glossary.md](../glossary.md)
