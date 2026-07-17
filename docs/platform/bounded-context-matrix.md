# Bounded Context Matrisi

[repo-structure.md](../repo-structure.md) bağımlılık kuralını prosa olarak anlatıyordu ("modüller core'a bağımlı, birbirine değil"). Bu dosya aynı kuralı **taranabilir bir tablo** olarak verir — modül sayısı arttıkça (fitness, berber, restoran, oto yıkama, kuaför...) prosa okumak yerine bu tabloya bakmak yeterli olsun diye.

## Erişim matrisi

| | core | platform | fitness | berber |
|---|---|---|---|---|
| **core** görebilir mi | — | ❌ | ❌ | ❌ |
| **platform** görebilir mi | ✅ | — | ❌ | ❌ |
| **fitness** görebilir mi | ✅ | ❌ | — | ❌ |
| **berber** görebilir mi | ✅ | ❌ | ❌ | — |

Okuma yönü: satır, sütunu **görebilir mi**. Örnek: "fitness satırı, core sütunu = ✅" → fitness, core'u import edebilir. "fitness satırı, berber sütunu = ❌" → fitness, berber'i asla import edemez.

**Yeni bir modül eklendiğinde** (bkz. [new-module-checklist.md](../workflow/new-module-checklist.md)) bu tabloya yeni bir satır/sütun eklenir — yeni modülün `core` sütunu her zaman ✅, diğer tüm modül sütunları her zaman ❌ olur. Bu hiç değişmez, tartışmaya açılmaz.

## Aynı isimli, farklı entity kuralı

`fitness` ve `berber` ikisi de bir "randevu" kavramına sahip ama bunlar **asla aynı entity olmaya zorlanmaz**:

- `fitness.training_appointment` ve `berber.appointment` iki ayrı tablo, iki ayrı Java sınıfı.
- İsimlerinin benzer olması ("appointment") bir tesadüf, bir paylaşım zorunluluğu değil — [modules/_template.md](../modules/_template.md)'deki "Diğer modüllerle ortak nokta" bölümü bunu **not düşmek** için var, otomatik birleştirmek için değil.
- Bir alan/kavram üçüncü bir modülde de tekrar ederse (fitness + berber + restoran'ın hepsi randevu alıyorsa), o zaman `core`'a taşımak değerlendirilir — ama bu **üçüncü tekrardan önce yapılmaz** (bkz. [modules/_template.md](../modules/_template.md)'deki erken soyutlama uyarısı).

## Paket sahipliği (özet)

| Modül | Paket öneki | Kim değiştirebilir |
|---|---|---|
| `core` | `com.saas.core.*` | Herhangi bir agent, ama değişiklik ayrı bir issue/PR olarak açılır (bkz. [new-module-checklist.md](../workflow/new-module-checklist.md)) |
| `platform` | `com.saas.platform.*` | Sadece platform'a atanmış görevler — modül geliştiren bir agent buraya asla dokunmaz |
| `modules/fitness` | `com.saas.modules.fitness.*` | Fitness görevine atanmış agent |
| `modules/berber` | `com.saas.modules.berber.*` | Berber görevine atanmış agent |

Bu tablo, "kim sahip" kavramını bir insan takımı değil, **hangi görev/issue hangi pakete dokunma izni veriyor** anlamında kullanır — [issue-management.md](../workflow/issue-management.md)'deki `module:` etiketiyle birebir eşleşir.

## İlgili dosyalar

- [../repo-structure.md](../repo-structure.md) — bu matrisin dayandığı prosa kural
- [architecture-constraints.md](architecture-constraints.md) — bu matrisin ihlalinin nasıl engellendiği (ArchUnit)
- [../workflow/new-module-checklist.md](../workflow/new-module-checklist.md)
