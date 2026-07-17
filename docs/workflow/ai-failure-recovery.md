# AI Hata Kurtarma Prosedürü

[pr-and-definition-of-done.md](pr-and-definition-of-done.md)'daki "Max Iteration = 3" kuralı bir PR'ın **merge olmadan önce** takılıp kalmasını yönetiyor. Bu doküman farklı bir senaryo: **hatalı bir değişiklik zaten merge oldu** — planner yanlış anladı, coder yanlış yazdı, reviewer kaçırdı, test bir şekilde geçti. Şimdi ne olacağı tanımlı değildi.

## Neden bu insan hata kurtarmasından farklı

Klasik bir "bug fix" issue'su yeterli değil çünkü asıl soru kodun düzeltilmesi değil: **pipeline'ın neden bu hatayı yakalayamadığı**. Sadece kodu düzeltip geçersen, aynı hata sınıfı bir sonraki agent tarafından tekrar üretilebilir — kök neden dokümantasyonda/kuralda bir boşluk olabilir.

## Prosedür

```
1) Tespit
   → Hata nasıl fark edildi (production log, kullanıcı şikayeti, sonraki bir PR'da fark edildi)

2) Geri alma (rollback)
   → git revert ile hızlıca geri alınır (bkz. branch-strategy.md main koruması)
   → Bu adım TARTIŞMASIZ ilk yapılan şey, kök neden analizini beklemez

3) Kök neden sınıflandırması — dördünden biri:
   a) Spec eksikti (hiçbir dokümanda bu kural yoktu)
   b) Spec vardı ama agent'a context hydration'da verilmedi (issue-management.md'deki liste eksikti)
   c) Spec vardı, agent okudu ama yanlış yorumladı (spec belirsizdi)
   d) Spec ve context doğruydu, agent basitçe hata yaptı (rastgele hata, sistemik değil)

4) Duruma göre aksiyon
   a) → yeni bir domain-invariants.md / architecture-constraints.md maddesi eklenir
   b) → docs-dependency-map.md güncellenir, o issue tipi için "İlgili dokümanlar" listesi düzeltilir
   c) → ilgili spec dosyası netleştirilir (belirsiz cümle, somut örnekle değiştirilir)
   d) → aksiyon gerekmez, ama tekrar ederse (aynı agent/görev tipinde) (c)'ye dönülüp incelenir

5) Kayıt
   → (a) veya (c) durumunda değişiklik bir ADR'a bağlanır ("bu kural şu incident'tan sonra eklendi")
   → (b) durumunda docs-dependency-map.md'nin kendisi güncellenmiş olur, ayrı kayıt gerekmez
```

## "Sadece kodu düzelt" ile karışmaması gereken durum

Eğer hata sadece (d) kategorisindeyse (agent'ın tek seferlik, sistemik olmayan hatası), bu prosedürün tamamı işletilmez — normal bir `fix/` branch'i yeterli. Bu prosedür özellikle **(a), (b), (c)** durumları için var: hatanın kaynağı koddan önce dokümantasyon/süreçteyse.

## İlgili dosyalar

- [pr-and-definition-of-done.md](pr-and-definition-of-done.md) — merge öncesi döngü limiti (bu dosyanın önlediği durum)
- [../docs-versioning.md](../docs-versioning.md) — context drift'in (b) kategorisiyle ilişkisi
- [../adr/_template.md](../adr/_template.md) — kayıt altına alma
