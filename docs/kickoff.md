# Başlangıç Talimatı — Bu Projeye Yeni Başlayan Agent İçin

> Bu dosya, bu repoyu ilk kez gören bir agent'ın (ya da insanın) **ilk yapması gereken şey** olarak tasarlandı. Kullanıcı yeni bir oturum açtığında ilk mesaj olarak şunu yazabilir: *"Bu repoyu klonladım, `docs/kickoff.md`'yi oku ve başla."*

## Adım 1 — Bağlamı oku (sırayla)

1. [README.md](README.md) — tüm doküman setinin indeksi, önce burayı oku
2. [glossary.md](glossary.md) — terimler, hiçbir yerde bunun dışında bir isim uydurma
3. [architecture-constraints.md](platform/architecture-constraints.md) + [domain-invariants.md](platform/domain-invariants.md) — ihlal edilemez kurallar

Bu üçünü okumadan hiçbir soru sorma, hiçbir dosya oluşturma.

## Adım 2 — Açık soruları kullanıcıya sor

[open-questions.md](open-questions.md)'yi aç. İçindeki **10 madde**nin durumunu kontrol et. Dosyanın sonundaki özet tabloya göre:

- **Bloklayıcı olanları** (şu an: madde 1 domain/şirket adı, madde 4 onboarding modeli, madde 8 hosting hedefi) kullanıcıya **tek tek, sade bir dille** sor — dosyadaki teknik metni doğrudan yapıştırma, "bu ne demek" sorusuna kullanıcı cevap verebilsin diye 1-2 cümleyle özetleyerek sor.
- **Bloklayıcı olmayanları** (ödeme/SMS sağlayıcısı, panel eşiği, fiyatlandırma, e-Fatura, KVKK/ToS hukuki onayı) aynı oturumda sormaya çalışma — kullanıcıya "bunlar da var ama şimdi bloklamıyor, istersen sonra konuşuruz" de, cevap istemiyorsa geç.

**Nasıl sor — örnek:**

> "Başlamadan önce 3 karar lazım:
> 1. Şirketin/ürünün gerçek adı ne olacak? (Kod paketlerinde `com.saas.*` yerine kullanılacak)
> 2. İlk müşterileri sen mi manuel açacaksın, yoksa kayıt formuyla kendi kendine mi kayıt olsunlar?
> 3. Sunucu nerede barındırılsın — Hetzner/DigitalOcean gibi ucuz bir VPS mi, yoksa AWS/GCP gibi daha kurumsal bir şey mi?"

## Adım 3 — Cevapları dosyalara işle

Kullanıcı cevap verince, cevabı **sadece kaydetme, ilgili dosyaları güncelle**:

| Cevaplanan soru | Güncellenecek dosyalar |
|---|---|
| Domain/şirket adı | [package-naming.md](package-naming.md), tüm modül dosyalarındaki `com.saas.*` → gerçek isim (toplu değiştirme) |
| Onboarding modeli | [platform/onboarding.md](platform/onboarding.md) — zaten "manuel" varsayımı var, kullanıcı farklı derse bu dosya yeniden yazılır |
| Hosting hedefi | [infra/environments.md](infra/environments.md), [infra/backup-recovery.md](infra/backup-recovery.md) |

Her cevaplanan madde için [open-questions.md](open-questions.md)'deki ilgili bölümün **Durum** satırını `Açık` → `Çözüldü (yyyy-aa-gg)` olarak güncelle, özet tablodan o satırı kaldır.

## Adım 4 — Faz 0'a geç

3 bloklayıcı soru cevaplanınca (ya da kullanıcı "bunlar olmadan da başla" derse — bu durumda varsayılanlar kullanılır ve `open-questions.md`'de not düşülür), [roadmap/phases.md](roadmap/phases.md)'deki **Faz 0**'ı başlat:

- Monorepo iskeletini [repo-structure.md](repo-structure.md)'ya göre oluştur
- [dev-setup.md](dev-setup.md)'deki komutları gerçek proje ile doğrula/güncelle
- [tech-stack.md](platform/tech-stack.md)'deki sürümlerle (Java 21, PostgreSQL, Flyway) iskeleti kur

## Adım 5 — Sonraki her adımda

- [agent-pipeline.md](workflow/agent-pipeline.md)'deki rol sınırlarına uy
- [code-style.md](platform/code-style.md)'deki dil kuralına uy (**kod her zaman İngilizce**, dokümanlar Türkçe)
- Yeni bir issue açmadan önce [issue-management.md](workflow/issue-management.md)'deki şablonu ve Context Hydration listesini doldur
- Bir modülde çalışıyorsan sadece o modülün spec dosyasını + [glossary.md](glossary.md)'yi oku, [docs-dependency-map.md](docs-dependency-map.md)'ye bakıp etkilenen diğer dosyaları kontrol et

## Bu dosya hakkında

Bu, [docs-versioning.md](docs-versioning.md)'deki context hydration mantığının **oturum başlangıcı** karşılığıdır — issue bazlı daraltma issue açıldıktan sonrası için, bu dosya ise "hiçbir issue yokken, sıfırdan başlarken" ne yapılacağını tanımlar.
