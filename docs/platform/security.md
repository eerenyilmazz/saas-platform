# Güvenlik — Rate Limiting, Token Stratejisi, Girdi Doğrulama

[api-conventions.md](api-conventions.md)'de API sözleşmesi tanımlandı ama güvenlik tarafındaki üç somut konu eksikti: kötüye kullanım koruması, token ömrü, girdi doğrulama.

## 1. Rate limiting

Özellikle iki endpoint grubu risk taşıyor:

- **Login/auth endpoint'leri** — brute-force şifre denemesine açık. Kural: aynı IP'den/aynı hesaba dakikada belirli sayıda (öneri: 5) başarısız denemeden sonra kısa süreli (öneri: 15 dakika) kilitleme.
- **QR check-in endpoint'i** (`/api/v1/fitness/checkins`) — public'e yakın bir endpoint olduğu için spam/DoS'a açık. Tenant başına ve IP başına istek sınırı uygulanır.

Uygulama: Spring Boot tarafında bir rate-limiting kütüphanesi (örn. Bucket4j) ya da reverse proxy seviyesinde (Nginx/Cloudflare) sınırlama — hangisinin seçileceği hosting kararına bağlı (bkz. [open-questions.md](../open-questions.md) madde 8).

## 2. Token stratejisi (JWT)

[api-conventions.md](api-conventions.md)'de "Bearer JWT" dendi ama ömür/yenileme tanımlanmamıştı:

- **Access token** ömrü kısa (öneri: 15-30 dakika) — çalınsa bile zarar penceresi küçük olsun.
- **Refresh token** ömrü daha uzun (öneri: 7-30 gün), sadece token yenileme endpoint'inde kullanılır, normal API isteklerinde geçmez.
- Refresh token, çıkış yapıldığında ya da şifre değiştirildiğinde geçersiz kılınabilir olmalı (DB'de veya bir blacklist'te tutulur).
- Mobil (Capacitor) tarafında refresh token güvenli depoda (Keychain/Keystore) tutulur, web tarafında `httpOnly` cookie tercih edilir (localStorage'da tutmak XSS riskine açar).

## 3. Girdi doğrulama

- Tüm API girişleri (özellikle kullanıcıdan gelen serbest metin alanlar) sunucu tarafında doğrulanır — frontend doğrulaması yeterli değildir, güvenlik sınırı her zaman backend'dedir.
- Spring Boot'ta bu, Bean Validation (`@Valid`, `@NotNull`, `@Size` vb.) ile standart bir şekilde uygulanır, her modül kendi DTO'sunda bunu tanımlar.
- SQL injection riski JPA/parametrik sorgular kullanıldığı sürece düşük, ancak ham SQL yazılan yerlerde (varsa) parametrik sorgu zorunlu.

## Kapsam dışı (MVP'de yok)

- Web Application Firewall (WAF) — hosting sağlayıcısı sunuyorsa (örn. Cloudflare) ücretsiz katmanı açılabilir, ayrı bir yatırım gerekmez.
- İki faktörlü kimlik doğrulama (2FA) — v1 sonrası, özellikle `platform_owner` ve `tenant_admin` rolleri için değerlendirilebilir.

## İlgili dosyalar

- [api-conventions.md](api-conventions.md)
- [roles-and-access.md](roles-and-access.md)
- [../modules/fitness.md](../modules/fitness.md) — QR token'ın kendi imza stratejisi (ayrıca ele alınıyor)
