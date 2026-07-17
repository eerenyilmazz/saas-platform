# Backup & Felaket Kurtarma

Üyelik ve ödeme verisi tutan bir sistemde veri kaybı kabul edilemez — bu doküman yedekleme ve geri yükleme stratejisini tanımlar.

## Yedekleme

| Ortam | Sıklık | Saklama süresi |
|---|---|---|
| production | Günlük tam yedek + sürekli WAL (write-ahead log) arşivleme | 30 gün |
| staging | Haftalık | 7 gün |
| local | Yedeklenmez (geliştirici ortamı, veri kaybı kritik değil) | — |

- PostgreSQL için yönetilen bir servis kullanılıyorsa (bkz. [open-questions.md](../open-questions.md) — hosting kararı) otomatik yedekleme genelde sağlayıcı tarafından sunulur; kendi sunucunuzda barındırılırsa `pg_dump` + cron ile kurulmalı.
- Yedekler, üretim veritabanından **farklı bir fiziksel konumda/sağlayıcıda** saklanır (aynı sunucu çökerse yedek de gitmesin diye).

## Geri yükleme testi

- Yedeklerin gerçekten geri yüklenebilir olduğu **düzenli olarak** (öneri: ayda bir) staging ortamında test edilir. Test edilmemiş bir yedek, yedek sayılmaz.

## Kurtarma hedefleri

| Metrik | Hedef | Anlamı |
|---|---|---|
| RPO (Recovery Point Objective) | ≤ 24 saat | En kötü ihtimalle son 24 saatlik veri kaybedilebilir (WAL arşivleme ile bu süre pratikte çok daha kısa olur) |
| RTO (Recovery Time Objective) | ≤ 4 saat | Bir felaket durumunda sistemin tekrar ayakta olma süresi hedefi |

Bu hedefler MVP ölçeği için makul bir başlangıç noktasıdır — tenant sayısı ve SLA taahhütleri arttıkça (bkz. [open-questions.md](../open-questions.md) madde 9, ToS/SLA) daha sıkı hedeflere geçilebilir.

## Felaket senaryoları

- **DB sunucusu çöker** → en son yedekten + WAL'dan geri yükleme, yeni bir instance'a bağlanma
- **Yanlışlıkla veri silinir** (insan hatası ya da hatalı migration) → point-in-time recovery ile silme anından öncesine dönme
- **Tüm hosting sağlayıcısı erişilemez hale gelir** → yedekler farklı sağlayıcıda/bölgede tutulduğu için yeni bir ortamda ayağa kaldırılabilir (bu senaryo için detaylı runbook, gerçek hosting kararı netleşince yazılır)

## Kapsam dışı (MVP'de yok)

- Otomatik failover (aktif-pasif DB kümesi) — tenant sayısı ve SLA baskısı arttığında değerlendirilir.

## İlgili dosyalar

- [environments.md](environments.md) — ortam tanımları
- [observability.md](observability.md) — yedekleme job'ının başarısız olması durumunda alarm
