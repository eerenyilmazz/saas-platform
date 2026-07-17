# Mobil-Backend Uyumluluk Stratejisi

## Problem

Backend her PR merge'de production'a deploy olabilir (bkz. [workflow/branch-strategy.md](../workflow/branch-strategy.md)) ama Capacitor ile store'a çıkan mobil app'in güncellemesi App Store/Play Store review sürecinden geçer — bu **günler** sürebilir. Bu ikisi arasındaki hız farkı yönetilmezse, backend'de yapılan bir değişiklik eski mobil app sürümünü kullanan üyeler için "app çalışmıyor" hatasına yol açabilir.

## Karar

API, **geriye dönük uyumlu** (backward compatible) değişiklik disipliniyle geliştirilir; kırıcı değişiklikler ayrı bir API versiyonu (`/api/v2/...`) olarak açılır, eski versiyon bir süre paralel yaşar.

## Kurallar

- **Kırıcı olmayan değişiklik** (yeni alan ekleme, yeni endpoint ekleme) → doğrudan `v1` içinde yapılır, mobil app'in güncellenmesini beklemez.
- **Kırıcı değişiklik** (bir alanı kaldırma/yeniden adlandırma, bir endpoint'in davranışını değiştirme) → `v2` açılır. `v1`, mevcut mobil app sürümleri store'daki en güncel sürüme yükselene kadar (öneri: minimum 60 gün) yaşamaya devam eder.
- Mobil app, backend'e her istekte kendi sürüm numarasını header olarak gönderir (`X-App-Version`). Backend bu bilgiyi logluyor — hangi sürümlerin hâlâ kullanımda olduğu görülebilir, bir `v1` endpoint'ini ne zaman güvenle kapatabileceğine buradan karar verilir.

## Zorla güncelleme mekanizması

Kritik bir güvenlik açığı ya da uyumsuzluk durumunda mobil app'i zorla güncelletmek gerekebilir:

- Backend, `/api/v1/app-config` gibi bir endpoint'ten "minimum desteklenen sürüm" bilgisini döner.
- Mobil app açılışta bu bilgiyi kontrol eder, kendi sürümü minimumun altındaysa kullanıcıyı güncellemeye yönlendiren bir ekran gösterir (uygulamayı tamamen kilitlemeden önce, önce "güncelleme öneriliyor" uyarısı verir).

## Kapsam dışı (MVP'de yok)

- Otomatik/sessiz güncelleme (native app'lerde bu App Store/Play Store politikaları gereği zaten mümkün değil, kullanıcı onayı gerekir).

## İlgili dosyalar

- [api-conventions.md](api-conventions.md) — versiyonlama şeması
- [../adr/0005-capacitor-mobil-sarmalama.md](../adr/0005-capacitor-mobil-sarmalama.md)
- [../roadmap/phases.md](../roadmap/phases.md) — Faz 5
