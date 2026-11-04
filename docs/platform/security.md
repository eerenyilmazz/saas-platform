# Security — Rate Limiting, Token Strategy, Input Validation

The API contract was defined in [api-conventions.md](api-conventions.md) but three concrete topics on the security side were missing: abuse protection, token lifetime, input validation.

## 1. Rate limiting

Two endpoint groups in particular carry risk:

- **Login/auth endpoints** — open to brute-force password attempts. Rule: after a certain number (suggestion: 5) of failed attempts per minute from the same IP/to the same account, a short lockout (suggestion: 15 minutes).
- **The QR check-in endpoint** (`/api/v1/fitness/checkins`) — because it is a near-public endpoint, it is open to spam/DoS. A request limit per tenant and per IP is applied.

Implementation: a rate-limiting library on the Spring Boot side (e.g. Bucket4j) or limiting at the reverse proxy level (Nginx/Cloudflare) — which one is chosen depends on the hosting decision (see [open-questions.md](../open-questions.md) item 8).

## 2. Token strategy (JWT)

[api-conventions.md](api-conventions.md) said "Bearer JWT" but the lifetime/refresh had not been defined:

- The **access token** lifetime is short (suggestion: 15-30 minutes) — so that even if stolen the damage window is small.
- The **refresh token** lifetime is longer (suggestion: 7-30 days), used only at the token refresh endpoint, and does not go with normal API requests.
- The refresh token must be revocable on logout or password change (kept in the DB or in a blacklist).
- On the mobile (Capacitor) side the refresh token is kept in secure storage (Keychain/Keystore), and on the web side an `httpOnly` cookie is preferred (keeping it in localStorage exposes it to XSS risk).

## 3. Input validation

- All API inputs (especially free-text fields coming from the user) are validated on the server side — frontend validation is not enough, the security boundary is always in the backend.
- In Spring Boot this is applied in a standard way with Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.), and each module defines this in its own DTOs.
- The SQL injection risk is low as long as JPA/parameterized queries are used, but parameterized queries are mandatory wherever raw SQL is written (if any).

## Out of scope (not in the MVP)

- Web Application Firewall (WAF) — if the hosting provider offers one (e.g. Cloudflare) the free tier can be enabled, no separate investment is needed.
- Two-factor authentication (2FA) — after v1, can be considered especially for the `platform_owner` and `tenant_admin` roles.

## Related files

- [api-conventions.md](api-conventions.md)
- [roles-and-access.md](roles-and-access.md)
- [../modules/fitness.md](../modules/fitness.md) — the QR token's own signature strategy (covered separately)
