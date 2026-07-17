# Performans/Yük Testi

[testing-strategy.md](testing-strategy.md)'deki dört seviye (birim/entegrasyon/tenant izolasyon/e2e) hep **doğruluğu** test ediyordu — "doğru sonucu veriyor mu". Bu doküman farklı bir soruyu cevaplar: **"kaç eşzamanlı tenant/istek kaldırıyor"**.

## MVP kararı: erken yatırım yapılmaz, tek bir eşik testi yeterli

Gerçek kullanım verisi (kaç tenant, ne kadar trafik) olmadan kapsamlı bir yük testi altyapısı kurmak spekülatif olur — [modules/_template.md](../modules/_template.md)'deki erken soyutlama uyarısıyla aynı gerekçe. Bunun yerine:

- **Faz 1 sonunda tek bir temel yük testi** çalıştırılır (öneri: k6 ya da Gatling gibi hafif bir araç, ayrı bir altyapı gerekmez) — hedef: "50 eşzamanlı tenant, saniyede 10 istek" gibi kaba bir referans yükü sorunsuz karşılıyor mu.
- Bu test **CI'da her PR'da çalışmaz** (yavaş ve maliyetli) — sadece büyük mimari değişikliklerden sonra (örn. yeni bir cache katmanı, [caching-strategy.md](../platform/caching-strategy.md)) manuel tetiklenir.

## Neyin izleneceği (öncelik sırası)

1. `tenant_id` bazlı sorgu gecikmesi — [database-conventions.md](../platform/database-conventions.md)'deki zorunlu index'ler gerçekten işe yarıyor mu, tenant sayısı arttıkça sorgu süresi lineer mi büyüyor
2. RLS'in performans etkisi — [tenant-isolation-enforcement.md](../platform/tenant-isolation-enforcement.md)'deki policy kontrolü her sorguya ek yük bindirir, bu yükün kabul edilebilir olduğu doğrulanır
3. QR check-in endpoint'i — fitness modülünün en yoğun uç noktası, [concurrency-and-idempotency.md](../platform/concurrency-and-idempotency.md)'deki idempotency kontrolünün darboğaz yaratmadığı kontrol edilir

## Ne zaman ciddiye alınır

Gerçek tenant sayısı arttıkça (bkz. [onboarding.md](../platform/onboarding.md)'deki ~10 tenant eşiği) bu testler düzenli hale getirilir, somut eşik değerleri ([open-questions.md](../open-questions.md) madde 9'daki ToS/SLA netleşince) tanımlanır. MVP'de sayısal bir SLA hedefi (örn. "p99 200ms") konmuyor — gerçek trafik olmadan bu rakam anlamsız olurdu.

## İlgili dosyalar

- [testing-strategy.md](testing-strategy.md) — doğruluk testleri (bu dosyanın kapsamadığı)
- [../infra/observability.md](../infra/observability.md) — production'da gerçek performansın izlenmesi
