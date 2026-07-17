# PR Şablonu & Definition of Done

[branch-strategy.md](branch-strategy.md)'deki adım 3 "PR açılır" diyordu ama PR'ın **neyi içermesi gerektiği** ve bir işin **ne zaman gerçekten bitmiş sayılacağı** tanımlanmamıştı.

## PR açıklama şablonu

```markdown
## Ne değişti
Kısa özet (bir cümle yeterli, detay commit'lerde zaten var)

## Neden
Closes #<issue numarası>

## Nasıl test edildi
- [ ] Birim testleri geçti
- [ ] Entegrasyon testleri geçti
- [ ] (varsa) manuel test adımları

## Dokunduğu modül
`module:<core|fitness|berber|platform>` — [branch-strategy.md](branch-strategy.md)'deki çakışma kuralına göre PR sadece bu modülün klasörüne dokunmalı
```

## Definition of Done — bir PR ne zaman gerçekten hazır sayılır

Aşağıdakilerin hepsi sağlanmadan PR merge edilmez:

- [ ] Kod, [code-style.md](../platform/code-style.md)'deki isimlendirme kurallarına uyuyor
- [ ] Kullanılan terimler [glossary.md](../glossary.md) ile tutarlı (yeni bir terim icat edilmemiş)
- [ ] Yeni/değişen her endpoint, [api-conventions.md](../platform/api-conventions.md)'deki URL/hata formatına uyuyor
- [ ] Her yeni tablo `tenant_id` taşıyor (bkz. [tenant-model.md](../platform/tenant-model.md))
- [ ] Kritik tablolara (üyelik, ödeme, rol) dokunuyorsa audit log mekanizmasından geçiyor (bkz. [audit-log.md](../platform/audit-log.md))
- [ ] Birim + entegrasyon testleri yazıldı ve geçiyor (bkz. [testing-strategy.md](testing-strategy.md))
- [ ] Tenant izolasyon testi kırılmadı (CI gate zaten bunu zorunlu kılıyor)
- [ ] PR sadece kendi modülünün klasörüne dokunuyor, `core`'a habersiz dokunmuyor
- [ ] Yeni bir modülse: [data-model-overview.md](../data-model-overview.md) güncellendi (bkz. [new-module-checklist.md](new-module-checklist.md))
- [ ] Reviewer agent onayı + (kritikse) insan onayı var — bkz. [code-review-checklist.md](code-review-checklist.md)

## Max Iteration = 3 kuralı (reviewer↔coder döngüsü)

Coder agent kodu yazar → reviewer reddeder → coder düzeltir → reviewer yine bir şeye takılır — bu döngü agent'lar arasında kontrolsüz uzayabilir, her tur bir API maliyeti demektir.

**Kural:** Aynı PR üzerinde reviewer-coder döngüsü **3 turu** geçerse (reviewer 3. kez red/değişiklik isterse), pipeline otomatik durur ve PR "insan incelemesi gerekiyor" etiketiyle işaretlenir — `approver` ya da `platform_owner` devreye girip ya kararı verir (kabul et/değiştir) ya da issue'yu yeniden kapsamlandırır. Coder 4. bir deneme yapmaz.

- Bu sayaç PR açıklamasında görünür tutulur (`review-iteration: 2/3` gibi).
- Amaç coder'ı cezalandırmak değil — 3 turda çözülemeyen bir anlaşmazlık genelde kuralın kendisinin (code-review-checklist.md'deki bir madde) belirsiz olduğunun işaretidir, bu durumda checklist'in kendisi gözden geçirilir.

## Küçük PR kuralı

Bir PR, tek bir issue'yu kapatacak kadar küçük tutulur. "Bu arada şunu da düzelttim" tarzı, issue kapsamı dışına çıkan değişiklikler ayrı bir issue/PR olur — [branch-strategy.md](branch-strategy.md)'deki kısa ömürlü branch prensibiyle aynı gerekçe: küçük PR, hem insan hem agent review'unu hızlandırır, çakışma riskini azaltır.

## İlgili dosyalar

- [issue-management.md](issue-management.md)
- [code-review-checklist.md](code-review-checklist.md)
