# ADR 0008: Tenant izolasyonu, uygulama katmanına ek olarak veritabanı seviyesinde de (RLS) zorlanır

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

[ADR 0003](0003-shared-db-tenant-id-izolasyonu.md) shared-DB + `tenant_id` kararını verdi. [tenant-model.md](../platform/tenant-model.md), bu izolasyonun "core'daki ortak repository/base sınıfında merkezi olarak uygulandığını" söylüyordu — ama bu **sadece uygulama katmanında** bir disiplin. Bir agent (ya da insan) elle yazılmış bir `@Query`'de, bir raporlama sorgusunda ya da ileride eklenecek bir admin panelinde `tenant_id` filtresini unutursa, hiçbir şey bunu engellemez — bir tenant'ın verisi başka bir tenant'a sızabilir. Shared-DB modelinin **tek ve en büyük riski** budur.

## Karar

Uygulama katmanındaki filtrelemeye **ek olarak** (onun yerine değil), PostgreSQL'in **Row-Level Security (RLS)** özelliği tüm tenant verisi taşıyan tablolarda aktif edilir. Veritabanı, bağlantının hangi tenant için açıldığını bilir ve o tenant dışındaki satırları **sorgu ne yazılırsa yazılsın** hiç döndürmez.

## Nasıl çalışır (özet)

```sql
ALTER TABLE member ENABLE ROW LEVEL SECURITY;
ALTER TABLE member FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON member
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid);
```

- Backend, her istek başında (auth interceptor'da, JWT'den `tenant_id` çıkarıldıktan hemen sonra) veritabanı bağlantısına `SET LOCAL app.current_tenant_id = '<tenant_id>'` komutunu çalıştırır.
- Bundan sonra o bağlantı üzerinden yazılan **her** sorgu (elle yazılmış, JPA'nın ürettiği, raporlama amaçlı olsun) otomatik olarak sadece o tenant'ın satırlarını görür — `WHERE tenant_id = ...` yazmayı unutmak artık bir güvenlik açığı değil, en kötü ihtimalle gereksiz bir satırdır.
- **Uygulama, migration'ları çalıştıran rolle asla aynı rolle bağlanmaz** — bkz. Faz 1 uygulama notu aşağıda. Bu ayrım olmadan yukarıdaki iki satır (`ENABLE`/`FORCE`) hiçbir işe yaramaz.

## Sonuç

- **Savunma derinliği (defense in depth):** Uygulama katmanı (ortak repository) birincil savunma, RLS ikincil/yedek savunma olur — biri atlanırsa diğeri hâlâ durur.
- Maliyet: Her tabloda RLS aktif etmek bir migration adımı (bkz. [database-conventions.md](../platform/database-conventions.md)) — yeni bir tablo eklenirken bu adım [new-module-checklist.md](../workflow/new-module-checklist.md)'ye zorunlu adım olarak eklenir.
- `platform_owner`'ın [support-access.md](../platform/support-access.md)'deki destek modu gibi **kasıtlı olarak** tenant sınırını aşması gereken senaryolar için ayrı, ayrıca loglanan bir veritabanı rolü/bypass mekanizması tanımlanır — RLS bunu da öngörür, "hiç kimse hiçbir zaman göremez" değil, "varsayılan olarak göremez, bilinçli/loglu istisna dışında."
- Detaylı uygulama ve test stratejisi: [platform/tenant-isolation-enforcement.md](../platform/tenant-isolation-enforcement.md).

## Gözden geçirme koşulu

Yok — bu, geri dönüşü kolay olmayan ama maliyeti düşük bir güvenlik kararı; shared-DB modeli kullanıldığı sürece geçerliliğini korur.

## Geçmiş bağlam

- **Orijinal olay/bulgu:** Bu karar, kod hiç yazılmadan önce yapılan bir dış mimari incelemesinden çıktı — harici bir AI değerlendirmesi (Gemini), o ana kadarki tasarımın (`tenant-model.md`'deki "ortak repository filtreler" ifadesi) sadece **uygulama disiplinine** dayandığını, veritabanının kendisinin hiçbir garantisi olmadığını işaret etti. O ana kadar bu, "elbette agent'lar `tenant_id` filtresini unutmaz" varsayımıyla örtük kabul edilmişti.
- **Reddedilen alternatifler:** Hibernate `@Filter` (sadece JPA üzerinden erişimde çalışır, ham SQL/raporlama sorgularında korumasız kalır — bu yüzden yetersiz bulundu). Sadece kod review disiplinine güvenmek (insan/agent hatasına açık, tek katmanlı savunma).
- **Başarısızlık senaryosu:** Bir agent, bir raporlama endpoint'i ya da elle yazılmış bir `@Query` içinde `tenant_id` filtresini unutursa (özellikle çok sayıda agent'ın paralel çalıştığı bir ortamda bu istatistiksel olarak er ya da geç olur), Tenant A'nın üye listesi/randevuları Tenant B'ye sızabilirdi — bu, shared-DB modelinin göze alınan tek büyük riskiydi ve RLS olmadan **sadece zaman meselesiydi**.

## Faz 1 uygulama notu — ikinci bir bulgu, kod yazılırken

Bu kararın Faz 1'de gerçekten koda dökülmesi sırasında (ilk `tenant` tablosu + RLS testi), **`FORCE ROW LEVEL SECURITY` tek başına yetersiz çıktı**: PostgreSQL'de superuser rolleri RLS'i `FORCE` dahil koşulsuz atlar. Yerel/Docker kurulumunda `POSTGRES_USER` ile açılan rol (migration'ları çalıştıran rol) her zaman superuser'dır — uygulama çalışma zamanında da bu rolle bağlansaydı, yukarıdaki `ENABLE`/`FORCE`/`CREATE POLICY` satırlarının **hiçbiri hiçbir işe yaramazdı**, sessizce.

**Düzeltme:** Migration'ları çalıştıran rol (superuser, `spring.flyway.*` ile ayrı yapılandırılır) ile uygulamanın çalışma zamanında bağlandığı rol (`saas_app`, `NOSUPERUSER NOBYPASSRLS`, `spring.datasource.*`) **kesin olarak ayrıldı**. Bu ayrım, [tenant-isolation-enforcement.md](../platform/tenant-isolation-enforcement.md)'ye zorunlu bir adım olarak eklendi — bir Testcontainers testi (`rlsCannotBeBypassed`) bu hatayı, kod incelemesi değil gerçek bir test çalıştırması yakaladı.
