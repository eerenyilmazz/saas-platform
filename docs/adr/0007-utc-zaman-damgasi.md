# ADR 0007: Tüm zaman damgaları UTC, görüntüleme anında yerel saate çevrilir

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

Randevu (`appointment`), giriş-çıkış (`checkin_log`), üyelik başlangıç/bitiş gibi birçok tabloda zaman damgası var. Platform şu an sadece Türkiye'de (`Europe/Istanbul`) çalışsa da, sunucu/veritabanının hangi saat diliminde zaman tuttuğu hiç netleşmemişti — bu, yaz saati uygulaması kaldırılmış olsa bile sunucu konumu/Docker container ayarına göre tutarsızlık riski taşır.

## Karar

Veritabanında ve backend'de **tüm zaman damgaları UTC** olarak tutulur (`TIMESTAMP WITH TIME ZONE` / Java `Instant`). Yerel saate (`Europe/Istanbul`) çevirme işlemi **sadece görüntüleme anında**, frontend'de ya da API'nin sunum katmanında yapılır.

## Sonuç

- Sunucunun fiziksel olarak nerede barındırıldığının (bkz. [open-questions.md](../open-questions.md) madde 8, hosting) zaman hesaplamalarına hiçbir etkisi olmaz.
- Bir randevunun "10:00" olarak kaydedilip kaydedilmediği tartışmasız hale gelir — DB'de her zaman UTC karşılığı vardır, "10:00 hangi saat dilimine göre" sorusu hiç sorulmaz.
- API, zaman alanlarını ISO 8601 formatında UTC olarak döner (`2026-07-17T07:00:00Z`), frontend bunu kullanıcının tarayıcı saat dilimine (pratikte `Europe/Istanbul`) göre biçimlendirir.
- Gözden geçirme koşulu: platform farklı saat dilimlerinde tenant'lara açılırsa (ileride), bu karar zaten değişiklik gerektirmez — tasarım baştan buna hazır.
- Bu hazırlığın somut karşılığı: yerel saate çevirme sabit `Europe/Istanbul` değil, `tenant.settings.timezone` alanından okunur (varsayılan `Europe/Istanbul`) — bugün tüm tenant'lar aynı değeri taşır, ama alan zaten tenant bazlı olduğu için farklı ülkede bir tenant açıldığında sadece o tenant'ın kaydında değer değişir, kod hiç değişmez.
