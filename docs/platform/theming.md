# Tema & Marka Sistemi

[tenant-model.md](tenant-model.md)'deki `settings.branding` alanı (`logo_url`, `color`) bir tenant'ın kendi markasını yansıtabileceğini ima ediyordu ama bunun **frontend'de nasıl uygulanacağı** hiç tanımlanmamıştı. Ayrıca sistemin kendi açık/koyu tema desteği de hiç ele alınmadı.

## İki ayrı tema kavramı — karıştırılmamalı

| Kavram | Ne | Kim kontrol eder |
|---|---|---|
| **Açık/koyu mod** | Sistemin kendi görsel modu (light/dark) | Kullanıcının cihaz tercihi |
| **Tenant markası** | Bir salonun kendi logosu/vurgu rengi | Tenant (`tenant_admin`) |

Bunlar birbirinden bağımsız çalışır — bir tenant koyu modda da kendi marka rengini görür.

## Açık/koyu mod — baştan desteklenir

- Frontend, CSS custom property (design token) tabanlı bir renk sistemi kullanır — `--color-background`, `--color-text`, `--color-accent` gibi. Bileşenler doğrudan renk kodu değil, bu token'ları referans alır.
- Koyu mod, kullanıcının işletim sistemi tercihine göre otomatik açılır (`prefers-color-scheme`), ayrıca manuel bir açık/koyu geçiş anahtarı da MVP'de bulunur (küçük bir eklenti, maliyeti düşük).
- Bu, sonradan eklenmesi zor bir özellik değil ama **baştan token sistemiyle** kurulmazsa (renkler bileşenlere gömülürse) sonradan tüm bileşenleri tek tek taramak gerekir — [adr/0006](../adr/0006-i18n-string-disarida-tutma.md)'daki "ucuza şimdi, pahalıya sonra" ilkesiyle aynı mantık.

## Tenant markası — sınırlı özelleştirme

Bir tenant'ın **tüm** renk paletini değiştirmesine izin verilmez (kontrol dışına çıkar, erişilebilirlik/okunabilirlik riski doğar) — sadece tek bir **vurgu rengi** (`--color-accent`) ve logo özelleştirilebilir.

```
tenant.settings.branding = { logo_url: "...", color: "#2C3E6B" }
```

- Bu vurgu rengi, buton/link gibi az sayıda belirli bileşende kullanılır; gövde metni, arka plan gibi temel okunabilirlik alanları sistemin kendi (açık/koyu mod) token'larından gelir, tenant bunları değiştiremez.
- **Kontrast kontrolü:** Bir tenant kendi rengini girdiğinde, sistem bu rengin arka planla yeterli kontrastı (WCAG AA eşiği) sağlayıp sağlamadığını otomatik kontrol eder — sağlamıyorsa admin panelinde uyarı gösterilir (rengi yine de kullanabilir ama bilerek seçmiş olur).

## Kapsam dışı (MVP'de yok)

- Tenant'ın kendi font/logo yerleşimi gibi ileri düzey özelleştirmesi — sadece renk + logo.
- Tema önizleme editörü — renk kodu admin panelinden düz metin olarak girilir, canlı önizleme v1 sonrası.

## İlgili dosyalar

- [tenant-model.md](tenant-model.md) — `branding` alanının veri modeli
- [file-storage.md](file-storage.md) — logo yükleme akışı
- [code-style.md](code-style.md) — frontend bileşen/token isimlendirmesi
- [accessibility.md](accessibility.md) — kontrast kontrolünün genel WCAG taahhüdüyle ilişkisi
