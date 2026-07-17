# Doküman Versiyonlama & Context Drift Tespiti

## Problem

Bir issue açıldığında `architecture-constraints.md`'nin belirli bir haline göre planlanır. Coder o issue'yu günler sonra alıp kodu yazar — bu sırada `architecture-constraints.md` değişmiş olabilir. Reviewer PR'ı yine başka bir anda inceler. **Hiçbiri hata yapmadı ama üçü farklı "gerçekliğe" göre çalıştı** — sistem, bireysel hatalar olmadan da tutarsız sonuç üretebilir.

## Karar: manuel semver değil, git commit SHA

`docs/` için elle güncellenen bir sürüm numarası (`v1.2.4` gibi) **kullanılmıyor** — bunu her doküman değişikliğinde elle bump etmek, tam olarak önlemeye çalıştığımız "unutma" riskinin kendisi olur. Bunun yerine zaten var olan git geçmişi kullanılır:

- Her issue açıldığında, [workflow/issue-management.md](workflow/issue-management.md)'deki şablona bir alan eklenir: **`Spec Commit: <docs/ klasörünün o anki git commit SHA'sı>`**
- Coder, göreve başlarken bu SHA'daki doküman haliyle çalıştığını bilir.
- PR açıldığında, **açılış anındaki mevcut SHA ile issue'daki SHA karşılaştırılır**: aradaki commit'lerde issue'nun referans aldığı dosyalardan biri değiştiyse, PR'a otomatik bir uyarı eklenir: *"Bu PR, `architecture-constraints.md`'nin N commit eski bir haline göre açıldı, aradaki değişikliği kontrol et."*

## Context Drift Tespiti — somut mekanizma

```
issue açılır → Spec Commit: abc123 kaydedilir
        ↓
coder çalışır (günler geçebilir)
        ↓
PR açılır → mevcut docs/ SHA: def456
        ↓
git diff abc123..def456 -- docs/<issue'nun "İlgili dokümanlar" listesindeki dosyalar>
        ↓
fark varsa → PR'a otomatik yorum: "⚠️ Context drift: şu dosyalar değişti: [...]"
```

- Bu kontrol basit bir CI script'i (birkaç satır git komutu) — ayrı bir araç/servis gerekmez, zaten git deposu olduğu için ücretsiz.
- Fark bulunması PR'ı bloklamaz (çoğu zaman ilgisiz bir değişikliktir), sadece reviewer'ın/insanın dikkatine sunulur.

## Hangi dosyalar bu takibe tabi

Her dosya değil — sadece **bağlayıcı kural içeren** dosyalar (bkz. [docs-dependency-map.md](docs-dependency-map.md)): `architecture-constraints.md`, `domain-invariants.md`, `glossary.md`, `tenant-model.md`, `database-conventions.md`, `api-conventions.md`, `event-catalog.md`. Roadmap/ADR gibi "geçmişi anlatan" dosyalar bu takibe girmez, onlar zaten değişmeyecek şekilde tasarlandı (ADR'ler "Kabul edildi" olduktan sonra değişmez, yenisi açılır).

## İlgili dosyalar

- [docs-dependency-map.md](docs-dependency-map.md) — hangi dosya değişince hangileri etkileniyor
- [workflow/issue-management.md](workflow/issue-management.md) — Spec Commit alanının şablona eklenmesi
- [workflow/change-impact-analysis.md](workflow/change-impact-analysis.md) — bir dosya kasıtlı değiştirildiğinde izlenecek süreç
