# ADR 0005: Capacitor ile mobil sarmalama

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

Üye tarafı (fitness QR giriş, ileride berber randevu) için native app mi, PWA mı, yoksa web app'i native kabuğa sarmak mı (hybrid) sorusu vardı. Admin panel zaten web olarak karara bağlanmıştı.

## Karar

Tek web app kod tabanı (React) yazılır; admin paneli doğrudan web'de kalır, üye tarafı Capacitor ile iOS + Android'e sarılır.

## Sonuç

- Tek kod tabanından hem web hem iki mobil platforma çıkılır, ayrı native geliştirme gerekmez.
- App Store/Play Store'da yayınlanabilir olmak, PWA'ya göre daha güçlü keşfedilebilirlik ve güven sağlıyor; push notification desteği (özellikle iOS'ta) PWA'dan daha güvenilir.
- Apple'ın "çıplak WebView" reddi riskine karşı native splash screen, native push gibi birkaç Capacitor plugin'i kullanılacak (bkz. [roadmap/phases.md](../roadmap/phases.md) Faz 5).
- iOS derleme/test için Mac gerekiyor — bu engel değil, mevcut donanımda hem Mac hem Windows var.
