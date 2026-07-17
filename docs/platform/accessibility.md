# Erişilebilirlik (Accessibility)

[theming.md](theming.md) sadece tenant'ın seçtiği marka renginin kontrastını kontrol ediyordu — bu doküman ürünün genelinde asgari bir erişilebilirlik taahhüdünü tanımlar.

## MVP kararı: WCAG AA asgari, otomatik denetim

Kapsamlı bir erişilebilirlik programı (ekran okuyucu testleri, kullanıcı testleri) MVP için aşırı yatırım — bunun yerine **ucuza baştan kurulabilecek, otomatikleştirilebilir asgari kurallar**:

- **Kontrast:** [theming.md](theming.md)'deki tenant renk kontrolüne ek olarak, sistemin kendi (açık/koyu mod) token'ları WCAG AA eşiğini (4.5:1 normal metin, 3:1 büyük metin) baştan karşılayacak şekilde seçilir.
- **Klavye navigasyonu:** Tüm interaktif elemanlar (buton, form alanı, link) sadece klavye ile ulaşılabilir/kullanılabilir olmalı — fare zorunlu olmayan bir gereksinim, özellikle admin panelinde (personel günlük kullanır) önemli.
- **Semantik HTML:** Frontend bileşenleri (React) anlamlı HTML etiketleri kullanır (`<button>` yerine `onClick`'li `<div>` yazılmaz) — ekran okuyucu desteğinin temel ön koşulu, ekstra çaba gerektirmez.
- **Form etiketleri:** Her form alanının bir `<label>`'ı olur, sadece placeholder metin yeterli sayılmaz.

## Otomatik denetim

- Frontend CI'ına bir erişilebilirlik linter'ı eklenir (öneri: `eslint-plugin-jsx-a11y`) — [static-analysis.md](static-analysis.md)'deki statik analiz katmanının bir uzantısı, ayrı bir araç zinciri gerekmez.
- Bu, [code-review-checklist.md](../workflow/code-review-checklist.md)'nin 2. kategorisine (tutarlılık) bir madde olarak eklenir.

## Kimin için özellikle önemli

- **`tenant_staff`** (fitness resepsiyonu, berber personeli) — günlük, hızlı kullanım, çoğu zaman ellerinin dolu olduğu anlarda (bkz. [modules/fitness.md](../modules/fitness.md) check-in senaryosu) — klavye/hızlı etkileşim burada bir "güzel olsa iyi olur" değil, gerçek kullanılabilirlik meselesi.

## Kapsam dışı (MVP'de yok)

- Tam WCAG AAA uyumu
- Gerçek kullanıcı testleri (ekran okuyucu kullanan gerçek kullanıcılarla test) — v1 sonrası, gerçek talep gelirse.
- Çoklu dil erişilebilirlik testi (bkz. [localization.md](localization.md) — şu an tek dil olduğu için gündemde değil).

## İlgili dosyalar

- [theming.md](theming.md) — renk kontrastı, tenant marka rengi kısıtı
- [static-analysis.md](static-analysis.md) — otomatik denetim katmanı
