# Karar Ağacı — Bu Yeni Şey Nereye Ait?

Yeni bir ihtiyaç geldiğinde ("randevu hatırlatma mesajına özel bir seçenek eklensin", "yeni bir sektör desteklensin" gibi) aynı problem için birden fazla makul çözüm var olabilir — bir agent bunlardan rastgele birini seçerse, aynı tür ihtiyaçlar zamanla tutarsız şekillerde çözülür. Bu karar ağacı, sırayla sorulacak sorularla doğru yeri tek bir cevaba indirger.

## Karar akışı

```
Yeni ihtiyaç geldi
        │
        ▼
1) Bu, TÜM tenant'larda/modüllerde aynı şekilde mi çalışacak?
        │
   ┌────┴────┐
  EVET      HAYIR
   │          │
   ▼          ▼
"core'a     2) Bu, yepyeni bir sektöre mi (dikey) ait,
 ekle"          yoksa var olan bir modülün bir parçası mı?
   │                  │
   │           ┌──────┴──────┐
   │         YENİ SEKTÖR   VAR OLAN MODÜLÜN PARÇASI
   │            │                  │
   │            ▼                  ▼
   │      "new-module-       3) Bu, tenant'tan tenant'a
   │       checklist.md'yi      açılıp kapanabilmeli mi
   │       izleyerek yeni       (bazı tenant ister,
   │       modül aç"            bazısı istemez)?
   │                                  │
   │                          ┌───────┴───────┐
   │                        EVET             HAYIR
   │                          │                │
   │                          ▼                ▼
   │                  "feature flag,      "modülün kendi
   │                   tenant.settings     sabit davranışı,
   │                   altına ekle"        config'e gerek yok"
```

## Ayrım kriterleri (tablo formunda)

| Soru | Cevap → Yer |
|---|---|
| Auth, tenant izolasyonu, üyelik, ödeme, bildirim gibi **her sektörde aynı** bir ihtiyaç mı? | `core` |
| Belirli bir sektöre özgü, o sektörün iş kuralı mı? | İlgili `modules/<ad>` |
| Bambaşka bir sektör mü (örn. inşaat, restoran)? | Yeni modül — [new-module-checklist.md](../workflow/new-module-checklist.md) |
| Aynı modül içinde, bazı tenant'lar açık bazıları kapalı isteyebilir mi? | Feature flag (`tenant.settings.modules.<ad>.<özellik>`) — bkz. [tenant-model.md](tenant-model.md) |
| Tüm tenant'larda sabit ama ortam/deploy'a göre değişebilir mi (örn. SMS sağlayıcı API anahtarı)? | Config/environment variable — bkz. [environments.md](../infra/environments.md) |
| Senin (platform_owner) kendi işletmen için mi, tenant'lar için mi? | `platform` katmanı |

## Belirsiz kalan durumlar

Yukarıdaki ağaç iki farklı dala eşit olasılıkla düşen bir durum üretirse (örn. "hem yeni sektöre hem var olan modüle benziyor"), bu bir [ADR](../adr/_template.md) konusu olur — karar tek bir agent'ın kendi başına vereceği bir şey değil, gerekçesiyle kayda geçirilir.

## İlgili dosyalar

- [tenant-model.md](tenant-model.md) — feature flag mekanizması
- [../workflow/new-module-checklist.md](../workflow/new-module-checklist.md)
- [../adr/_template.md](../adr/_template.md)
