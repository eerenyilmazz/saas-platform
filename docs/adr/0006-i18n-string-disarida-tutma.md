# ADR 0006: Metinlerin baştan dışarıda tutulması (i18n hazırlığı)

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

Platform şu an sadece Türkçe hedefliyor, çoklu dil desteği MVP kapsamında değil. Ama kullanıcıya gösterilen tüm metinler (hata mesajları, buton yazıları, bildirim şablonları) koda doğrudan gömülürse (hardcode), ileride ikinci bir dil eklemek büyük bir retrofit çalışması gerektirir.

## Karar

Kullanıcıya gösterilen tüm metinler baştan itibaren **dışarıda** (resource bundle / i18n dosyası) tutulur, koda gömülmez — dil şu an tek (Türkçe) olsa bile.

## Sonuç

- Backend: hata mesajları (bkz. [platform/api-conventions.md](../platform/api-conventions.md) hata formatındaki `message` alanı) `code`'a göre bir mesaj dosyasından (`messages_tr.properties` gibi) üretilir, Java kodunda literal Türkçe string yazılmaz.
- Frontend: React tarafında metinler bir çeviri dosyasından (örn. `tr.json`) okunur, JSX içine literal metin yazılmaz.
- Bildirim şablonları ([platform/notifications.md](../platform/notifications.md)) da aynı mantıkla ayrı dosyada tutulur.
- Bu, MVP hızını neredeyse hiç yavaşlatmaz (sadece "metni nereye yazıyorum" alışkanlığı değişir) ama ikinci dil eklenmek istendiğinde maliyeti onlarca dosyayı tek tek tarayıp string çıkarmaktan, sadece yeni bir dil dosyası eklemeye indirir.
- Gözden geçirme koşulu yok — bu erken ve ucuz bir disiplin kararı, geri dönüşü de kolay (kullanılmazsa bir maliyeti olmaz).

Somut uygulama detayı (tarih/sayı biçimlendirme, çoğul kalıpları, dil seçici) için bkz. [platform/localization.md](../platform/localization.md).
