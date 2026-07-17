# Observability — Loglama, Hata İzleme, Uptime

Production'da bir şey bozulduğunda bunu tenant'tan önce senin öğrenmen gerekiyor. Bu doküman üç katmanı tanımlar: loglama, hata izleme, uptime/alerting.

## 1. Yapılandırılmış loglama

- Tüm servisler (Spring Boot `api`) **yapılandırılmış (JSON) log** üretir — düz metin log yerine, her satırda `tenant_id`, `request_id`, `seviye` (info/warn/error) gibi alanlar bulunur.
- `request_id`, bir isteğin API'ye girişinden çıkışına kadar tüm log satırlarında aynı kalır — bir hatayı araştırırken ilgili tüm logları tek sorguda bulabilmek için.
- Loglar merkezi bir yerde toplanır (öneri: hosting sağlayıcısının sunduğu log servisi, örn. bir VPS'te self-hosted basit bir log toplayıcı; büyük bir log altyapısına MVP'de gerek yok).

### PII maskeleme (KVKK ile bağlantılı)

Loglar `tenant_id`/`request_id` gibi teknik alanları serbestçe taşır ama **kişisel veri asla düz metin loglanmaz** — telefon numarası, ad-soyad gibi alanlar log satırına yazılmadan önce maskelenir (örn. `05XX XXX XX 34` gibi kısmi maskeleme) ya da hiç loglanmaz, sadece ID referansı (`member_id`) geçer. Bu, [kvkk-checklist.md](../platform/kvkk-checklist.md)'deki veri koruma ilkesinin loglama katmanına yansımasıdır — bir log sızıntısı KVKK ihlaline dönüşmesin diye.

## 2. Hata izleme (error tracking)

- Backend ve frontend'de yakalanmayan hatalar otomatik raporlanır (öneri: Sentry — açık kaynak self-host seçeneği de var, ücretsiz katmanı MVP için yeterli).
- Her hata raporu `tenant_id` ve `request_id` ile etiketlenir — "hangi tenant'ta, hangi istekte" sorusu hemen cevaplanabilsin.
- Kritik hatalar (örn. ödeme kaydı başarısız, tenant izolasyon ihlali denemesi) anlık bildirim (e-posta/Slack) tetikler.

## 3. Uptime / alerting

- Basit bir dış izleme servisi (örn. UptimeRobot, Better Uptime) `api`'nin `/health` endpoint'ini düzenli aralıklarla kontrol eder.
- `/health` endpoint'i sadece "ayakta mıyım" değil, DB bağlantısının da çalıştığını kontrol eder (DB'ye basit bir sorgu atarak).
- Downtime durumunda anlık bildirim (SMS/e-posta) — bu servisler genelde ücretsiz/düşük maliyetli katman sunar, MVP için ayrı bir altyapı kurmaya gerek yok.

## Neyin izleneceği — öncelik sırası

1. API ayakta mı (`/health`)
2. Ödeme/üyelik kayıtlarında hata oranı (yakalanmayan exception sayısı)
3. Yedekleme job'ı başarılı çalıştı mı (bkz. [backup-recovery.md](backup-recovery.md))
4. QR check-in başarısız oranı (fitness modülü için — anormal yükselme, donanım veya API sorununu işaret edebilir)

## Kapsam dışı (MVP'de yok)

- Dağıtık tracing (birden fazla servis arası istek takibi) — modüler monolit tek servis olduğu için şimdilik gereksiz, mikroservise geçilirse gündeme gelir.
- Gelişmiş dashboard/metrik altyapısı (Grafana/Prometheus) — tenant ve trafik sayısı arttıkça değerlendirilir.

## İlgili dosyalar

- [backup-recovery.md](backup-recovery.md)
- [environments.md](environments.md)
