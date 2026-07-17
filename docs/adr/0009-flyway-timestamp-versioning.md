# ADR 0009: Flyway migration'ları sıralı numara değil, zaman damgası ile versiyonlanır

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

[database-conventions.md](../platform/database-conventions.md) başlangıçta klasik Flyway kuralını (`V1`, `V2`, `V3`... sıralı numara) benimsemişti. Bu, tek bir geliştiricinin sırayla çalıştığı bir ortamda sorunsuzdur — ama bu platform [agent-pipeline.md](../workflow/agent-pipeline.md) gereği **paralel çalışan birden fazla coder agent** ile geliştiriliyor. İki agent, farklı branch'lerde aynı anda (biri berber modülü, diğeri fitness modülü için) migration yazarsa, ikisi de kendi branch'inde "bir sonraki boş numara"yı görür ve aynı numarayı (örn. ikisi de `V7`) kullanır.

## Karar

Migration dosya adları sıralı numara yerine **zaman damgası** (`V<yyyyAAggSSDD>__<açıklama>.sql`, dakika hassasiyetinde) kullanır.

## Sonuç

- İki agent aynı anda migration yazsa bile, farklı zaman damgaları alacakları neredeyse kesindir — versiyon numarası çakışması yapısal olarak ortadan kalkar.
- Flyway bu deseni (community edition dahil) doğrudan destekler, ek bir araç gerekmez.
- Dezavantaj: dosya adları `V1`, `V2` kadar okunaklı değil — ama migration'ların **sırası** zaten dosya içeriğinden/git geçmişinden takip edilir, dosya adının "okunaklı sayı" olması bir gereklilik değil.

## Geçmiş bağlam

- **Orijinal olay/bulgu:** Bu karar, henüz hiçbir migration yazılmadan, ikinci bir dış mimari incelemesinde (Gemini) ortaya çıktı — inceleme, agent-pipeline'ın **paralel** çalışma modelini (aynı anda birden fazla coder agent, farklı branch'lerde) dikkate aldığında sıralı Flyway numaralandırmasının yapısal olarak kırılgan olduğunu gösterdi. Önceki tasarım bunu hiç düşünmemişti çünkü klasik Flyway dokümantasyonu tek geliştiricili senaryoyu varsayar.
- **Reddedilen alternatifler:** Merkezi bir "sıra numarası rezervasyon" servisi (bir agent migration yazmadan önce bir sonraki numarayı merkezi bir yerden alır) — bu, ekstra bir altyapı bileşeni ve tek hata noktası (single point of failure) getirdiği için reddedildi, zaman damgası aynı sonucu sıfır ek altyapıyla veriyor.
- **Başarısızlık senaryosu:** İki agent aynı `V7` numarasını kullanan migration'larla ayrı ayrı PR açıp merge ederse, Flyway CI'da checksum/versiyon çakışması hatasıyla durur — bu, [pr-and-definition-of-done.md](../workflow/pr-and-definition-of-done.md)'daki "Max Iteration = 3" döngüsünü migration numarası kaydırma konusunda anlamsızca tüketebilirdi (agent'lar sıralama çatışmalarını çözmekte zayıftır).
