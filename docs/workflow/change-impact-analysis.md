# Değişim Etki Analizi (Var Olan Kavramı Değiştirme)

[new-module-checklist.md](new-module-checklist.md) **yeni bir şey eklemeyi** anlatıyor. Bu doküman farklı bir senaryo: **var olan, zaten kullanılan bir kavramı** değiştirmek (örn. `membership` tablosunu `subscription` olarak yeniden adlandırmak, bir alanın anlamını değiştirmek). Bu, new-module-checklist'ten daha riskli çünkü zaten üzerine kod yazılmış bir şeye dokunuyor.

## Ne zaman bu süreç izlenir

- Bir tablo/alan/sınıf **yeniden adlandırılıyorsa**
- Bir kavramın **anlamı** değişiyorsa (örn. `membership.durum`'a yeni bir değer eklemek değil, mevcut bir değerin ne anlama geldiğini değiştirmek)
- [domain-invariants.md](../platform/domain-invariants.md)'deki bir değişmez **kasıtlı olarak** gevşetiliyorsa/değiştiriliyorsa

Basit ekleme (yeni bir alan, yeni bir endpoint) bu süreci gerektirmez — sadece normal [issue-management.md](issue-management.md) akışı yeterli.

## Adımlar

1. **Etki taraması** — [docs-dependency-map.md](../docs-dependency-map.md)'den değiştirilecek kavramın hangi dosyalarda geçtiği bulunur; harita eksikse `grep`/arama ile tüm doküman ve kod tabanında kavram adı taranır.
2. **Etki listesi çıkarılır** — hangi dosyalar, hangi tablolar, hangi API alanları etkileniyor; bu bir issue açıklamasına yazılır (bkz. örnek aşağıda).
3. **ADR gerekiyor mu?** — kavram bir [domain-invariants.md](../platform/domain-invariants.md) maddesini ya da bir [ADR](../adr/_template.md)'de alınmış kararı etkiliyorsa, değişiklik önce yeni bir ADR ile gerekçelendirilir (eskisi "vazgeçildi" olarak işaretlenir, değiştirilmez — bkz. [tech-stack.md](../platform/tech-stack.md)'deki ADR immutability prensibi).
4. **Migration + kod + doküman tek bir PR zincirinde** — isim değişikliği asla sadece kodda ya da sadece dokümanda yapılmaz, ikisi birlikte günceller (bkz. [database-conventions.md](../platform/database-conventions.md)'deki migration kuralı — eski kolon silinmez, yeni kolon eklenip veri taşınır, sonra eskisi ayrı bir migration'da kaldırılır).
5. **Glossary güncellenir** — eski terim [glossary.md](../glossary.md)'nin yasaklı kelimeler tablosuna eklenir, yeni terim tanımlanır.

## Örnek: `Membership` → `Subscription` yeniden adlandırması

```markdown
## Etki listesi (change-impact-analysis çıktısı)
- modules/core.md — membership veri modeli bölümü
- modules/fitness.md, modules/berber.md — core.membership referansları
- platform/tenant-model.md — "Üyelik" bölümü
- platform/billing-model.md — tenant_payment.kaynak_tipi enum değeri
- platform/domain-invariants.md — INV-1, INV-3, INV-7
- platform/event-catalog.md — MembershipExpiringEvent, MembershipFrozenEvent
- glossary.md — yasaklı kelimeler tablosuna "membership" eklenir
- Migration: membership tablosu → subscription (rename, veri kaybı yok)
```

Bu liste görülünce, bunun **tek bir küçük PR olamayacağı** hemen anlaşılır — [pr-and-definition-of-done.md](pr-and-definition-of-done.md)'daki "küçük PR kuralı" burada geçerli değil, bu tür kavram değişiklikleri istisnai olarak birden fazla koordineli PR'a bölünür, her biri kendi issue'suna sahip olur ama hepsi tek bir üst issue'ya (`epic`) bağlanır.

## İlgili dosyalar

- [../docs-dependency-map.md](../docs-dependency-map.md)
- [../platform/domain-invariants.md](../platform/domain-invariants.md)
- [new-module-checklist.md](new-module-checklist.md) — bunun tersi senaryo (ekleme, değiştirme değil)
