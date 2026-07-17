# Bağımlılık Yönetimi

Modüler monolit çok sayıda agent/kişi tarafından geliştirilecek — her biri kendi işini kolaylaştıracak bir kütüphane eklemeye kalkarsa, kısa sürede birbiriyle çakışan, denetlenmemiş bağımlılıklar birikir.

## Yeni bir bağımlılık eklerken kural

- Yeni bir üçüncü taraf kütüphane eklemek **her zaman** ayrı bir küçük PR'da yapılır, bir özellik PR'ının içine gizlenmez — reviewer'ın bunu ayrıca değerlendirebilmesi için (bkz. [code-review-checklist.md](../workflow/code-review-checklist.md)).
- **Coder agent kendi başına yeni bağımlılık ekleyemez** — [agent-pipeline.md](../workflow/agent-pipeline.md)'deki olgunluk seviyelerinden bağımsız olarak, bağımlılık ekleme her zaman insan onayı gerektiren bir kategori sayılır (approver'a bırakılmaz). Basit bir string/tarih işlemi için ajanın "hızlı bir kütüphane eklerim" refleksi, en sık karşılaşılan sapma olduğu için bu istisnasız tutulur.
- Eklenecek kütüphane şu sorulardan geçer: (1) aktif bakımı var mı (son commit ne zaman), (2) bilinen güvenlik açığı var mı, (3) zaten eklenmiş başka bir kütüphane aynı işi yapıyor mu (örn. iki farklı HTTP client kütüphanesi birden eklenmez), (4) standart kütüphane (Java `java.time`, `Optional` vb.) bu işi zaten görüyor mu — çoğu zaman görüyor.
- Sürüm sabitlenir (`^` veya `~` gibi gevşek aralık kullanılmaz) — beklenmedik bir minor/patch güncellemesinin davranışı sessizce değiştirmesini önlemek için.

## Otomatik güncelleme taraması

- Dependabot (ya da Renovate) CI'a bağlanır, güvenlik açığı olan bağımlılıklar için otomatik PR açar — bu PR'lar normal review sürecinden geçer, otomatik merge edilmez.
- Kritik (CVE'si olan) bir güncelleme çıkarsa, bu PR diğerlerinin önüne geçer (`priority:critical` etiketiyle, bkz. [issue-management.md](../workflow/issue-management.md)).

## Modüller arası bağımlılık tekrarı

Birden fazla modül aynı işlevi (örn. tarih işleme, HTTP client) farklı kütüphanelerle çözmeye çalışırsa, bu [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 2. kategorisinde (isimlendirme/tutarlılık) bir bulgu olarak işaretlenir — proje genelinde her iş için **tek** kütüphane kullanılır, bu tercih ilk kullanıldığı yerde sabitlenir.

## Zorunlu merkezi sürüm yönetimi (BOM)

**Hiçbir modül kendi `build.gradle`/`pom.xml` dosyasında bağımsız bir kütüphane sürümü tanımlayamaz.** Tüm üçüncü taraf kütüphane sürümleri, root projedeki tek bir merkezi noktada (Gradle'da `platform`/version catalog, Maven'da `dependencyManagement`/BOM) tanımlanır; modüller sadece kütüphaneyi **isim olarak** kullanır, sürüm numarası yazmaz.

**Neden bu kadar katı:** [architecture-constraints.md](architecture-constraints.md)'deki modül izolasyonu (`modules/fitness`, `modules/berber` birbirini bilmez) bağımlılık sürümlerinde **tersine** işler — hepsi aynı JVM'de, aynı çalıştırılabilir uygulamada (`apps/api`) birleşiyor. Bir modül Jackson'ı `2.15`, başka bir modül `2.17` isterse, ikisi de aynı JAR'da birlikte çalışamaz (jar hell / `ClassNotFoundException` riski) — modüller kod olarak izole ama **runtime'da tek bir process**, bu yüzden bağımlılık sürümleri modüller arası izolasyondan **muaf**, merkezi ve tektir.

- Yeni bir kütüphane ihtiyacı olan bir agent, önce merkezi kataloğa bakar — zaten varsa onu kullanır, yoksa (insan onaylı, bkz. yukarıki kural) merkezi kataloğa **tek bir yerden** eklenir, kullanan modül kendi sürümünü belirlemez.
- Bu, [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 1. kategorisinde (sınır ihlali) denetlenir — bir modülün kendi sürüm numarası tanımladığı görülürse PR otomatik reddedilir.

## İlgili dosyalar

- [tech-stack.md](tech-stack.md) — çekirdek framework/araç kararları (bu dosyanın kapsamadığı, üçüncü taraf yardımcı kütüphaneler için)
- [../workflow/code-review-checklist.md](../workflow/code-review-checklist.md)
