# Açık Sorular

Bu dosya, mimaride benim karar veremeyeceğim — iş kararı, mevcut ilişkiler veya hukuki onay gerektiren — noktaları toplar. Her madde karara bağlandığında ilgili spec dosyası güncellenir ve bu dosyadan "çözüldü" olarak işaretlenir.

## 1. Gerçek domain / şirket adı

**Neden önemli:** Tüm Java paketleri şu an yer tutucu `com.saas.*` ile yazıldı. Gerçek isim netleşmeden kod yazılırsa, sonradan tüm paket adlarını (yüzlerce dosya) toplu değiştirmek gerekir — bu maliyetli ve hataya açık bir refactor.

**Etkilenen dosyalar:** [package-naming.md](package-naming.md), her modül spec'i, tüm ADR'ler.

**Karar gerekiyor:** Şirket/ürün adı ne olacak? (Örn. bir isim netleşirse paket `com.<isim>.core`, `com.<isim>.modules.fitness` şeklinde tek seferde güncellenir.)

**Durum:** Açık — kod yazımına başlamadan önce netleşmesi önerilir (Faz 0'ın bir parçası).

---

## 2. Tenant-billing için ödeme sağlayıcısı

**Neden önemli:** Tenant'ın kendi müşterisinden (spor salonu üyesi, berber müşterisi) online ödeme alması istenirse hangi sağlayıcıyla entegre olunacağı belirlenmeli. MVP'de bu akış manuel kayıt tutma olarak tasarlandı (bkz. [platform/billing-model.md](platform/billing-model.md)), ama v1.3'te otomasyona geçilecek.

**Etkilenen dosyalar:** [platform/billing-model.md](platform/billing-model.md), [roadmap/versions.md](roadmap/versions.md) (v1.3).

**Seçenekler (kısa karşılaştırma):**

| Sağlayıcı | Not |
|---|---|
| iyzico | Türkiye'de en yaygın, pazar yeri/marketplace modeli de destekliyor (tenant'lara alt hesap açmak istenirse uygun) |
| PayTR | Basit entegrasyon, küçük işletmelerde yaygın |
| Param | Kurumsal ağırlıklı, banka entegrasyonları güçlü |

**Karar gerekiyor:** Mevcut bir üye işyeri/API anlaşman var mı, yoksa sıfırdan mı seçilecek?

**Durum:** Açık — v1.3'e kadar bloklayıcı değil.

---

## 3. Bildirim (SMS) sağlayıcısı

**Neden önemli:** [platform/notifications.md](platform/notifications.md)'de MVP kanalı SMS olarak kararlaştırıldı, kanal soyutlaması (`NotificationChannel`) sağlayıcıdan bağımsız tasarlandı — ama gerçek entegrasyon için bir sağlayıcı seçilmeli.

**Etkilenen dosyalar:** [platform/notifications.md](platform/notifications.md).

**Seçenekler (kısa karşılaştırma):**

| Sağlayıcı | Not |
|---|---|
| Netgsm | Türkiye'de yaygın, basit REST API |
| İletimerkezi | Benzer, fiyat/paket karşılaştırması yapılmalı |

**Karar gerekiyor:** Fiyat/paket karşılaştırması yapıp birini seçmek — soyutlama sayesinde bu geç karar verilebilir, kod tarafını bloklamaz.

**Durum:** Açık — Faz 2 (fitness MVP, hatırlatma bildirimleri) öncesine kadar bloklayıcı değil.

---

## 4. Onboarding modeli onayı

**Neden önemli:** [platform/onboarding.md](platform/onboarding.md)'de "MVP'de manuel onboarding, sen/ekibin tenant açar" kararını ben verdim çünkü erken aşamada self-servis kayıt formuna yatırım yapmak gereksiz mühendislik yükü. Bu varsayımın senin iş planınla uyuştuğundan emin olmam gerekiyor.

**Etkilenen dosyalar:** [platform/onboarding.md](platform/onboarding.md), [roadmap/phases.md](roadmap/phases.md) (Faz 6).

**Karar gerekiyor:** İlk müşterileri sen mi bulup manuel mi açacaksın, yoksa baştan "kim isterse kayıt olsun" self-servis bir model mi hedefliyorsun? (Self-servis olsaydı Faz 0'a kayıt formu + ödeme eklenirdi — mimari şu an bunu MVP dışına aldı.)

**Durum:** Açık — Faz 1 başlamadan önce netleşmeli, çünkü tenant seed script'i bu varsayıma göre yazılıyor.

---

## 5. KVKK — hukuki onay

**Neden önemli:** [platform/kvkk-checklist.md](platform/kvkk-checklist.md) mühendislik tarafını (açık rıza alanı, silme endpoint'i, veri saklama süresi) kapsıyor, ama bu bir hukuki danışmanlık değil. Gerçek üye verisiyle (özellikle fitness — dolaylı sağlık/hareket verisi) yayına çıkmadan önce bir avukatın aydınlatma metnini ve veri işleme envanterini onaylaması gerekiyor.

**Etkilenen dosyalar:** [platform/kvkk-checklist.md](platform/kvkk-checklist.md).

**Karar gerekiyor:** Bu onayı kimin/ne zaman alacağı — v1.0 (ilk canlı sürüm, gerçek tenant) öncesi tamamlanmalı, MVP geliştirme sürecini bloklamaz ama canlıya çıkışı bloklamalı.

**Durum:** Açık — v1.0 için zorunlu ön koşul olarak işaretlendi.

---

## 6. Platform admin paneli zamanlaması

**Neden önemli:** [platform/roles-and-access.md](platform/roles-and-access.md) ve [platform/onboarding.md](platform/onboarding.md)'de "~10 tenant" eşiğini ben varsaydım (manuel onboarding'in operasyonel yük haline geldiği nokta). Bu sayı tamamen tahmini.

**Etkilenen dosyalar:** [platform/onboarding.md](platform/onboarding.md), [roadmap/versions.md](roadmap/versions.md) (v1.2), [roadmap/phases.md](roadmap/phases.md) (Faz 6).

**Karar gerekiyor:** Senin öngördüğün büyüme hızına göre bu eşik daha erken (örn. 5 tenant) veya daha geç (örn. 20 tenant) olabilir.

**Durum:** Açık — bloklayıcı değil, tenant sayısı arttıkça gözden geçirilir.

---

## 7. Modül bazlı fiyatlandırma rakamları

**Neden önemli:** [platform/billing-model.md](platform/billing-model.md)'deki `platform_subscription` tablosu "plan (hangi modüller dahil), aylık_ücret" alanını tutuyor ama gerçek rakamlar hiçbir dosyada yok — bunlar tamamen bir pazarlama/fiyatlandırma kararı, mühendislik kararı değil.

**Etkilenen dosyalar:** [platform/billing-model.md](platform/billing-model.md).

**Karar gerekiyor:** Başlangıç paketi (örn. sadece fitness ya da sadece berber) ile tüm modüller dahil paketin fiyatı ne olacak? Rakip analizini (Türkiye'deki benzer SaaS'ların fiyatları) sen mi yapacaksın, yoksa birlikte mi değerlendirelim?

**Durum:** Açık — kod tarafını bloklamaz (fiyat alanı serbest metin/sayı olarak tutulabilir), ama v1.0 öncesi netleşmeli çünkü ilk tenant'la görüşürken lazım.

---

## 8. Hosting / deploy hedefi

**Neden önemli:** [infra/environments.md](infra/environments.md) local/staging/production ayrımını tanımladı ama **nerede** çalışacağı (hangi bulut sağlayıcı) belirtilmedi.

**Etkilenen dosyalar:** [infra/environments.md](infra/environments.md).

**Seçenekler (kısa karşılaştırma):**

| Sağlayıcı | Not |
|---|---|
| Hetzner / DigitalOcean | Ucuz, basit VPS, küçük ölçek için yeterli, kendi Docker altyapını kurarsın |
| AWS/GCP/Azure | Daha fazla yönetilen servis (managed Postgres, otomatik ölçekleme) ama daha karmaşık ve pahalı, erken aşamada gereksiz olabilir |
| Railway / Render | PaaS, en az operasyon yükü, orta ölçekte maliyet artabilir |

**Karar gerekiyor:** Bütçe ve operasyon tercihine göre biri seçilmeli — Faz 0'da CI/CD'nin nereye deploy edeceğini bilmemiz gerekiyor.

**Durum:** Açık — Faz 0'ı tamamlamak için gerekli (staging deploy hedefi olmadan CI/CD adımı yarım kalır).

---

## 9. ToS / SLA (tenant sözleşmesi)

**Neden önemli:** [platform/kvkk-checklist.md](platform/kvkk-checklist.md) veri koruma tarafını kapsıyor, ama tenant'la aranızdaki ticari sözleşme (Kullanım Şartları, varsa uptime taahhüdü/SLA, sorumluluk sınırları, ödeme gecikmesinde ne olacağı) ayrı bir hukuki konu ve hiç ele alınmadı.

**Etkilenen dosyalar:** [platform/onboarding.md](platform/onboarding.md) (kayıt sırasında bu sözleşme gösterilmeli), [infra/backup-recovery.md](infra/backup-recovery.md) (RPO/RTO hedefleri burada taahhüde dönüşür).

**Karar gerekiyor:** Bir avukatla birlikte ToS ve varsa SLA metni hazırlanmalı — bu tamamen hukuki bir iş, mühendislik tarafı sadece "ödeme gecikirse modül otomatik kapanır mı" gibi teknik uygulamayı hazırlar.

**Durum:** Açık — v1.0 (ilk gerçek tenant) öncesi tamamlanmalı, madde 5 (KVKK) ile birlikte aynı hukuki danışmanlık sürecinde ele alınabilir.

---

## 10. e-Fatura / e-Arşiv zorunluluğu

**Neden önemli:** Türkiye'de belirli ciro eşiğini aşan işletmeler GİB'in (Gelir İdaresi Başkanlığı) e-Fatura/e-Arşiv sistemine geçmek zorunda. Bu, iki farklı yerde gündeme gelebilir: (a) sen tenant'lara platform-billing için yasal fatura kesmen gerekebilir, (b) tenant'ın kendi müşterisine (üye/hizmet ücreti) yasal fatura kesme ihtiyacı olabilir. Şu ana kadar `tenant_payment`/`platform_subscription` sadece iç kayıt tutma amaçlı tasarlandı (bkz. [billing-model.md](platform/billing-model.md)), yasal fatura üretimi hiç ele alınmadı.

**Etkilenen dosyalar:** [platform/billing-model.md](platform/billing-model.md), [platform/pricing-packaging.md](platform/pricing-packaging.md).

**Karar gerekiyor:**
- Sen (platform_owner) tenant'lara kestiğin platform-billing faturasını nasıl yasallaştırıyorsun — muhasebecin üzerinden mi, yoksa sisteme entegre bir e-Fatura API'si mi gerekiyor?
- Tenant'ların kendi müşterisine yasal fatura kesme ihtiyacı MVP'de var mı, yoksa "tutar kaydı" yeterli mi (birçok küçük esnaf zaten fiş/fatura kesmiyor)?

**Durum:** Açık — mühendislik tarafını bloklamaz (mevcut `tenant_payment` şeması ileride bir `e_fatura_referans_no` alanıyla genişletilebilir, migration ucuz), ama gerçek ciro oluşmaya başladığında (v1.0 sonrası) hızla netleşmesi gereken bir konu; senin ya da tenant'ların muhasebecisiyle konuşulması gerekiyor.

---

## Özet — hangileri kod yazmaya başlamadan önce netleşmeli

| # | Soru | Bloklayıcı mı? |
|---|---|---|
| 1 | Domain/şirket adı | Faz 0 öncesi önerilir |
| 4 | Onboarding modeli | Faz 1 öncesi zorunlu |
| 8 | Hosting hedefi | Faz 0 öncesi zorunlu |
| 7 | Fiyatlandırma rakamları | v1.0 öncesi (kod değil, satış için) |
| 5 | KVKK hukuki onay | v1.0 (canlıya çıkış) öncesi zorunlu |
| 9 | ToS/SLA hukuki onay | v1.0 (canlıya çıkış) öncesi zorunlu — madde 5 ile birlikte |
| 2, 3, 6, 10 | Ödeme/SMS sağlayıcısı, panel eşiği, e-Fatura | Bloklayıcı değil, ileri fazda karar verilebilir |
