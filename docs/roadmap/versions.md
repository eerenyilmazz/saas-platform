# Sürüm Yol Haritası

Monorepo'da tek uygulama sürümü tutulur (SemVer, `v0.x` yayın öncesi, `v1.0` ilk canlı sürüm). Modüller ayrı ayrı versiyonlanmaz — olgunluk durumu bir etiketle (deneysel / beta / kararlı) takip edilir, her production deploy'da git tag atılır.

| Sürüm | Kapsam | Modül | Durum |
|---|---|---|---|
| v0.1 | Repo iskeleti, core (auth + tenant) | core | planlandı |
| v0.2 | Fitness — QR check-in MVP | fitness | planlandı |
| v0.3 | Admin panel — üye/üyelik yönetimi | core | planlandı |
| v0.4 | Berber — randevu MVP | berber | planlandı |
| v1.0 | İlk canlı sürüm (web, tek/birkaç tenant pilot, manuel onboarding) | core | planlandı |
| v1.1 | Capacitor mobil sarmalama, store çıkışı | fitness | planlandı |
| v1.2 | Platform admin paneli + self-servis onboarding | platform | planlandı |
| v1.3 | Otomatik ödeme entegrasyonu (platform-billing + tenant-billing) | platform | planlandı |
