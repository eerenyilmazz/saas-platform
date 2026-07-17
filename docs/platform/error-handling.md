# Hata Yönetimi (Exception Stratejisi)

[api-conventions.md](api-conventions.md) hatanın **tel üzerindeki** (wire) formatını tanımlıyor (`error.code`, `error.message`, `error.field`) ama kod içinde bir hatanın nasıl fırlatılıp bu formata dönüştüğü tanımlanmamıştı — her modül kendi tarzında exception fırlatırsa format tutarlılığı garanti edilemez.

## Exception hiyerarşisi

```
DomainException (abstract, core'da tanımlı)
 ├─ NotFoundException           → HTTP 404
 ├─ ValidationException         → HTTP 400 (field doluysa)
 ├─ UnauthorizedException       → HTTP 401
 ├─ ForbiddenException          → HTTP 403 (örn. tenant izolasyon ihlali denemesi)
 ├─ ConflictException           → HTTP 409 (örn. randevu çakışması)
 └─ BusinessRuleException       → HTTP 422 (örn. üyelik süresi dolmuş → MEMBERSHIP_EXPIRED)
```

- Tüm modüller **sadece** bu hiyerarşideki tipleri fırlatır, kendi özel exception tipini tanımlamaz — yeni bir hata durumu için mevcut tiplerden biri kullanılır, `code` alanıyla ayrıştırılır (örn. `BusinessRuleException("MEMBERSHIP_EXPIRED", "Üyelik süresi dolmuş.")`).
- `code` değerleri modül prefix'i taşır (`FITNESS_QR_EXPIRED`, `BERBER_SLOT_TAKEN`) — [glossary.md](../glossary.md)'deki terimlerle tutarlı isimlendirilir.

## Merkezi dönüştürme

`core`'da tek bir `@ControllerAdvice` (global exception handler) bu hiyerarşideki her tipi yakalar, [api-conventions.md](api-conventions.md)'deki JSON zarfına çevirir. Hiçbir controller kendi try/catch'iyle hata formatlamaz — bu, her endpoint'in aynı formatta hata döndürmesini merkezi olarak garanti eder.

## Beklenmeyen hatalar

Yukarıdaki hiyerarşiye girmeyen (öngörülmemiş) her exception, `@ControllerAdvice` tarafından `INTERNAL_ERROR` koduyla HTTP 500'e çevrilir ve otomatik olarak [observability.md](../infra/observability.md)'deki hata izleme sistemine (Sentry) raporlanır — kullanıcıya asla stack trace veya iç detay sızdırılmaz.

## İlgili dosyalar

- [api-conventions.md](api-conventions.md) — wire formatı
- [../infra/observability.md](../infra/observability.md) — beklenmeyen hataların izlenmesi
- [code-review-checklist.md](../workflow/code-review-checklist.md) — review sırasında bu hiyerarşiye uyulup uyulmadığının kontrolü
