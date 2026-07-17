# Test Stratejisi

Tester agent'ın ne çalıştırdığı, hangi seviyede test beklendiği burada tanımlanır — bu netleşmeden agent pipeline'daki "test geçti/geçmedi" kararı anlamsız kalır.

## Test piramidi

| Seviye | Kapsam | Araç | Beklenti |
|---|---|---|---|
| Birim testi | Domain mantığı (üyelik hesaplama, QR token geçerliliği, randevu çakışma kontrolü) | JUnit 5 | Her modülde zorunlu, özellikle iş kuralı içeren sınıflarda |
| Entegrasyon testi | API endpoint'leri, gerçek DB davranışı | Spring Boot Test + Testcontainers (gerçek PostgreSQL) | Her yeni endpoint için en az bir happy-path + bir hata senaryosu |
| Tenant izolasyon testi | Bir tenant'ın başka tenant'ın verisine erişemediği | Entegrasyon testi, `core` seviyesinde | **Zorunlu gate** — her PR'da çalışır, geçmezse merge edilmez |
| E2E testi | Kritik kullanıcı akışları (QR check-in, randevu oluşturma) | Az sayıda, yavaş çalıştıkları için seçici kullanılır | Sadece "para/erişim" içeren akışlarda |

## Coverage beklentisi

- `core` (auth, tenant, membership, billing): kritik olduğu için yüksek coverage hedeflenir, özellikle tenant izolasyonu ve ödeme/üyelik hesaplama mantığı.
- `modules/*`: asgari olarak her endpoint'in happy-path testi zorunlu, kapsamlı coverage zorunlu değil — MVP hızını yavaşlatmamak için.
- Sayısal bir yüzde hedefi (örn. "%80 coverage") koymuyoruz — coverage yüzdesi tek başına kalite göstergesi değil, kritik yol testlerinin var olması önceliklidir.

## Agent pipeline ile ilişki

[workflow/agent-pipeline.md](agent-pipeline.md)'deki `tester` agent'ı şu sırayla çalışır:
1. Birim testleri çalıştır
2. Entegrasyon testlerini çalıştır (Testcontainers ile gerçek DB ayağa kaldırır)
3. Tenant izolasyon testi geçmediyse PR'ı otomatik reddet, insan onayına bile gitmesin
4. Sonucu PR'a rapor olarak yaz

## İlgili dosyalar

- [performance-testing.md](performance-testing.md) — bu piramidin kapsamadığı yük/performans testi (doğruluk değil, ölçek sorusu)
