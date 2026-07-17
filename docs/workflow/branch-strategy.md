# Branch & PR Akışı

Her görev bir [issue](issue-management.md)'den doğar, kısa ömürlü bir branch'te yaşar.

## İsimlendirme

```
feature/<modül>-<özellik>     örn: feature/fitness-qr-giris
fix/<modül>-<konu>            örn: fix/berber-randevu-cakisma
```

Modül adı branch isminde zorunlu — hem insan hem agent hangi klasöre dokunulacağını isimden anlar. Issue numarasını taşımak zorunlu değil, PR açıklamasında `Closes #<numara>` ile bağlanır (bkz. [issue-management.md](issue-management.md)).

## Akış

1. **Issue'dan branch aç** — `feature/fitness-qr-giris`
2. **Coder agent** — kodu yazar, commit atar
3. **PR açılır** — `main`'e karşı, [pr-and-definition-of-done.md](pr-and-definition-of-done.md)'daki şablonla
4. **Reviewer agent** — [code-review-checklist.md](code-review-checklist.md)'deki 6 kategoriyi kontrol eder, PR'a yorum bırakır
5. **Tester agent** — test suite'ini çalıştırır, sonucu raporlar
6. **İnsan onayı** — sadece kritik değişikliklerde (ödeme, üyelik mantığı, core değişiklikleri)
7. **Merge → main** — Definition of Done'daki tüm maddeler işaretliyse, branch silinir, issue otomatik kapanır

## Çakışmayı azaltan kural

- Branch modül sınırında tutulursa (sadece `modules/fitness`'a dokunan bir branch), paralel çalışan başka bir branch ile çakışmaz.
- Asıl risk `core`'a dokunan değişiklikler — bunlar hızlı review edilip erken merge edilmeli ki diğer branch'ler "geride" kalmasın.
- Branch uzun süre açık tutulmaz; `core`'da değişiklik olduysa ilgili branch hemen `main`'i çeker (rebase/merge).

## main koruması

- `main`'e direkt push yasak, sadece PR üzerinden merge.
- Merge için review + test geçme şartı.
