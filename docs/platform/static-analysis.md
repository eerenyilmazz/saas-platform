# Statik Kod Analizi

[code-style.md](code-style.md)'deki "Lint/format" bölümü sadece biçimlendirmeyi (Checkstyle/Spotless/ESLint) kapsıyordu — bu doküman, kodun **biçimi doğru ama mantığı/güvenliği riskli** olabileceği durumları yakalayan ayrı bir katmanı tanımlar. Çok agent'lı geliştirmede bu katman özellikle önemli: bir agent'ın yazdığı kod biçim olarak kusursuz ama karmaşıklık/güvenlik açısından sorunlu olabilir, bunu bir insanın her PR'da elle yakalaması güvenilir değildir.

## İki ayrı statik analiz türü

| Tür | Araç (öneri) | Ne yakalar |
|---|---|---|
| **Kod kalitesi/karmaşıklık** | SonarQube (Community Edition, self-host) ya da SonarCloud | Yüksek cyclomatic complexity, kod tekrarı, ölü kod, potansiyel null pointer |
| **Güvenlik** | CodeQL (GitHub'da ücretsiz) veya Semgrep | SQL injection, hardcoded secret, güvensiz deserialization gibi kalıplar |

Bu, [dependency-management.md](dependency-management.md)'deki Dependabot'tan farklı — Dependabot **üçüncü taraf** kütüphanelerdeki bilinen açıkları tarar, buradaki araçlar **kendi yazdığımız** kodu tarar.

## CI'a entegrasyon

- Her PR'da otomatik çalışır (GitHub Actions'ın bir adımı olarak, bkz. [agent-pipeline.md](../workflow/agent-pipeline.md)).
- Bulgular şiddet derecesine göre ayrılır: **kritik/yüksek** bulgu PR'ı bloklar (reviewer agent'ın [code-review-checklist.md](../workflow/code-review-checklist.md)'deki "sınır ihlali/güvenlik" kategorisiyle aynı ciddiyette ele alınır), **orta/düşük** bulgu yorum olarak bırakılır, blocklamaz.

## Karmaşıklık eşiği (rehber, katı kural değil)

- Bir metod/fonksiyonun cyclomatic complexity'si belirli bir eşiği (öneri: 10) aşarsa SonarQube uyarı verir — bu bir "asla aşılamaz" kuralı değil, "muhtemelen bölünmeli" sinyali. Reviewer (agent ya da insan) bağlama göre karar verir.
- Amaç mekanik bir sayıya uymak değil, agent'ların ürettiği kodun zamanla okunabilir kalmasını sağlamak — özellikle birden fazla agent aynı codebase'e katkı yaptığında, karmaşıklık kontrolsüz büyüyebilir.

## Kapsam dışı (MVP'de yok)

- Otomatik kod kalitesi "skoru" bazlı PR reddi (örn. "coverage %80'in altına düşerse otomatik red") — [testing-strategy.md](../workflow/testing-strategy.md)'de zaten belirtildiği gibi sayısal bir coverage hedefi koymuyoruz, bu tutarlılığı korur.

## İlgili dosyalar

- [code-style.md](code-style.md) — biçimlendirme katmanı (bu dosyanın kapsamadığı)
- [dependency-management.md](dependency-management.md) — üçüncü taraf kütüphane taraması
- [../workflow/code-review-checklist.md](../workflow/code-review-checklist.md)
