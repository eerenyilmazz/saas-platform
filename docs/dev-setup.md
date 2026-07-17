# Geliştirici Kurulum Rehberi

Bu rehber, repoyu klonlayan biri (insan ya da coder agent) projeyi yerel makinede nasıl ayağa kaldıracağını gösterir. Faz 0'da gerçek proje iskeletiyle **doğrulandı** (bkz. [roadmap/phases.md](roadmap/phases.md)).

## Gereksinimler

| Araç | Sürüm | Neden |
|---|---|---|
| JDK | 21 (LTS) | Spring Boot backend |
| Node.js | 20+ | React frontend |
| Docker + Docker Compose | güncel | Yerel PostgreSQL |
| Git | güncel | — |

### Java/Node sürüm çakışması varsa

Bu makinede başka projeler farklı Java/Node sürümü gerektirebilir — global kurulumu bozmadan bu proje için doğru sürümü izole etmek için:

- **Node:** [nvm](https://github.com/nvm-sh/nvm) kullan, repo kökündeki `.nvmrc` (`20`) otomatik doğru sürümü seçer: `nvm use`
- **Java:** `brew install openjdk@21` (keg-only kurulur, global `java` komutuna dokunmaz) — `apps/api/gradle.properties`'deki `org.gradle.java.home` zaten doğru yolu (`/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`) gösteriyor, ek bir PATH ayarına gerek yok, `./gradlew` otomatik doğru JDK'yı kullanır.

## İlk kurulum

```bash
git clone <repo-url>
cd saas-platform

# Yerel veritabanını ayağa kaldır (PostgreSQL, infra/docker altında tanımlı)
docker compose -f infra/docker/docker-compose.yml up -d

# Backend
cd apps/api
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
# → Flyway migration'ları otomatik çalışır, API http://localhost:8080'de ayağa kalkar
# → doğrulama: curl http://localhost:8080/actuator/health  →  {"status":"UP"}

# Frontend (ayrı bir terminalde)
cd apps/web
nvm use   # .nvmrc'deki Node 20'yi seçer
npm install
npm run dev
# → http://localhost:5173'te (Vite varsayılanı) ayağa kalkar
```

> **Port notu:** Yerel PostgreSQL `5433` portunda çalışır (standart `5432` değil) — bu makinede zaten başka bir projeye ait native bir Postgres süreci `5432`'yi kullanıyordu, çakışmayı önlemek için taşındı. Kendi makinende böyle bir çakışma yoksa `infra/docker/docker-compose.yml` ve `apps/api/src/main/resources/application-local.properties`'i `5432`'ye geri alabilirsin, zorunlu değil.

## Ortam değişkenleri

- `SPRING_PROFILES_ACTIVE=local` — `application-local.properties`'i aktif eder (yerel DB bağlantısı)
- `DB_PASSWORD` — opsiyonel, verilmezse `local_dev_only` varsayılanı kullanılır (sadece local profilde, bkz. [infra/environments.md](infra/environments.md))

## Testleri çalıştırma

```bash
cd apps/api
./gradlew test              # birim + entegrasyon testleri (Testcontainers gerçek PostgreSQL ayağa kaldırır)
```

Detay: [workflow/testing-strategy.md](workflow/testing-strategy.md)

## CI

`.github/workflows/ci.yml` — her PR'da backend (`./gradlew build`) ve frontend (`npm run lint` + `npm run build`) ayrı job olarak çalışır. **Not:** GitHub Actions workflow dosyaları GitHub'ın taradığı tek yer olan `.github/workflows/` altında olmak zorunda — `infra/` altında değil (bu, [repo-structure.md](repo-structure.md)'de daha önce yanlış dokümante edilmişti, Faz 0'da düzeltildi).

## Örnek veri (seed/fixture)

Henüz eklenmedi — Faz 1'de core (auth/tenant/membership) tabloları oluşunca, sadece `local` profilinde çalışan bir seed migration'ı eklenecek. Şu an veritabanı boş, sadece Flyway'in kendi şema geçmişi tablosu var.

## Yeni bir modül üzerinde çalışırken

Kod yazmadan önce ilgili modülün spec dosyasını oku: `docs/modules/<modül_adı>.md`. Coder agent da aynı kuralı izler — bkz. [workflow/agent-pipeline.md](workflow/agent-pipeline.md).
