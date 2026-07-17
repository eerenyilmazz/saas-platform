# API Dokümantasyonu & Frontend Tip Paylaşımı

[api-conventions.md](api-conventions.md) kuralları tanımlıyor ama bu kuralların **gerçek zamanlı** olarak (kod değiştikçe otomatik güncellenen) nasıl belgeleneceği ve frontend'in backend DTO'larıyla nasıl senkron kalacağı tanımlanmamıştı. Bu olmadan frontend agent'ı (ya da geliştirici) backend'deki değişikliği elle takip etmek zorunda kalır — drift kaçınılmaz olur.

## Karar: OpenAPI otomatik üretimi

- Spring Boot, `springdoc-openapi` ile her endpoint'ten otomatik bir OpenAPI (Swagger) şeması üretir — bu şema elle yazılmaz, kod (controller + DTO'lar) değiştikçe otomatik güncellenir.
- Şema, geliştirme ortamında `/api/v1/docs` altında canlı olarak görüntülenebilir (Swagger UI) — hem insan geliştirici hem coder agent, bir endpoint'i kullanmadan önce burayı referans alır.

## Frontend tip senkronizasyonu

- Frontend (React/TypeScript), backend'in OpenAPI şemasından **otomatik TypeScript tip üretimi** yapan bir araç kullanır (öneri: `openapi-typescript`) — CI'da bir adım olarak, backend değiştiğinde frontend'in tip dosyaları da yeniden üretilir.
- Bu sayede backend'de bir DTO alanı değişirse (örn. `name` → `fullName`), frontend derleme anında (compile-time) hata verir — çalışma zamanına kadar fark edilmeyen bir tutarsızlık olmaz.

## Agent workflow ile ilişki

Bir coder agent frontend tarafında çalışırken, backend'in gerçek DTO şemasını (varsayım değil, üretilmiş tip dosyasından) kullanır — bu, [agent-pipeline.md](../workflow/agent-pipeline.md)'deki "coder ilgili modülün spec'ini okur" prensibiyle aynı mantık: agent, tahmin etmez, üretilmiş kaynaktan okur.

## Kapsam dışı (MVP'de yok)

- Harici (üçüncü taraf) API tüketicileri için genel API dokümantasyon portalı — MVP'de API sadece kendi frontend/mobil app'imiz tarafından tüketiliyor, dış geliştiricilere açık değil.

## İlgili dosyalar

- [api-conventions.md](api-conventions.md)
- [../platform/code-style.md](code-style.md) — DTO isimlendirme kuralları (bu şemanın okunabilirliğini doğrudan etkiler)
