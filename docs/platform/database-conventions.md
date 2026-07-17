# Veritabanı Kod Kuralları

[tech-stack.md](tech-stack.md) PostgreSQL + Flyway kararını verdi, [data-model-overview.md](../data-model-overview.md) tabloları listeledi — bu doküman **migration'ların nasıl yazılacağını** ve **performans için nelerin baştan kurulacağını** tanımlar. Bunlar sonradan eklenmesi pahalı, baştan kurulması ucuz kararlar.

## Migration dosya kuralları — sıralı değil, zaman damgalı versiyonlama

**İsimlendirme: `V<yyyyAAggSSDD>__<açıklama>.sql`** — örn. `V202607171530__create_member_table.sql`, `V202607181045__add_member_kvkk_onay_tarihi.sql`.

**Neden sıralı numara (`V1`, `V2`, `V3`) değil:** Paralel çalışan birden fazla coder agent, farklı branch'lerde aynı anda migration yazarsa (biri berber, biri fitness için), ikisi de kendi branch'inde "bir sonraki boş numara"yı (örn. `V3`) alır — ana branch'e merge edildiğinde Flyway versiyon çakışması oluşur ve CI/CD hattı kilitlenir. Zaman damgası (dakika hassasiyetinde) bu çakışmayı yapısal olarak imkansız kılar; iki agent aynı dakikada migration yazsa bile bu son derece nadir bir çakışmadır ve normal git merge conflict'i gibi çözülür, versiyon numarası "kimin kazanacağı" tartışmasına dönüşmez. Gerekçe/geçmiş: [ADR 0009](../adr/0009-flyway-timestamp-versioning.md).

- **Merge edilmiş bir migration asla değiştirilmez.** Bir hata bulunursa yeni bir migration ile düzeltilir (yeni bir zaman damgasıyla) — Flyway checksum kontrolü yaptığı için değiştirilmiş bir migration, başka bir ortamda zaten çalışmışsa hata verir.
- Her migration tek bir mantıksal değişikliği içerir (bir tablo + ilişkili index'leri, ya da tek bir alan ekleme) — birden fazla konuyu aynı migration'da birleştirmek review'u zorlaştırır.

## Zorunlu index kuralı

**Her tabloda `tenant_id` index'lenir** — [tenant-model.md](tenant-model.md)'deki izolasyon kuralı gereği her sorgu `tenant_id` ile filtrelendiği için, bu alan index'siz kalırsa tenant sayısı arttıkça tüm sistem yavaşlar. Bu bir "sonra optimize ederiz" konusu değil, her migration'ın parçası.

- Sık birlikte sorgulanan alanlar için composite index (örn. `membership` tablosunda `(tenant_id, member_id)`, `checkin_log`'da `(tenant_id, member_id, giriş zamanı)`).
- Foreign key alanları (`member_id`, `plan_id` vb.) da index'lenir — JPA varsayılan olarak bunu otomatik yapmaz, migration'da elle eklenir.

## N+1 sorgu önleme

- JPA ile ilişkili veri çekerken (örn. bir üyenin `membership` + `membership_plan` bilgisi) varsayılan lazy-loading, döngü içinde her kayıt için ayrı sorguya (N+1 problemi) yol açabilir.
- Kural: bir listeleme endpoint'i (örn. üye listesi + aktif üyelik durumu) yazılırken `JOIN FETCH` ya da `@EntityGraph` kullanılarak tek sorguda çekilir — bu, [code-review-checklist.md](../workflow/code-review-checklist.md)'ye "performans" maddesi olarak eklenir.

## Soft delete kuralı

[api-conventions.md](api-conventions.md)'de "DELETE çoğunlukla soft-delete" deniyordu — somut kural: silinebilir tablolarda (`member`, `service` vb.) bir `deleted_at` (nullable timestamp) alanı bulunur, gerçek `DELETE` SQL komutu hiç kullanılmaz. Sorgular varsayılan olarak `deleted_at IS NULL` filtresini taşır (JPA `@Where` ya da benzeri mekanizma ile merkezi uygulanır, her repository'de tekrar yazılmaz).

## Zorunlu tenant izolasyonu (RLS)

Her yeni tablo, `tenant_id` index'ine ek olarak aynı migration'da Row-Level Security de aktif eder — bu artık bir öneri değil, [ADR 0008](../adr/0008-row-level-security.md) gereği zorunlu adım. Detay ve migration şablonu: [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md).

## Sektöre özgü esnek alanlar (JSONB metadata)

Yeni bir dikey (bkz. [new-module-checklist.md](../workflow/new-module-checklist.md)) genelde birkaç kendine özgü, küçük alan getirir — oto yıkama için "araç plakası", berber için "saç tipi notu" gibi. Bu tür **tek modüle özgü, sık sorgulanmayan** alanlar için her seferinde ana tabloya yeni kolon eklemek yerine bir `metadata JSONB` kolonu kullanılır:

```sql
ALTER TABLE appointment ADD COLUMN metadata JSONB DEFAULT '{}';
```

**Ne zaman kolon, ne zaman metadata JSONB:**

| Durum | Karar |
|---|---|
| Alan sorgu/filtreleme/index gerektiriyor (örn. `staff_id`, `start_at`) | Gerçek kolon |
| Alan birden fazla modülde tekrar ediyor (bkz. [decision-tree.md](decision-tree.md)) | `core`'a taşınmayı değerlendir, kolon olarak |
| Alan tek modüle özgü, nadiren filtrelenir, görüntüleme amaçlı (örn. "saç tipi notu") | `metadata` JSONB içinde |

Bu ayrım [decision-tree.md](decision-tree.md)'deki "core'a mı modüle mi" mantığının bir uzantısı — burada ek olarak "gerçek kolon mu, esnek alan mı" sorusunu cevaplıyor. `metadata` içeriği modülün kendi spec dosyasında (`docs/modules/<ad>.md`) belgelenir, şema serbest ama keyfi değildir.

## İlgili dosyalar

- [tenant-model.md](tenant-model.md) — tenant_id izolasyon kuralı
- [tenant-isolation-enforcement.md](tenant-isolation-enforcement.md) — RLS uygulama detayı
- [tech-stack.md](tech-stack.md) — Flyway kararı
- [decision-tree.md](decision-tree.md) — core/modül/metadata ayrım mantığı
- [../workflow/code-review-checklist.md](../workflow/code-review-checklist.md)
