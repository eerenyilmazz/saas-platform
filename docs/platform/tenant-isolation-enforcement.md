# Tenant İzolasyonunun Uygulanması ve Test Edilmesi

[ADR 0008](../adr/0008-row-level-security.md) RLS kararını verdi — bu doküman bunun kod/test seviyesindeki somut karşılığı.

## Savunma katmanı — bugün tek katman

| Katman | Mekanizma | Durum |
|---|---|---|
| 1. Uygulama | Ortak repository/base sınıf, her sorguya `tenant_id` ekler (bkz. [tenant-model.md](tenant-model.md)) | **Ertelendi** — [ADR 0011](../adr/0011-uygulama-katmani-tenant-filtresi-ertelendi.md): mevcut transaction mimarisinde (her repository çağrısı kendi Session'ını açıyor) güvenilir şekilde kurulamıyor |
| 2. Veritabanı | PostgreSQL RLS (bkz. [ADR 0008](../adr/0008-row-level-security.md)) | Aktif — kod yanlış yazılsa/unutulsa bile çalışır |

Bugün tenant izolasyonunun tamamı RLS'e dayanıyor — bu kırılgan değil (aşağıdaki testler bunu kanıtlıyor), ama tek katman. Katman 1 eklenirse (ADR 0011'in yeniden değerlendirilmesiyle) bu tablo güncellenir.

## Migration'a zorunlu adım

[database-conventions.md](database-conventions.md)'deki migration kuralına ek: **tenant verisi taşıyan her yeni tablo**, aynı migration içinde RLS'i de aktif eder:

```sql
CREATE TABLE service (...);
CREATE INDEX idx_service_tenant_id ON service(tenant_id);
ALTER TABLE service ENABLE ROW LEVEL SECURITY;
ALTER TABLE service FORCE ROW LEVEL SECURITY;  -- bkz. aşağıdaki "tablo sahibi" tuzağı, ASLA atlanmaz
CREATE POLICY tenant_isolation ON service
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid);
```

Bu dört satır (index + RLS aktif + RLS zorunlu + policy), [new-module-checklist.md](../workflow/new-module-checklist.md)'ye eklenen zorunlu bir kontrol maddesidir — [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 1. kategorisinde (sınır ihlali) reviewer bunu arar, yoksa PR bloklanır.

### Kritik tuzak: `FORCE` olmadan RLS tablo sahibini hiç etkilemez

`ENABLE ROW LEVEL SECURITY` **tek başına yetersiz** — PostgreSQL, RLS'i varsayılan olarak tablo **sahibi olmayan** rollere uygular. Uygulama, tabloyu migration'la oluşturan **aynı** veritabanı kullanıcısıyla bağlandığı için (yani tablonun sahibi kendisi), `FORCE ROW LEVEL SECURITY` eklenmezse policy **sessizce hiç çalışmaz** — hata vermez, sadece işe yaramaz. Bu, Faz 1'de gerçek bir Testcontainers testiyle yakalandı (kod incelemesiyle değil): `tenant` tablosu için ilk yazılan migration `FORCE` içermiyordu, test "context olmadan 0 satır görünmeli" diye beklerken tüm satırları gördü. `FORCE` eklenene kadar bu böyleydi.

**Sonuç:** `FORCE ROW LEVEL SECURITY` satırı asla atlanmaz, "zaten ENABLE ettim" yeterli değildir.

### Daha kritik tuzak: superuser `FORCE`'u bile atlar

`FORCE` bile **superuser rolleri** için hiçbir işe yaramaz — PostgreSQL'de superuser, RLS'i koşulsuz atlar, bu davranış kapatılamaz. Yerel/Docker kurulumunda `POSTGRES_USER` ile açılan rol **her zaman superuser**'dır. Migration'ları çalıştıran rolle uygulamanın çalışma zamanında bağlandığı rol **aynı** olursa, yukarıdaki tüm RLS kurulumu (ENABLE + FORCE + policy) sessizce hiçbir işe yaramaz.

**Zorunlu ayrım — her ortamda iki rol:**

| Rol | Kullanım | Yetki |
|---|---|---|
| Migration rolü (örn. `saas`) | Sadece Flyway migration'larını çalıştırır (`spring.flyway.url/user/password`) | Superuser (DDL, `CREATE ROLE` için gerekli) |
| Uygulama rolü (`saas_app`) | Uygulamanın çalışma zamanı bağlantısı (`spring.datasource.url/username/password`) | `NOSUPERUSER NOBYPASSRLS`, sadece ihtiyacı olan tablolarda `SELECT/INSERT/UPDATE/DELETE` grant'i |

`saas_app` rolü ve temel grant'leri, ilk migration'da (`V202607171821__create_application_role.sql`) oluşturulur — her yeni tablo, kendi migration'ında bu role `GRANT SELECT, INSERT, UPDATE, DELETE ON <tablo> TO saas_app;` eklemek zorundadır, yoksa uygulama o tabloya hiç erişemez (RLS'ten bağımsız, temel bir yetki hatası olarak).

Bu iki bulgu (FORCE ve superuser) tek bir gerçek Testcontainers test çalıştırmasıyla ortaya çıktı — ikisi de kod incelemesiyle fark edilmezdi, ikisi de "RLS kuruldu" görünümü verirken sessizce hiçbir şeyi korumuyordu.

## Test — bu bir "iyi olursa" değil, zorunlu gate

[../workflow/testing-strategy.md](../workflow/testing-strategy.md)'deki "tenant izolasyon testi" artık iki senaryoyu da kapsar:

```java
@Test void applicationLayerFilteringWorks() {
    // normal akış: repository doğru tenant_id ile sorgular
}

@Test void rlsCannotBeBypassed() {
    // KASITLI OLARAK tenant_id filtresini atlayan ham bir SQL sorgusu çalıştır
    // beklenen: yine de sadece o session'ın tenant'ının satırları döner
    // (RLS'in gerçekten çalıştığının kanıtı — sadece var olduğunu değil, iş gördüğünü test eder)
}
```

İkinci test özellikle önemli: RLS'in "aktif edildiğini" değil, "gerçekten sızıntıyı engellediğini" doğrular — bir migration'da `ENABLE ROW LEVEL SECURITY` unutulursa bu test kırılır.

## platform_owner için kasıtlı istisna — iki farklı bayrak, iki farklı senaryo

platform_owner'ın RLS'i bilinçli olarak aşması gereken **iki ayrı senaryo** var, ikisi de ayrı bir session bayrağıyla çözülür:

| Bayrak | Ne zaman aktif | Hangi tablolar | Örnek |
|---|---|---|---|
| `app.platform_context` | platform_owner'ın `com.saas.platform.*` üzerinden yaptığı **rutin** tenant yönetimi işlemleri (yeni tenant açma, tenant listeleme) | `tenant` tablosunun kendisi | [modules/core.md](../modules/core.md)'daki `tenant` — bkz. [ADR 0008](../adr/0008-row-level-security.md)'deki migration örneği |
| `app.support_mode` | [support-access.md](support-access.md)'deki **geçici, sebep gerektiren** destek modu | Diğer tüm tenant-scoped tablolar (`member`, `appointment` vb.) | Bir tenant "üyemi bulamıyorum" dediğinde |

İkisi de her erişimi [audit-log.md](audit-log.md)'a yazar — "kimse aşamaz" değil "sadece loglu/yetkili bir yoldan aşılabilir." `app.platform_context`, platform_owner'ın günlük işi olduğu için sebep sorulmaz; `app.support_mode` her zaman bir sebep metni ister (bkz. [support-access.md](support-access.md)).

## İlgili dosyalar

- [../adr/0008-row-level-security.md](../adr/0008-row-level-security.md)
- [database-conventions.md](database-conventions.md)
- [../workflow/testing-strategy.md](../workflow/testing-strategy.md)
- [support-access.md](support-access.md)
