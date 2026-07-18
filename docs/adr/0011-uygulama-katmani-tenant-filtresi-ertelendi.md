# ADR 0011: Uygulama katmanı tenant filtresi ertelendi, RLS tek katman

**Durum:** Kabul edildi
**Tarih:** 2026-07-18

## Bağlam

[tenant-model.md](../platform/tenant-model.md) ve [tenant-isolation-enforcement.md](../platform/tenant-isolation-enforcement.md) "iki katmanlı savunma" tanımlıyordu: (1) `core`'daki ortak bir repository/base sınıf her sorguya `tenant_id` ekler, (2) RLS. Faz 1'in tam denetiminde katman 1'in hiç kodlanmadığı bulundu — tüm repository'ler (`UserRepository`, `MemberRepository`, `MembershipRepository`, ...) düz `JpaRepository<T, ID>`, hiçbir ortak taban sınıf yok.

Hibernate'in `@Filter` mekanizması (`@FilterDef`+`@Filter`, session bazlı, tüm sorgulara otomatik `tenant_id` koşulu ekler) bunun standart çözümü. Ama bu projede `spring.jpa.open-in-view=false` + her repository çağrısının kendi, ayrı transaction'ını açması nedeniyle (`TenantAwareDataSource`/`AuthenticationServiceImpl`'in yorumlarında defalarca açıklanan mimari), filtreyi "her transaction başında güvenilir şekilde etkinleştir" diyebileceğimiz tek, stabil bir kanca yok: Spring'in shared `EntityManager` proxy'si her repository çağrısında kendi Session'ını açıyor, filtreyi istek başına ayrı bir yerde (örn. bir servlet filter'da) etkinleştirmek o çağrının kullandığı Session'a değil, atılacak farklı bir Session'a uygulanmış olurdu.

## Karar

Katman 1 (uygulama katmanı tenant filtresi) şimdilik **kurulmuyor**. Tenant izolasyonu bugün yalnızca RLS (katman 2) tarafından sağlanıyor — bu, `TenantRlsTest`/`MembershipModelTest`/`TenantAuditTest` gibi testlerle defalarca kanıtlanmış, gerçekten çalışan bir mekanizma. `tenant-model.md` ve `tenant-isolation-enforcement.md` mevcut durumu yansıtacak şekilde güncellendi.

## Sonuç

- Güvenlik açısından bir gerileme yok: RLS zaten "kod yanlış yazılsa/unutulsa bile" çalışan katmandı, asıl garantiyi hep o veriyordu.
- Transaction mimarisi ileride değişirse (örn. istek başına tek bir `EntityManager` açılırsa, ya da tüm request'i saran bir `@Transactional` sınırı eklenirse), Hibernate `@Filter`'ı güvenilir şekilde etkinleştirmek mümkün hale gelir — bu ADR o noktada yeniden değerlendirilir.

## Geçmiş bağlam

- **Orijinal olay/bulgu:** Faz 1'in tam denetiminde dokümanların iddia ettiği "ortak repository/base sınıf" hiçbir yerde bulunamadı.
- **Reddedilen alternatifler:** Hibernate `@Filter` + `repositoryBaseClass` ile `SimpleJpaRepository`'yi saran özel bir taban sınıf değerlendirildi — ama bu yalnızca `SimpleJpaRepository`'nin kendi metodlarını (`findById`, `findAll`) sarar; Spring Data'nın türetilmiş sorgu metodları (örn. `UserRepository.findByEmail`) `EntityManager`'a doğrudan gider, bu taban sınıfı hiç görmez. Sonuç "bazı sorgularda çalışır, bazılarında çalışmaz" gibi yanıltıcı bir kısmi çözüm olurdu — hiç olmamasından daha kötü, çünkü var olduğu izlenimi verirdi.
- **Başarısızlık senaryosu:** Bu ADR yazılmasaydı, biri "doküman böyle diyor" diyerek var olmayan bir korumaya güvenip RLS'i gevşetebilirdi.
