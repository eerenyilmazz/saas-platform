# Mevcut Veriden Geçiş (Data Import)

## Neden gerekli

Hedef müşteri profili "Excel + WhatsApp'tan sipariş/randevu alan esnaf" — yani yeni bir tenant açıldığında elinde genelde zaten bir müşteri/üye listesi (Excel, defter, başka bir programın çıktısı) oluyor. [onboarding.md](onboarding.md) hesabın nasıl açıldığını anlatıyor ama "var olan 200 üyeyi nasıl sisteme sokarız" sorusuna cevap vermiyordu.

## MVP kararı: CSV import, tek seferlik, platform_owner destekli

Self-servis bir "Excel yükle" arayüzü MVP'de yok — sebebi [onboarding.md](onboarding.md)'deki aynı mantık: erken aşamada az sayıda tenant için bu otomasyon gereksiz mühendislik yükü.

## MVP akışı

1. Tenant, elindeki Excel/liste'yi sana (platform_owner) iletir.
2. Sen, [onboarding.md](onboarding.md)'deki seed script'in bir uzantısı olarak, veriyi standart bir CSV şablonuna dönüştürüp bir import script'iyle sisteme yüklersin.
3. Import script, her satırı `member` kaydına dönüştürür; üyelik/paket bilgisi varsa `membership` kaydı da açar.
4. KVKK açık rızası — mevcut üyelerin verisi aktarılırken açık rıza yeniden alınmalı mı, yoksa tenant'ın kendi geçmiş rızası yeterli mi, bu [kvkk-checklist.md](kvkk-checklist.md) kapsamında hukuki onayla netleşir (bkz. [open-questions.md](../open-questions.md) madde 5).

## CSV şablonu (taslak)

Tenant'ın dolduracağı CSV **kullanıcıya gösterilen bir dosya** olduğu için başlıkları Türkçe kalır (bkz. [localization.md](localization.md) — kullanıcıya dönük içerik ile kod farklı dildedir):

```
ad, telefon, e-posta (opsiyonel), üyelik_başlangıç, üyelik_bitiş, paket_adı
```

- Import script bu Türkçe başlıkları okurken **İngilizce DB alanlarına eşler** (`ad`→`name`, `telefon`→`phone`, `üyelik_başlangıç`→`membership.start_at` vb.) — kod içindeki değişken/alan adı yine [code-style.md](code-style.md) kuralına göre İngilizce'dir, sadece CSV'nin kendisi (kullanıcı girdisi) Türkçe.
- Şablon [modules/_template.md](../modules/_template.md)'deki gibi her modülde küçük farklılaşabilir (örn. berber için `son_ziyaret_tarihi` eklenebilir, DB'de `last_visit_at`'a eşlenir) ama temel alanlar (`ad`/`name`, `telefon`/`phone`) tüm modüllerde ortak.

## Veri kalitesi kuralı

- Import sırasında telefon numarası formatı normalize edilir (05XX XXX XX XX gibi tutarlı bir formata).
- Mükerrer kayıt kontrolü (aynı telefon numarasıyla iki kez import) — script bunu tespit edip atlar, sessizce tekrar oluşturmaz.
- Import sonucu bir özet raporu döner: kaç kayıt eklendi, kaç kayıt hatalı/atlandı (ve neden).

## Ne zaman self-servis'e geçilir

[onboarding.md](onboarding.md)'deki aynı eşik (~10 tenant) burada da geçerli — tenant sayısı arttıkça platform admin panelinde (bkz. [roadmap/phases.md](../roadmap/phases.md) Faz 6) bir "CSV yükle" ekranı eklenir, tenant_admin kendi verisini kendisi yükler.

## Kapsam dışı (MVP'de yok)

- Başka bir yazılımdan (örn. belirli bir muhasebe programından) doğrudan API entegrasyonuyla veri çekme — sadece CSV/manuel aktarım.
- Otomatik veri temizleme/eşleştirme (fuzzy matching) — mükerrer kontrolü basit (telefon numarası birebir eşleşmesi) tutulur.

## İlgili dosyalar

- [onboarding.md](onboarding.md)
- [kvkk-checklist.md](kvkk-checklist.md)
- [../modules/_template.md](../modules/_template.md)
- [tenant-offboarding.md](tenant-offboarding.md) — bu dosyanın simetriği (veri dışa aktarma)
