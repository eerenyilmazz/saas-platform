# Repo Yapısı

Tek monorepo, klasör bazlı modül ayrımı. Her modül `core`'a bağımlı, birbirine bağımlı değil — biri bozulursa diğeri etkilenmez, yeni modül eklemek mevcut kodu değiştirmeden yeni bir klasör açmak demek.

```
saas-platform/
├── apps/
│   ├── api/                  # Spring Boot — tüm modülleri birleştiren çalıştırılabilir uygulama
│   └── web/                  # React — admin panel + üye arayüzü (Capacitor ile sarılır)
├── packages/
│   ├── platform/              # SaaS sahibinin kendi işletim katmanı — tenant provizyonlama, platform faturalama
│   ├── core/                  # her tenant'ın kullandığı ortak katman: auth, tenant, üyelik motoru, tenant-billing, bildirim
│   └── modules/
│       ├── berber/            # randevu tipi, hizmet süresi, iş kuralları
│       └── fitness/           # QR giriş/çıkış, PT randevu, dondurma
├── infra/
│   ├── github-actions/        # CI/CD ve agent pipeline tanımları
│   ├── docker/                # yerel geliştirme ortamı (db + api + web tek komutla ayağa kalkar)
│   └── environments/          # staging/production ortam tanımları
└── docs/
    └── adr/                   # mimari karar kayıtları (Architecture Decision Records)
```

## Neden bu ayrım

- `apps/` çalıştırılabilir uygulamaları taşır, `packages/` onları besleyen mantığı.
- `packages/platform/` ile `packages/core/` birbirine karıştırılmaz: **platform** = sen (SaaS sahibi) tenant'ları yönetirken kullanırsın, **core** = her tenant kendi işletmesini yönetirken kullanır. Detay: [platform/roles-and-access.md](platform/roles-and-access.md)
- `docs/adr/` küçük ama önemli: "neden Spring Boot", "neden monorepo" gibi kararları tarihe not düşer — dolu liste: [adr/](adr/)
- Yeni bir dikey (örn. güzellik salonu) eklemek = `packages/modules/` altına yeni klasör, `core`'u değiştirmeden.

## Bağımlılık kuralı

```
apps/api        → packages/platform, packages/core, packages/modules/*
apps/web        → (tüm API'leri HTTP üzerinden tüketir, doğrudan bağımlılık yok)
packages/platform → packages/core (izin verilir — platform, core'un üstünde bir yönetim katmanı)
modules/*       → packages/core (izin verilir)
modules/*       → modules/* (yasak — modüller birbirine bağımlı olamaz)
modules/*       → packages/platform (yasak — modüller platform katmanını hiç bilmez)
```
