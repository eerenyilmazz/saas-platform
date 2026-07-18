# ADR 0010: Audit mekanizmasının reflection kullanımı - bilinçli istisna

**Durum:** Kabul edildi
**Tarih:** 2026-07-18

## Bağlam

[architecture-constraints.md](../platform/architecture-constraints.md) şunu mutlak bir kural olarak koyuyor: *"ASLA Java reflection ile framework dışı, elle yazılmış bir mekanizma kurulmaz (Spring zaten reflection'ı kendi içinde kullanır, uygulama kodu bunu tekrar etmez)."*

`com.saas.core.audit.AuditListener`/`AuditWriter` ([audit-log.md](../platform/audit-log.md)'nin uygulaması) tam olarak bu kalıbı kullanıyor: `AuditListener.snapshot()` her `@Audited` entity'nin private alanlarını `Field.setAccessible(true)` + `field.get(entity)` ile elle geziyor; `AuditWriter.requireFieldValue()` aynı şekilde `tenantId`/`id` alanlarını elle reflection'la okuyor. Bir Faz 1 tam incelemesinde (bkz. bu ADR'ın "geçmiş bağlam" bölümü) bu, kuralın doğrudan ihlali olarak işaretlendi.

Değerlendirilen alternatif: Jackson'ın kendi reflection'ını kullanacak şekilde (`ObjectMapper` + özel bir `@AuditSensitive` redaksiyon `BeanSerializerModifier`'ı) yeniden yazmak - bu, kuralın parantezindeki "framework'ün reflection'ı sorun değil" ayrımına tam uyardı.

## Karar

Mevcut mekanizma **değiştirilmiyor**, bilinçli bir istisna olarak kabul ediliyor.

Gerekçe: `audit-log.md`'nin tasarımının bütün amacı, yeni bir `@Audited` entity'nin **hiçbir kendi koduna gerek duymadan** (sadece bir annotation ekleyerek) audit edilebilmesi - bu, entity'lerin alan kümesini derleme zamanında bilmeyen, tamamen jenerik bir mekanizma gerektiriyor. Jackson'a geçmek aynı jenerik ihtiyacı karşılar ama:

- Mevcut, test edilmiş, çalışan bir mekanizmayı (create/update/delete snapshot, `@AuditSensitive` redaksiyonu) `AuditListenerTest`'in davranışını bire bir koruyacak şekilde yeniden yazmak gerçek bir risk taşır - refactor'un kendisi bu incelemenin kapsamı dışında, ayrı ve dikkatli bir çalışma ister.
- Reflection kullanımının kendisi güvenlik/encapsulation açısından zaten SonarCloud'da incelendi ve aynı gerekçeyle ("jenerik erişim, entity başına getter yok") Won't Fix olarak kapatıldı - bu ADR, aynı gerekçeyi mimari kural seviyesinde resmileştiriyor.

## Sonuç

- `architecture-constraints.md`'nin "ASLA" listesi, bu tek mekanizma için **istisnai** olarak kabul edilir - bu istisna yeni bir emsal değildir, başka bir yerde "reflection lazım" denilerek genişletilemez.
- İleride bu mekanizma büyük bir revizyondan geçerse (örn. Faz 2'de lazy-association taşıyan bir `@Audited` entity eklenirse - `AuditListener`'ın kendi javadoc'u bunun bugün desteklenmediğini zaten belirtiyor), Jackson tabanlı yeniden yazım o zaman, gerçek bir ihtiyaçla birlikte değerlendirilir.

## Geçmiş bağlam

- **Orijinal olay/bulgu:** Faz 1'in tamamının bağımsız bir agent tarafından incelenmesi sırasında, `architecture-constraints.md`'nin reflection yasağıyla `AuditListener`/`AuditWriter`'ın doğrudan çeliştiği tespit edildi.
- **Reddedilen alternatifler:** Jackson `ObjectMapper` + özel `BeanSerializerModifier` ile yeniden yazım - mimari kurala tam uyar ama mevcut audit trail davranışını bozma riski taşıdığı için bu ADR kapsamında ertelendi.
- **Başarısızlık senaryosu:** Bu ADR yazılmasaydı, gelecekte bu kodu okuyan bir agent/insan reflection kullanımını "kural ihlali, düzeltilmeli" sanıp habersizce bir refactor'a girişebilir - bu ADR, kararın bilinçli ve gerekçeli olduğunu, rastgele bir gözden kaçma olmadığını kayıt altına alır.
