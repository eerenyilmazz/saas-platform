# Beklemede Olan İşler

Kod tarafında henüz yapılamamış, ama açık bir iş kararına ([open-questions.md](../open-questions.md)) bağlı olduğu için bekleyen maddelerin listesi. Amaç: "bu neden yapılmadı" sorusuna her seferinde yeniden analiz etmeden, tek bakışta cevap verebilmek.

## Faz 0 — altyapı

| Madde | Durum | Bağlı olduğu karar | Detay |
|---|---|---|---|
| Yapılandırılmış loglama (JSON, `tenant_id`/`request_id`) | Başlanmadı | [open-questions.md](../open-questions.md) madde 8 (hosting) | [../infra/observability.md](../infra/observability.md) §1 |
| Hata izleme (Sentry) | Başlanmadı | [open-questions.md](../open-questions.md) madde 8 (hosting) | [../infra/observability.md](../infra/observability.md) §2 |
| Yedekleme job'ı (`pg_dump`/yönetilen servis) | Başlanmadı | [open-questions.md](../open-questions.md) madde 8 (hosting) | [../infra/backup-recovery.md](../infra/backup-recovery.md) |
| Uptime/alerting (dış izleme servisi) | Başlanmadı | [open-questions.md](../open-questions.md) madde 8 (hosting) | [../infra/observability.md](../infra/observability.md) §3 |

## Faz 1 — core

| Madde | Durum | Bağlı olduğu karar | Detay |
|---|---|---|---|
| Feature-flag servis mantığı (`Tenant.settings` oku/yaz) | Devam ediyor | — (bağımlılık yok, hemen bitirilebilir) | [../platform/tenant-model.md](../platform/tenant-model.md) |
| SMS bildirim kanalı (gerçek gönderim) | Soyutlama (`NotificationChannel`) yazılabilir, gerçek gönderim çalışmaz | [open-questions.md](../open-questions.md) madde 3 (SMS sağlayıcı) | [../platform/notifications.md](../platform/notifications.md) |

## Kod dışı, iş kararı bekleyen

| Madde | Bağlı olduğu karar | Etkisi |
|---|---|---|
| Domain/şirket adı | [open-questions.md](../open-questions.md) madde 1 | `com.saas.*` paket adları yer tutucu; netleşmesi gecikirse yüzlerce dosyada tek seferlik toplu rename gerekir |
| Hosting hedefi | [open-questions.md](../open-questions.md) madde 8 | Yukarıdaki dört Faz 0 maddesini + CI/CD'nin deploy adımını bloklar |
| Onboarding modeli onayı | [open-questions.md](../open-questions.md) madde 4 | Seed script'in "manuel onboarding" varsayımının iş planıyla uyuştuğunun teyidi |

## Bu dosya nasıl güncellenir

Bir madde tamamlandığında ya da ilgili [open-questions.md](../open-questions.md) maddesi "çözüldü" olarak işaretlendiğinde bu tablodan silinir. Çelişki olursa `open-questions.md`/[phases.md](phases.md)/ilgili modül spec'i geçerlidir — bu dosya sadece bir takip/özet katmanıdır, otoriter kaynak değildir.

## İlgili dosyalar

- [open-questions.md](../open-questions.md)
- [phases.md](phases.md)
- [../infra/observability.md](../infra/observability.md)
- [../infra/backup-recovery.md](../infra/backup-recovery.md)
- [../platform/notifications.md](../platform/notifications.md)
