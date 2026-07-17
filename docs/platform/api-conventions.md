# API Sözleşmesi

Tüm modüller aynı REST kurallarını izler — aksi halde her modül kendi tarzında yazar, frontend ve agent'lar için tutarsızlık çıkar.

## URL yapısı

```
/api/v1/<katman>/<kaynak>
```

- Platform katmanı: `/api/v1/platform/tenants`, `/api/v1/platform/subscriptions`
- Core: `/api/v1/members`, `/api/v1/memberships`, `/api/v1/auth`
- Modül: `/api/v1/fitness/checkins`, `/api/v1/berber/appointments`

Versiyon (`v1`) baştan itibaren zorunlu — ileride kırıcı değişiklik gerekirse `v2` açılır, `v1` bir süre paralel yaşar.

## HTTP metodları

Standart REST: `GET` (okuma), `POST` (oluşturma), `PUT`/`PATCH` (güncelleme), `DELETE` (silme — çoğunlukla soft-delete, kod seviyesindeki `deleted_at` kuralı için bkz. [database-conventions.md](database-conventions.md), KVKK kapsamı için bkz. [kvkk-checklist.md](kvkk-checklist.md)).

## Hata formatı

Tüm hatalar tek bir zarf (envelope) formatında döner — bu formatın kod içinde nasıl üretildiği (exception hiyerarşisi) için bkz. [error-handling.md](error-handling.md):

```json
{
  "error": {
    "code": "MEMBERSHIP_EXPIRED",
    "message": "Üyelik süresi dolmuş.",
    "field": null
  }
}
```

- `code` — makine tarafından okunacak sabit hata kodu (frontend bu koda göre davranış değiştirir, mesaj metnine göre değil)
- `message` — kullanıcıya gösterilecek Türkçe metin
- `field` — validasyon hatasıysa hangi alanla ilgili olduğu, yoksa `null`

## Sayfalama

Liste dönen endpoint'ler `page` ve `size` query parametresi alır, yanıt:

```json
{ "content": [...], "page": 0, "size": 20, "totalElements": 134 }
```

## Kimlik doğrulama

`Authorization: Bearer <JWT>` header'ı zorunlu (auth endpoint'leri hariç). JWT içinde `tenant_id` ve `role` claim'leri taşınır — her istekte bu claim'e göre tenant izolasyonu ve yetki kontrolü yapılır (bkz. [tenant-model.md](tenant-model.md)). Token ömrü/yenileme detayı: [security.md](security.md).

## İlgili dosyalar

- [security.md](security.md) — rate limiting, token ömrü, girdi doğrulama
- [mobile-compatibility.md](mobile-compatibility.md) — kırıcı değişikliklerin mobil app'i nasıl etkilediği, `v1`/`v2` yaşam döngüsü
- [error-handling.md](error-handling.md) — exception hiyerarşisi, hata formatının kod içinde üretilmesi
- [database-conventions.md](database-conventions.md) — migration kuralları, index stratejisi, soft-delete
- [api-documentation.md](api-documentation.md) — bu sözleşmenin OpenAPI/Swagger'a nasıl yansıdığı
