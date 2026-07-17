# ADR 0001: Monorepo yapısı

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

Modüler bir SaaS (core + berber + fitness + ileride yeni dikeyler) geliştirilecek. Her modülü ayrı repo yapmak (multi-repo) mı, tek repo altında klasör bazlı ayırmak (monorepo) mı sorusu netleşmeliydi.

## Karar

Monorepo, klasör bazlı modül ayrımı (`packages/core`, `packages/modules/*`) kullanılacak.

## Sonuç

- Ortak kod (`core`) tekrar edilmez, versiyon senkronizasyonu derdi olmaz.
- Tek CI/CD pipeline'ı, path bazlı tetikleme ile hangi modülün değiştiğini ayırt edebilir.
- Risk: `core`'a dokunan branch'ler diğer açık branch'leri "geride" bırakabilir — bu [workflow/branch-strategy.md](../workflow/branch-strategy.md)'deki kısa ömürlü branch kuralıyla azaltılıyor.
- Gözden geçirme koşulu: bir modül tamamen bağımsız bir ürün olarak ayrı satılmaya başlarsa (farklı deploy döngüsü gerekirse), o modül ayrı repoya çıkarılabilir.
