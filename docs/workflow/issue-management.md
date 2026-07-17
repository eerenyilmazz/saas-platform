# Issue Yönetimi

[branch-strategy.md](branch-strategy.md) bir görev **branch'e döküldükten sonrasını** anlatıyor — bu doküman görevin **nasıl tanımlandığını** (issue) ve branch'e nasıl bağlandığını tanımlar.

## Issue tipleri

| Tip | Etiket | Ne zaman açılır |
|---|---|---|
| `feature` | `type:feature` | Yeni bir özellik/modül parçası |
| `bug` | `type:bug` | Var olan davranış hatalı |
| `chore` | `type:chore` | Kod yazımı içermeyen iş (bağımlılık güncelleme, dokümantasyon) |
| `spike` | `type:spike` | Araştırma/karar gerektiren, henüz koda dökülmeyecek iş — çıktısı genelde bir [ADR](../adr/_template.md) olur |

## Zorunlu etiketler

Her issue en az şu üç etiketi taşır:

- **Modül**: `module:core`, `module:fitness`, `module:berber`, `module:platform` — hangi klasöre dokunulacağını baştan belirtir, [branch-strategy.md](branch-strategy.md)'deki branch isimlendirmesiyle birebir eşleşir.
- **Öncelik**: `priority:critical` (üretimi etkiliyor), `priority:high`, `priority:normal`, `priority:low`
- **Tip**: yukarıdaki tablo

## Issue şablonu

```markdown
## Bağlam
Bu iş neden gerekli? (bir kullanıcı şikayeti mi, roadmap maddesi mi, bkz. [roadmap/phases.md](../roadmap/phases.md))

## Beklenen davranış
Ne olmalı — mümkünse ilgili modül spec'ine referans ver (örn. [modules/fitness.md](../modules/fitness.md))

## Kapsam dışı
Bu issue'da yapılmayacak olan (scope creep'i önlemek için)

## İlgili dokümanlar (coder agent SADECE bunları okur)
- [ ] docs/modules/<modül>.md
- [ ] docs/platform/<varsa ilgili dosyalar>
- [ ] docs/glossary.md (her zaman)

## Definition of Done
- [ ] Kod yazıldı
- [ ] Test yazıldı (bkz. [testing-strategy.md](testing-strategy.md))
- [ ] İlgili doküman güncellendi (varsa)
```

## Context Hydration — agent'a tüm dokümanı değil, daraltılmış bağlamı ver

63 dosyalık doküman büyüdükçe, bir coder agent'ın her görevde hepsini okuması hem maliyetli hem de "context drift" riski taşır (bir agent `fitness` üzerinde çalışırken `core`'daki küçük bir değişikliği gözden kaçırabilir). Bu yüzden **planner rolünün işinin bir parçası**, issue açılırken yukarıdaki "İlgili dokümanlar" listesini doldurmaktır — coder agent'a görev verilirken sadece bu daraltılmış liste + [glossary.md](../glossary.md) (her zaman zorunlu, terim tutarlılığı için) verilir, 63 dosyanın tamamı değil.

- Minimum zorunlu bağlam her görevde: ilgili modülün spec dosyası + [glossary.md](../glossary.md) + [code-style.md](../platform/code-style.md).
- Değişiklik `core`'a dokunuyorsa (ayrı issue olarak açıldığı için, bkz. aşağıdaki "core'a dokunan issue'lar"), o issue'nun bağlam listesi `core.md` + ilgili `platform/*` dosyalarını içerir.
- Bu liste eksik doldurulursa (agent'ın gerçekte ihtiyaç duyduğu bir dosya listede yoksa) ve bu [code-review-checklist.md](code-review-checklist.md)'de bir tutarsızlık olarak ortaya çıkarsa, planner'ın bir sonraki issue'da bunu düzeltmesi beklenir — bu liste de zamanla öğrenilen bir şey, ilk seferde mükemmel olması gerekmiyor.

## Issue → branch → PR bağlantısı

```
issue #42 (module:fitness, type:feature) → branch: feature/fitness-qr-giris → PR açıklamasında "Closes #42"
```

- Branch adı issue içeriğini yansıtır ama issue numarasını taşımak zorunda değil (branch-strategy.md'deki `feature/<modül>-<özellik>` formatı korunur).
- PR açıklamasında issue'ya `Closes #<numara>` ile referans verilir — PR merge olunca issue otomatik kapanır, bu da audit/izlenebilirlik sağlar (bir kod değişikliğinin hangi ihtiyaçtan doğduğu her zaman geriye izlenebilir).

## core'a dokunan issue'lar

Bir issue hem bir modülü hem `core`'u etkiliyorsa (örn. yeni bir modül core'da eksik bir alan istiyor), **iki ayrı issue** açılır: biri `module:core`, biri `module:<ilgili modül>` — [new-module-checklist.md](new-module-checklist.md)'deki "core'a ayrı görev olarak aç" kuralıyla tutarlı, tek PR'da ikisi birden karışmaz.

## İlgili dosyalar

- [branch-strategy.md](branch-strategy.md)
- [pr-and-definition-of-done.md](pr-and-definition-of-done.md)
