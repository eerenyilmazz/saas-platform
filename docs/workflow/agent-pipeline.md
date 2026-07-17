# Agent Pipeline

GitHub Actions, git event'lerini (PR açıldı, push edildi) yakalar ve ilgili agent'ı tetikler. Onay/merge adımı otonom değil — kritik değişikliklerde insan döngüde kalır.

## Rol dağılımı

| Rol | Tetikleyici | Görev |
|---|---|---|
| `planner` | Yeni görev / issue | İşi görevlere böler, dokunulacak modülü belirler, **coder'ın okuyacağı daraltılmış doküman listesini doldurur** (context hydration, bkz. [issue-management.md](issue-management.md)) |
| `coder` | Görev ataması | Branch açar, ilgili modülün `docs/modules/*.md` spec'ini okur, kodu yazar, commit atar |
| `reviewer` | PR açıldığında | [code-review-checklist.md](code-review-checklist.md)'deki 6 kategoriyi sırayla kontrol eder — sınır ihlali/güvenlik bulgusu otomatik reddeder, diğerlerini yorum olarak bırakır |
| `tester` | PR açıldığında | Test suite'i çalıştırır, sonucu raporlar |
| `approver` | Review + test geçince | [pr-and-definition-of-done.md](pr-and-definition-of-done.md)'daki tüm maddeler işaretliyse kritik olmayan değişiklikte merge eder, kritikte insana bırakır |

## Rol sınırları — kim neyi yapamaz

Her agent sadece kendi rolünün işini yapar, sınırın dışına çıkması bir bulgu değil, doğrudan bir hatadır:

- `planner` kod yazamaz, sadece görev böler.
- `coder` kendi yazdığı kodu review edemez/onaylayamaz.
- `reviewer` kodu **değiştiremez**, sadece yorum/red bırakır — düzeltmeyi her zaman `coder` yapar.
- `tester` test dışında kod (üretim kodu) değiştiremez.
- `coder`, migration dosyası yazabilir (bu onun işinin parçası) ama merge edilmiş bir migration'ı asla değiştiremez (bkz. [database-conventions.md](../platform/database-conventions.md)).

## Olgunluk seviyeleri — insan onayı zamanla azalır

Pipeline baştan tam otomatik çalışmaz — güven arttıkça insan onayı adım adım geri çekilir. Geçiş **ne sadece takvime/faz numarasına ne de sadece belirsiz bir "hissiyata"** bırakılır — her seviyenin hem bir **faz referansı** (planlama için, ne zaman bekleneceğini bilesin) hem bir **somut kanıt şartı** (gerçek geçiş şartı, faz bitti diye otomatik gevşetme yapılmaz) var:

| Aşama | Faz referansı | Somut geçiş kanıtı | İnsan onayı nerede |
|---|---|---|---|
| **Başlangıç** | Faz 0-1 | — (varsayılan başlangıç noktası) | Planner çıktısından **sonra, coder başlamadan önce** + merge öncesi — iki kontrol noktası da insanda |
| **Orta** | Faz 2 tamamlandıktan sonra (fitness MVP) | Son **10 PR**'ın hiçbirinde [code-review-checklist.md](code-review-checklist.md)'nin 1. kategorisinde (sınır ihlali/güvenlik) bulgu çıkmamış olması | Sadece merge öncesi, planlama otomatik geçer |
| **Olgun** | Faz 4 tamamlandıktan sonra (berber MVP — ikinci modül, cross-module tutarlılık kanıtlanmış) | [ai-failure-recovery.md](ai-failure-recovery.md)'deki prosedürün son 20 PR'da hiç tetiklenmemiş olması | Sadece [pr-and-definition-of-done.md](pr-and-definition-of-done.md)'daki "kritik" işaretli değişikliklerde (ödeme, üyelik mantığı, core) insan onayı, geri kalanı approver'a bırakılır |

**Kural:** Faz numarası dolması **tek başına** geçiş için yeterli değil — somut kanıt şartı da sağlanmalı. Kanıt şartı sağlanmadan faz numarası geçilmişse, bir sonraki seviyeye geçilmez, mevcut seviyede kalınır ve neden geçilemediği (hangi PR'da hangi bulgu çıktı) [ai-failure-recovery.md](ai-failure-recovery.md)'deki kök neden sınıflandırmasına göre değerlendirilir.

## Spec-driven çalışma prensibi

Her agent göreve başlamadan önce **sadece ilgili modülün spec dosyasını** okur (örn. fitness görevinde `docs/modules/fitness.md`), o kapsamın dışına çıkmaz. `core`'a dokunması gereken bir durum varsa bu ayrı bir görev/PR olarak açılır, aynı PR içinde modül sınırı karıştırılmaz.

## Bilgi sıkıştırma — rol bazlı temel okuma seti

[issue-management.md](issue-management.md)'deki Context Hydration, **issue bazında** bağlamı daraltıyordu. Buna ek olarak her rolün, issue'dan bağımsız her zaman geçerli bir **taban okuma seti** var — bu iki mekanizma birlikte çalışır (taban set + issue'ya özel ek dosyalar):

| Rol | Taban okuma seti (her zaman) |
|---|---|
| `planner` | [glossary.md](../glossary.md), [decision-tree.md](../platform/decision-tree.md), [docs-dependency-map.md](../docs-dependency-map.md), [roadmap/phases.md](../roadmap/phases.md) |
| `coder` | [glossary.md](../glossary.md), [code-style.md](../platform/code-style.md), [architecture-constraints.md](../platform/architecture-constraints.md), [domain-invariants.md](../platform/domain-invariants.md) + issue'ya özel modül spec'i |
| `reviewer` | [code-review-checklist.md](code-review-checklist.md), [anti-patterns.md](../platform/anti-patterns.md), [glossary.md](../glossary.md)'deki yasaklı kelimeler |
| `tester` | [testing-strategy.md](testing-strategy.md), [domain-invariants.md](../platform/domain-invariants.md) (her invariant'ın bir testi olmalı) |

Taban set küçük ve sabit tutulur (63+ dosyanın tamamı değil) — amaç her agent'ın **rolüne özgü, sürekli ihtiyaç duyduğu** dosyaları ezbere bilmesi, geri kalanını issue'ya özel olarak alması.

## GraphAI ile ilişkisi

GraphAI markdown'ı doğrudan işlemez — her spec dosyası, GraphAI grafındaki ilgili node'un **statik girdisi** olarak yüklenir (örn. `coder` node'u çalışmadan önce `docs/modules/fitness.md` dosyasını okuyup prompt'a ekler). GraphAI burada node'lar arası veri akışını (spec → coder → PR → reviewer → tester) yöneten boru hattı görevi görür; kararları veren agent'lar (LLM çağrıları) ayrı, akışı bağlayan motor ayrı.

## Maliyet notu

Başlangıçta sadece `reviewer` + `tester` agent'ıyla başlamak önerilir; `planner`/`approver` gibi tam otomasyon katmanları sonraya bırakılabilir.
