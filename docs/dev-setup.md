# Geliştirici Kurulum Rehberi

Bu rehber, repoyu klonlayan biri (insan ya da coder agent) projeyi yerel makinede nasıl ayağa kaldıracağını gösterir. [tech-stack.md](platform/tech-stack.md)'deki kararlara dayanır — Faz 0 (bkz. [roadmap/phases.md](roadmap/phases.md)) tamamlandığında buradaki komutlar gerçek proje iskeletiyle doğrulanıp güncellenmeli.

## Gereksinimler

| Araç | Sürüm | Neden |
|---|---|---|
| JDK | 21 (LTS) | Spring Boot backend |
| Node.js | 20+ | React frontend |
| Docker + Docker Compose | güncel | Yerel PostgreSQL + servisler |
| Git | güncel | — |

## İlk kurulum

```bash
git clone <repo-url>
cd saas-platform

# Yerel veritabanını ayağa kaldır (PostgreSQL, infra/docker altında tanımlı)
docker compose -f infra/docker/docker-compose.yml up -d

# Backend
cd apps/api
./gradlew bootRun
# → Flyway migration'ları otomatik çalışır, API http://localhost:8080'de ayağa kalkar

# Frontend (ayrı bir terminalde)
cd apps/web
npm install
npm run dev
# → http://localhost:5173'te (Vite varsayılanı) ayağa kalkar
```

## Ortam değişkenleri

`apps/api/.env.local` ve `apps/web/.env.local` dosyaları (gitignore'da) — bkz. [infra/environments.md](infra/environments.md). Örnek şablon `*.env.example` dosyaları repoya eklenir, gerçek değerler asla commit edilmez.

## Testleri çalıştırma

```bash
cd apps/api
./gradlew test              # birim + entegrasyon testleri (Testcontainers gerçek PostgreSQL ayağa kaldırır)
```

Detay: [workflow/testing-strategy.md](workflow/testing-strategy.md)

## Örnek veri (seed/fixture)

Boş bir veritabanıyla geliştirme yapmak zor — Faz 0'da bir `V0__seed_dev_data.sql` (sadece `local` profilinde çalışır, `staging`/`production`'da hiç uygulanmaz) eklenir: örnek bir tenant (fitness modülü açık), birkaç `member`, aktif bir `membership`. Bu, yeni bir geliştirici ya da agent'ın API'yi test etmek için önce manuel kayıt açması gerekmemesini sağlar.

## Yeni bir modül üzerinde çalışırken

Kod yazmadan önce ilgili modülün spec dosyasını oku: `docs/modules/<modül_adı>.md`. Coder agent da aynı kuralı izler — bkz. [workflow/agent-pipeline.md](workflow/agent-pipeline.md).

## Not

Bu dosyadaki komutlar (`./gradlew`, `docker-compose.yml` yolu, port numaraları) Faz 0'da proje iskeleti gerçekten oluşturulduğunda doğrulanmalı — şu an [tech-stack.md](platform/tech-stack.md)'deki kararlara dayanan öngörülen standart komutlardır.
