# Tenant Offboarding — Ayrılma ve Veri Dışa Aktarma

[data-import.md](data-import.md) bir tenant'ın **nasıl içeri** gireceğini anlatıyordu — bu doküman tersini tanımlar: bir tenant abonelik iptal ettiğinde/ayrıldığında ne olur.

## Neden gerekli

Bu simetrik bir boşluktu: onboarding var, offboarding yoktu. Ayrıca bir gereklilik — KVKK'nın veri taşınabilirliği hakkı, tenant'ın kendi verisini (üye listesi, geçmiş kayıtlar) alıp gidebilmesini gerektirir; sessizce silmek ya da süresiz tutmak ikisi de sorunlu.

## Akış

```
1) Tenant/platform_owner abonelik iptalini başlatır
        ↓
2) Grace period (öneri: 30 gün) — bu sürede tenant hâlâ giriş yapabilir, verisini görebilir
        ↓
3) Export — tenant kendi verisini CSV olarak indirebilir (data-import.md'nin ters yönü:
   member, membership, tenant_payment, checkin_log/appointment — kendi tenant_id'sine ait her şey)
        ↓
4) Grace period bitince → erişim kesilir (user hesapları deaktif edilir)
        ↓
5) Veri saklama süresi (öneri: 90 gün, yasal zorunluluklar hariç) → sonra anonimleştirme/silme
```

## MVP kararı: manuel, platform_owner destekli

[onboarding.md](onboarding.md)'deki aynı mantık: self-servis bir "hesabımı kapat" akışı MVP'de yok. Tenant iptal isteğini sana iletir, sen grace period'u ve export'u [data-import.md](data-import.md)'deki import script'inin tersi bir export script'iyle manuel yürütürsün.

## Veri modeli

- `tenant.status` alanına yeni bir değer: `cancelled` (bkz. [tenant-model.md](tenant-model.md)) — `active` değilse yeni giriş/işlem yapılamaz ama grace period boyunca veri hâlâ okunabilir.
- Silme, [database-conventions.md](database-conventions.md)'deki soft-delete kuralına tabi — `deleted_at` set edilir, gerçek `DELETE` çalışmaz (mali kayıt zorunluluğu, bkz. [kvkk-checklist.md](kvkk-checklist.md)).

## Platform-billing ile ilişki

İptal, [billing-model.md](billing-model.md)'deki `platform_subscription.status`'unu `cancelled` yapar — bu senin gelirinin durması demektir, tenant'ın kendi verisiyle (tenant-billing) karıştırılmaz.

## Kapsam dışı (MVP'de yok)

- Self-servis "hesabımı kapat" butonu — platform admin paneliyle (bkz. [roadmap/phases.md](../roadmap/phases.md) Faz 6) birlikte değerlendirilir.
- Otomatik export formatı zenginleştirme (sadece CSV, başka format yok).

## İlgili dosyalar

- [data-import.md](data-import.md) — bu dosyanın simetriği
- [kvkk-checklist.md](kvkk-checklist.md) — veri taşınabilirliği/silme hakkı
- [billing-model.md](billing-model.md) — platform_subscription iptali
