# ADR 0002: Spring Boot ile modüler monolit

**Durum:** Kabul edildi
**Tarih:** 2026-07-17

## Bağlam

Backend dili/framework kararı gerekiyordu. Alternatifler: Node/NestJS, Python/Django, Spring Boot. Ayrıca mikroservis mi modüler monolit mi sorusu vardı.

## Karar

Spring Boot (Java 21), modüler monolit olarak (mikroservis değil) geliştirilecek.

## Sonuç

- Spring Security ile rol/yetki yönetimi, Spring Data JPA ile DB katmanı hazır ve olgun.
- Modüler monolit, erken aşamada mikroservis karmaşıklığından (dağıtık sistem, network hataları, servisler arası iletişim) kaçınır — paket sınırları (`core`/`modules/*`) ile mantıksal ayrım zaten sağlanıyor.
- Dezavantaj: Node/Python'a göre ilk MVP çıkışı biraz daha yavaş olabilir; bu, uzun vadeli sağlamlık için kabul edilebilir bir bedel olarak değerlendirildi.
- Gözden geçirme koşulu: bir modülün trafiği/ölçek ihtiyacı diğerlerinden çok farklılaşırsa, o modül ayrı bir servise çıkarılabilir (paket sınırları zaten bunu kolaylaştırıyor).
