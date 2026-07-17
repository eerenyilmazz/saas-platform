# Doküman Bağımlılık Haritası

65 dosya var ama hangisinin değişmesi hangilerini "geçersiz kılabileceğini" (invalidate) hiçbir yer söylemiyordu. Bu dosya, **sadece temel/bağlayıcı dosyalar** için bir "değişince kim etkilenir" haritası verir — 65 dosyanın tam grafiğini çıkarmak kendi başına bir bakım yüküne dönüşür ve güncel tutulmazsa yanıltıcı olur, bu yüzden kapsam bilinçli olarak dar tutuldu.

## Etki haritası (temel dosyalar → etkilenenler)

| Değişen dosya | Muhtemelen etkilenenler |
|---|---|
| **glossary.md** | Tüm `modules/*.md`, tüm `platform/*.md` — bir terim değişirse her yerde tutarsızlık riski doğar |
| **tenant-model.md** | `roles-and-access.md`, `billing-model.md`, `event-catalog.md`, `api-conventions.md`, `database-conventions.md`, tüm `modules/*.md` |
| **database-conventions.md** | `data-model-overview.md`, `tenant-isolation-enforcement.md`, tüm `modules/*.md`'deki "Veri modeli" bölümleri |
| **api-conventions.md** | `error-handling.md`, `mobile-compatibility.md`, `api-documentation.md`, tüm modül controller tasarımları |
| **event-catalog.md** | `notifications.md`, `audit-log.md`, `external-integration-resilience.md` |
| **architecture-constraints.md** | `code-review-checklist.md`, `anti-patterns.md`, `bounded-context-matrix.md` |
| **domain-invariants.md** | `concurrency-and-idempotency.md`, `error-handling.md`, ilgili modül spec'leri |
| **billing-model.md** | `pricing-packaging.md`, `tenant-model.md`'deki membership bölümü, `open-questions.md` madde 10 (e-Fatura) |

## Kullanım kuralı

Bir agent (planner) yukarıdaki soldaki dosyalardan birine dokunan bir issue açtığında, sağdaki listede yer alan dosyaları da [issue-management.md](workflow/issue-management.md)'deki "İlgili dokümanlar" alanına otomatik ekler — insan/agent'ın bunu hatırlamasına güvenilmez, tablo bunun için var.

## Bu harita nasıl büyür

Yeni bir temel dosya (örn. yeni bir modül `core`'a yakın bir kavram getirirse) eklendiğinde, bu tabloya satır eklenir. Kapsam dışı bırakılan (tabloya girmeyen) dosyalar — roadmap, ADR, workflow prosedürleri — birbirini bu derece etkilemez, o yüzden burada yok.

## İlgili dosyalar

- [docs-versioning.md](docs-versioning.md) — bu haritanın context drift tespitinde nasıl kullanıldığı
- [workflow/change-impact-analysis.md](workflow/change-impact-analysis.md)
