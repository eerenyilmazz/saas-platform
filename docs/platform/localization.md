# Dil Desteği (Localization)

[adr/0006-i18n-string-disarida-tutma.md](../adr/0006-i18n-string-disarida-tutma.md) metinlerin koda gömülmemesi kararını verdi — bu doküman bunun **somut uygulamasını** tanımlar: dil nasıl seçilir, tarih/sayı nasıl biçimlenir, ikinci bir dil eklendiğinde ne değişir.

## MVP: tek dil, çoklu dile hazır mimari

- Kullanıcıya gösterilen bir **dil seçici yok** — sistem sabit `tr-TR` kullanır.
- Ama tüm metinler [adr/0006](../adr/0006-i18n-string-disarida-tutma.md)'daki gibi dışarıda (`tr.json` frontend'de, `messages_tr.properties` backend'de) tutulduğu için, ikinci bir dil eklemek **yeni bir dosya + dil seçici UI eklemek** kadar basit olur, mevcut kodun yeniden yazılmasını gerektirmez.

## Tarih/sayı biçimlendirme — baştan locale-aware

- Tarih, saat, para birimi gösterimi ilk günden **locale-aware** kütüphanelerle yapılır (frontend: `Intl.DateTimeFormat`/`Intl.NumberFormat`, backend: Java `java.time` + `NumberFormat.getInstance(Locale)`) — `"17.07.2026"` gibi elle formatlanmış string asla yazılmaz.
- Neden şimdiden: tek dilde de olsa bu ayrım ücretsiz (kütüphane zaten locale parametresi alıyor), ama sonradan elle yazılmış tarih formatlarını tarayıp değiştirmek pahalı — [adr/0006](../adr/0006-i18n-string-disarida-tutma.md)'daki "ucuza şimdi, pahalıya sonra" ilkesiyle aynı mantık.
- Zaman damgaları için [adr/0007-utc-zaman-damgasi.md](../adr/0007-utc-zaman-damgasi.md)'deki UTC→yerel çevrimi bu locale-aware biçimlendirmeyle aynı katmanda yapılır.

## Çoğul (plural) biçimler

Türkçe'de İngilizce'deki gibi karmaşık çoğul kuralları yok ("1 üye" / "5 üye" — sayı değişse de kelime değişmiyor) ama mesaj şablonları yine de ICU MessageFormat tarzı yazılır (`{count, plural, other {# üye}}`) — bugün gereksiz görünse de, ileride İngilizce gibi çoğul kuralı olan bir dil eklenirse mesaj şablonlarının yeniden yazılmasını değil, sadece yeni bir dil dosyasının eklenmesini gerektirir.

## Kapsam dışı (MVP'de yok)

- Dil seçici UI
- Sağdan-sola (RTL) dil desteği (Arapça vb.) — şu an hedeflenen diller (Türkçe, ileride muhtemelen İngilizce) için gereksiz.
- Otomatik çeviri — herhangi bir ikinci dil, elle/profesyonel çeviriyle eklenir.

## İlgili dosyalar

- [../adr/0006-i18n-string-disarida-tutma.md](../adr/0006-i18n-string-disarida-tutma.md)
- [../adr/0007-utc-zaman-damgasi.md](../adr/0007-utc-zaman-damgasi.md)
- [code-style.md](code-style.md)
