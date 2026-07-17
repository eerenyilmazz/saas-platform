# Domain Değişmezleri (Invariants)

[architecture-constraints.md](architecture-constraints.md) **kodun nasıl yapılanacağını** kısıtlıyordu (katmanlar, import kuralları). Bu dosya farklı bir şey: **iş kurallarının asla ihlal edilemeyecek gerçeklerini** listeler — bunlar kod detayı değil, işin kendisi hakkında mutlak doğrular. Bir agent yeni bir özellik yazarken, mimariye uysa bile bu listedeki bir kuralı çiğneyen bir kod yazabilir; bu liste onu önlemek için var.

## Neden ayrı bir dosya

`architecture-constraints.md`: *"Controller, Repository'yi çağıramaz"* — bu bir **kod yapısı** kararı, değişebilir (örn. katman mimarisi değişirse).
`domain-invariants.md`: *"Bir üye aynı anda iki aktif üyeliğe sahip olamaz"* — bu bir **iş gerçeği**, kod nasıl yazılırsa yazılsın doğru kalmalı.

## Değişmezler

| # | Değişmez | Neden | Nerede uygulanır |
|---|---|---|---|
| INV-1 | Bir `member`, aynı `tenant` içinde aynı anda birden fazla **aktif** `membership`'e sahip olamaz | Çift ücretlendirme/çakışan hak riski | `MembershipService`, DB constraint |
| INV-2 | Aynı `staff_id` için iki randevu **zaman aralığı asla örtüşemez** | Bir personel aynı anda iki yerde olamaz | DB exclusion constraint (bkz. [concurrency-and-idempotency.md](concurrency-and-idempotency.md)) |
| INV-3 | `platform_subscription` **ödeme onaylanmadan** `active` durumuna geçemez | Ücretsiz/yetkisiz erişim riski | `SubscriptionService`, durum makinesi |
| INV-4 | `tenant_payment` kaydı, silinen/var olmayan bir `source_id`'ye asla bağlı kalamaz | Muhasebe/audit bütünlüğü | FK constraint + soft-delete kuralı (bkz. [database-conventions.md](database-conventions.md)) |
| INV-5 | Bir `member`, `consent_at` boşken hiçbir bildirim kanalından (SMS/e-posta) mesaj alamaz | KVKK zorunluluğu | `NotificationService` gönderim öncesi kontrol |
| INV-6 | Bir `user`, kendi `tenant_id`'si dışındaki hiçbir veriye erişemez (platform_owner'ın loglu istisnası hariç) | Tenant izolasyonu | RLS + uygulama katmanı (bkz. [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md)) |
| INV-7 | Dondurulmuş (`frozen`) bir `membership` üzerinden hiçbir modül erişim/giriş izni veremez | Üyelik dondurma anlamsızlaşmasın | Her modülün erişim kontrolü `membership.status`'unu kontrol eder |
| INV-8 | Bir `appointment`/`checkin_log` kaydı oluştuktan sonra `tenant_id` **asla** değiştirilemez | Bir kaydın tenant'lar arası "taşınması" mantıksız ve güvenlik riski | Entity seviyesinde immutable alan |

## Bu liste nasıl büyür

Yeni bir modül eklendiğinde ([new-module-checklist.md](../workflow/new-module-checklist.md)), o modülün kendi iş kuralı değişmezleri varsa (örn. "bir randevu geçmiş bir tarihe alınamaz") buraya eklenir — modülün kendi spec dosyasında da tekrar edilebilir ama **otoriter kaynak burasıdır**, çelişki olursa bu dosya kazanır.

## Test zorunluluğu

Her değişmez, [testing-strategy.md](../workflow/testing-strategy.md)'de en az bir "bunu ihlal etmeye çalış, sistem engellemeli" testine sahip olmalı — INV-6 için zaten `rlsCannotBeBypassed` testi var ([tenant-isolation-enforcement.md](tenant-isolation-enforcement.md)), diğerleri için ilgili modülün Faz'ında aynı desen izlenir.

## İlgili dosyalar

- [architecture-constraints.md](architecture-constraints.md) — kod yapısı kısıtları (bu dosyadan farklı katman)
- [concurrency-and-idempotency.md](concurrency-and-idempotency.md)
- [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md)
- [../docs-dependency-map.md](../docs-dependency-map.md)
