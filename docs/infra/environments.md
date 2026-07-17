# Ortamlar & Secrets Yönetimi

## Ortamlar

| Ortam | Amaç | Nerede çalışır |
|---|---|---|
| `local` | Geliştirme | Geliştiricinin kendi makinesi, `infra/docker` ile db+api+web tek komutla ayağa kalkar |
| `staging` | Test/demo | Gerçek veriye yakın ama üretim olmayan ortam, PR merge sonrası otomatik deploy |
| `production` | Canlı | Gerçek tenant'ların kullandığı ortam, sadece `main`'den deploy edilir |

## Secrets yönetimi

- **Local:** `.env.local` dosyası (gitignore'da), DB şifresi/JWT secret gibi değerler burada, gerçek değerler asla commit edilmez.
- **Staging/Production:** GitHub Actions secrets üzerinden environment variable olarak enjekte edilir — küçük ekip ölçeğinde ayrı bir secrets vault (Vault, AWS Secrets Manager) gerekmiyor, ekip/altyapı büyüdükçe bu karar gözden geçirilir.

## Deploy akışı

```
PR merge → main → staging'e otomatik deploy → manuel onay → production'a deploy
```

Production'a otomatik deploy yapılmaz, staging'de kısa bir doğrulama sonrası manuel tetiklenir.

## Kapsam dışı (MVP'de yok)

- Blue-green veya canary deploy — tek instance ile başlanır, ölçek gerektiğinde eklenir.
- Çoklu bölge (multi-region) altyapı.
