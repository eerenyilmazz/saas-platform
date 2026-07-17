# Yasaklı Kod Kalıpları (Anti-Pattern'ler)

[architecture-constraints.md](architecture-constraints.md) mimari sınırları tanımlıyor, bu dosya **sınır ihlali olmayan ama yine de kalitesiz** kod kalıplarını listeler — [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 5. kategorisinde (iş mantığı) reviewer'ın aradığı somut kalıplar.

## Yasaklı kalıplar

| Kalıp | Neden yasak | Doğrusu |
|---|---|---|
| **God Class / Service** | Tek bir sınıf her şeyi yapmaya çalışır, [architecture-constraints.md](architecture-constraints.md)'deki karmaşıklık bütçesini aşar | Sorumluluğa göre böl (örn. `MembershipService` ayrı, `MembershipNotificationService` ayrı) |
| **Repository Explosion** | Her küçük sorgu için ayrı bir repository metodu, aynı sorgunun varyasyonları çoğalır | Ortak sorgu mantığı `Specification`/query builder ile parametrik yazılır |
| **DTO içinde Entity** | API yanıtında doğrudan JPA entity dönülür — iç veri modeli dışa sızar, ileride entity değişikliği API'yi kırar | Her zaman ayrı `Response` DTO'su (bkz. [code-style.md](code-style.md)) |
| **N+1 Query** | Liste + ilişkili veri döngü içinde ayrı sorguyla çekilir | `JOIN FETCH`/`@EntityGraph` (bkz. [database-conventions.md](database-conventions.md)) |
| **Business Logic Controller** | Controller içinde if/else ile karar verme | İş kuralı her zaman `Service` katmanında (bkz. [architecture-constraints.md](architecture-constraints.md)) |
| **Generic Repository** | Tek bir `GenericRepository<T>` ile tüm tablolar aynı kalıpla yönetilmeye çalışılır, modül-özel sorgu ihtiyaçları zorlanır | Her entity kendi `XxxRepository` arayüzüne sahip (Spring Data JPA standardı) |
| **Utils Hell** | Alakasız yardımcı metodların tek bir `Utils`/`Helper` sınıfında birikmesi | Her yardımcı fonksiyon, ilgili olduğu dar bir sınıfa/paket içine konur, "genel" bir çöp kutusu sınıfı olmaz |
| **Silent catch** | `catch (Exception e) {}` — hata yutulur, iz bırakılmaz | Her exception ya [error-handling.md](error-handling.md)'deki hiyerarşiye çevrilir ya da yeniden fırlatılır, asla sessizce yutulmaz |

## Bu liste nasıl büyür

Review sırasında (insan ya da agent) yukarıdaki listede olmayan ama tekrar eden bir kötü kalıp fark edilirse, bu dosyaya yeni bir satır olarak eklenir — liste kapalı değil, [code-review-checklist.md](../workflow/code-review-checklist.md) sürecinin bir çıktısıdır.

## İlgili dosyalar

- [architecture-constraints.md](architecture-constraints.md)
- [database-conventions.md](database-conventions.md) — N+1 detayı
- [error-handling.md](error-handling.md) — silent catch'in doğrusu
