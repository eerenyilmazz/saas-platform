# Kod Review Checklist'i

[agent-pipeline.md](agent-pipeline.md)'de reviewer agent'ın görevi "kod kalitesi, güvenlik, mantık kontrolü" olarak tanımlanmıştı — bu çok genel, hem bir agent'ın hem bir insanın **aynı** kriterlere göre kontrol etmesi için somut bir liste gerekiyordu.

## Sıra önemli — önce mimari, sonra detay

### 1. Sınır ihlali var mı (en kritik, en önce kontrol edilir)

- [ ] PR, kendi modülünün klasörü dışına dokunuyor mu? (bkz. [repo-structure.md](../repo-structure.md) bağımlılık kuralı) — dokunuyorsa **reddedilir**, ayrı bir core PR'ı açılması istenir
- [ ] `modules/*` içinde başka bir modülün paketini import eden kod var mı? — varsa **reddedilir**
- [ ] Yeni bir tablo `tenant_id` taşımıyor mu? — taşımıyorsa **reddedilir**, izolasyon ihlali riski

### 2. Terminoloji ve isimlendirme

- [ ] [glossary.md](../glossary.md)'deki terimler kullanılmış mı (`member` yerine `customer` gibi yeni bir isim icat edilmemiş)?
- [ ] Sınıf/tablo isimlendirmesi [code-style.md](../platform/code-style.md)'ye uygun mu?

### 3. Güvenlik

- [ ] Kullanıcı girdisi doğrulanıyor mu (bkz. [security.md](../platform/security.md))?
- [ ] Yeni bir endpoint, rate limiting gerektiren bir kategoriye giriyorsa (public/login benzeri) sınırlama eklenmiş mi?
- [ ] Ham SQL varsa parametrik mi (SQL injection riski)?
- [ ] Secrets (API key, şifre) koda gömülü mü? — gömülüyse **reddedilir**

### 4. API sözleşmesi

- [ ] URL yapısı [api-conventions.md](../platform/api-conventions.md)'deki `/api/v1/<katman>/<kaynak>` şemasına uyuyor mu?
- [ ] Hata yanıtları standart zarf formatında mı (`error.code`, `error.message`, `error.field`)?
- [ ] Kırıcı bir değişiklikse [mobile-compatibility.md](../platform/mobile-compatibility.md)'deki versiyonlama kuralına uyulmuş mu (yeni `v2` mi açılmış, yoksa `v1` sessizce mi kırılmış)?

### 5. İş mantığı

- [ ] PR'ın kapattığı issue'daki "Beklenen davranış" gerçekten karşılanmış mı?
- [ ] Kapsam dışına taşan bir değişiklik var mı (bkz. [pr-and-definition-of-done.md](pr-and-definition-of-done.md) "küçük PR kuralı")?
- [ ] Kritik bir tabloya (üyelik, ödeme, rol) dokunuyorsa audit log tetikleniyor mu (bkz. [audit-log.md](../platform/audit-log.md))?

### 6. Test

- [ ] Yeni endpoint/mantık için birim + entegrasyon testi var mı (bkz. [testing-strategy.md](testing-strategy.md))?
- [ ] Tenant izolasyon testi kırılmış mı (CI zaten bunu otomatik kontrol eder, ama reviewer da PR açıklamasındaki test sonucunu görmeli)?

## Reddetme vs yorum bırakma

- **1. ve 3. kategorideki** (sınır ihlali, güvenlik) bulgular her zaman PR'ı bloklar — otomatik reddedilir, tekrar düzenlenmesi istenir.
- **2, 4, 5, 6. kategorideki** bulgular yorum olarak bırakılır, coder agent (ya da insan) düzeltip tekrar sunar — otomatik red değildir ama merge öncesi çözülmesi beklenir.

## İlgili dosyalar

- [agent-pipeline.md](agent-pipeline.md) — reviewer agent'ın bu listeyi nasıl kullandığı
- [pr-and-definition-of-done.md](pr-and-definition-of-done.md)
