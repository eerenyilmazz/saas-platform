# Repo Yapısı

Tek monorepo, klasör bazlı modül ayrımı. Her modül `core`'a bağımlı, birbirine bağımlı değil — biri bozulursa diğeri etkilenmez, yeni modül eklemek mevcut kodu değiştirmeden yeni bir klasör açmak demek.

```
saas-platform/
├── .github/
│   └── workflows/             # GERÇEK CI/CD tanımları — GitHub sadece bu klasörü tarar
├── apps/
│   ├── api/                   # Spring Boot — tek Gradle projesi, tüm modülleri birleştiren çalıştırılabilir uygulama
│   │   └── src/main/java/com/saas/
│   │       ├── platform/      # SaaS sahibinin kendi işletim katmanı — tenant provizyonlama, platform faturalama
│   │       ├── core/          # her tenant'ın kullandığı ortak katman: auth, tenant, üyelik motoru, tenant-billing, bildirim
│   │       └── modules/
│   │           ├── berber/    # randevu tipi, hizmet süresi, iş kuralları
│   │           └── fitness/   # QR giriş/çıkış, PT randevu, dondurma
│   └── web/                   # React — admin panel + üye arayüzü (Capacitor ile sarılır)
├── infra/
│   ├── docker/                # yerel geliştirme ortamı (db + api + web tek komutla ayağa kalkar)
│   └── environments/          # staging/production ortam tanımları
└── docs/
    └── adr/                   # mimari karar kayıtları (Architecture Decision Records)
```

> **Faz 0'da netleşen karar:** `platform`/`core`/`modules` ayrı Gradle projeleri (fiziksel modüller) değil, **tek bir Gradle projesi** (`apps/api`) içindeki Java paketleridir. Fiziksel modül izolasyonu yerine [architecture-constraints.md](platform/architecture-constraints.md)'deki ArchUnit test suite'i sınır ihlallerini derleme sonrası test aşamasında yakalar — bu, modüler monolit felsefesine (tek deploy birimi, düşük operasyon yükü) daha uygun. Paket adları [package-naming.md](package-naming.md)'deki `com.saas.*` şemasıyla birebir eşleşir.

## Neden bu ayrım

- `apps/` çalıştırılabilir uygulamaları taşır, `apps/api` içindeki Java paketleri mantığı ayırır.
- `com.saas.platform` ile `com.saas.core` birbirine karıştırılmaz: **platform** = sen (SaaS sahibi) tenant'ları yönetirken kullanırsın, **core** = her tenant kendi işletmesini yönetirken kullanır. Detay: [platform/roles-and-access.md](platform/roles-and-access.md)
- `docs/adr/` küçük ama önemli: "neden Spring Boot", "neden monorepo" gibi kararları tarihe not düşer — dolu liste: [adr/](adr/)
- Yeni bir dikey (örn. güzellik salonu) eklemek = `com.saas.modules` altına yeni paket, `core`'u değiştirmeden.

## Bağımlılık kuralı

```
apps/api          → com.saas.platform, com.saas.core, com.saas.modules.*  (hepsi aynı Gradle projesinde)
apps/web          → (tüm API'leri HTTP üzerinden tüketir, doğrudan bağımlılık yok)
com.saas.platform → com.saas.core (izin verilir — platform, core'un üstünde bir yönetim katmanı)
modules/*         → com.saas.core (izin verilir)
modules/*         → modules/* (yasak — modüller birbirine bağımlı olamaz, ArchUnit ile denetlenir)
modules/*         → com.saas.platform (yasak — modüller platform katmanını hiç bilmez)
```
