# Veri Modeli — Genel Bakış

> Bu dosya bir **kaynak değil, türetilmiş referanstır**. Her tablonun asıl tanımı kendi modülünün spec dosyasındadır; burada sadece tüm sistemi tek sayfada görmek için toplanmıştır. Bir modül dosyası güncellenirse bu sayfa da güncellenmeli — çelişki olursa modül dosyası geçerlidir.

## Modül yetenek matrisi (Feature Capability Matrix)

Bir agent "fitness'te randevu var mı, berber'de QR var mı" gibi bir soruyu 10 dosyayı okuyarak değil, buradan cevaplar:

| Modül | Randevu | QR/Check-in | Üyelik (`membership`) | Ödeme (`tenant_payment`) | Bildirim |
|---|---|---|---|---|---|
| **core** | ❌ | ❌ | ✅ (motor burada) | ✅ (motor burada) | ✅ (motor burada) |
| **fitness** | ✅ (`training_appointment`) | ✅ (`qr_token`, `checkin_log`) | ✅ kullanır | ✅ kullanır | ✅ kullanır |
| **berber** | ✅ (`appointment`) | ❌ | ✅ kullanır (opsiyonel, sadakat kartı) | ✅ kullanır | ✅ kullanır |

Yeni bir modül eklendiğinde ([new-module-checklist.md](workflow/new-module-checklist.md)) bu tabloya yeni bir satır eklenir — ✅/❌ dışında bir modülün kendine özgü tek bir yeteneği varsa (örn. ileride "stok") yeni bir sütun açılır.

## Tüm tablolar, sahibine göre

### core (`packages/core`) — bkz. [modules/core.md](modules/core.md)

| Tablo | Alanlar | Not |
|---|---|---|
| `tenant` | id, name, settings (JSON) | Feature flag'ler `settings` içinde |
| `user` | id, tenant_id, role, login bilgisi | Sadece işletme içi kişiler (bkz. [glossary.md](glossary.md)) |
| `member` | id, tenant_id, name, phone, consent_at, user_id (nullable) | İşletmenin kendi müşterisi |
| `membership_plan` | id, tenant_id, name, duration, entry_allowance | |
| `membership` | id, tenant_id, member_id, plan_id, start_at, end_at, status | |
| `tenant_payment` | id, tenant_id, source_type, source_id, amount, method, paid_at | `source_type` her modülün kendi domain nesnesine genişleyebilir |

### platform (`packages/platform`) — bkz. [platform/](platform/)

| Tablo | Alanlar | Not |
|---|---|---|
| `platform_subscription` | id, tenant_id, plan, monthly_fee, status, next_payment_at | Senin gelirin — bkz. [platform/billing-model.md](platform/billing-model.md) |
| `audit_log` | id, tenant_id, table_name, record_id, action, old_value, new_value, actor_user_id, actor_role, occurred_at | bkz. [platform/audit-log.md](platform/audit-log.md) |
| `support_session` | id, tenant_id, platform_owner_user_id, reason, started_at, ended_at, status | bkz. [platform/support-access.md](platform/support-access.md) |

### fitness (`packages/modules/fitness`) — bkz. [modules/fitness.md](modules/fitness.md)

| Tablo | Alanlar | Not |
|---|---|---|
| `checkin_log` | id, tenant_id, member_id, membership_id, checked_in_at, checked_out_at | |
| `qr_token` | member_id, token, issued_at, expires_at | |
| `training_appointment` | id, tenant_id, trainer_id (→user), member_id, scheduled_at | |

### berber (`packages/modules/berber`) — bkz. [modules/berber.md](modules/berber.md)

| Tablo | Alanlar | Not |
|---|---|---|
| `service` | id, tenant_id, name, duration, price | |
| `appointment` | id, tenant_id, staff_id (→user), member_id, service_id, start_at, end_at, status | |
| `staff_availability` | staff_id, day_of_week, start_time, end_time | |

## İlişki haritası (metinsel)

```
tenant
 ├─ user (tenant_admin / tenant_staff)
 ├─ member ──user (nullable, sadece giriş yapabilen üyelerde)
 │    ├─ membership ──membership_plan
 │    ├─ checkin_log, qr_token, training_appointment   (fitness)
 │    └─ appointment                                    (berber)
 ├─ tenant_payment ──(source_type/source_id ile membership | appointment | order...)
 ├─ audit_log
 └─ support_session ──platform_owner (user, tenant dışı)

platform_subscription ──tenant   (platform katmanı, tenant'ın "gördüğü" değil "hakkında" bir tablo)
```

## Yeni bir modül eklerken bu sayfayı güncelleme

[workflow/new-module-checklist.md](workflow/new-module-checklist.md)'e bir adım daha eklendi: yeni modülün veri modeli burada da (kendi bölümü altında) listelenmeli — bkz. checklist'in son hali.

## İlgili dosyalar

- [glossary.md](glossary.md) — tablo/alan isimlerinin tanımı
- [workflow/new-module-checklist.md](workflow/new-module-checklist.md)
