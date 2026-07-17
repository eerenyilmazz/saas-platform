# Tenant İzolasyonunun Uygulanması ve Test Edilmesi

[ADR 0008](../adr/0008-row-level-security.md) RLS kararını verdi — bu doküman bunun kod/test seviyesindeki somut karşılığı.

## İki katmanlı savunma

| Katman | Mekanizma | Ne olursa çalışır |
|---|---|---|
| 1. Uygulama | Ortak repository/base sınıf, her sorguya `tenant_id` ekler (bkz. [tenant-model.md](tenant-model.md)) | Kod doğru yazıldıysa |
| 2. Veritabanı | PostgreSQL RLS (bkz. [ADR 0008](../adr/0008-row-level-security.md)) | Kod yanlış yazılsa/unutulsa bile |

Bir agent asla "RLS zaten var, uygulama katmanında tenant_id yazmama gerek yok" diye düşünmemeli — iki katman **birbirinin yedeği**, ikisi de her zaman uygulanır.

## Migration'a zorunlu adım

[database-conventions.md](database-conventions.md)'deki migration kuralına ek: **tenant verisi taşıyan her yeni tablo**, aynı migration içinde RLS'i de aktif eder:

```sql
CREATE TABLE service (...);
CREATE INDEX idx_service_tenant_id ON service(tenant_id);
ALTER TABLE service ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON service
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

Bu üç satır (index + RLS aktif + policy), [new-module-checklist.md](../workflow/new-module-checklist.md)'ye eklenen zorunlu bir kontrol maddesidir — [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 1. kategorisinde (sınır ihlali) reviewer bunu arar, yoksa PR bloklanır.

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

## platform_owner için kasıtlı istisna

[support-access.md](support-access.md)'deki destek modu, RLS'i bilinçli olarak aşması gereken tek senaryo. Bu, RLS policy'sinde ayrı bir kural olarak tanımlanır (`current_setting('app.support_mode', true)` gibi bir bayrak), sadece `support_session` aktifken devreye girer ve her erişim zaten [audit-log.md](audit-log.md)'a yazılır — yani "kimse aşamaz" değil "sadece loglu/yetkili bir yoldan aşılabilir."

## İlgili dosyalar

- [../adr/0008-row-level-security.md](../adr/0008-row-level-security.md)
- [database-conventions.md](database-conventions.md)
- [../workflow/testing-strategy.md](../workflow/testing-strategy.md)
- [support-access.md](support-access.md)
