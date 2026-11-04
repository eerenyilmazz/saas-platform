# Code Style & Naming Rules

[dev-setup.md](../dev-setup.md) explains how to run the project; this file explains **how to write it**. The goal: even if different modules are written by different agents/people, the codebase should look as if it came from a single hand.

## Java / Spring Boot layer naming

| Layer | Suffix | Example |
|---|---|---|
| REST entry point | `Controller` | `FitnessCheckinController` |
| Business logic (interface) | `Service` | `MembershipService` |
| Business logic (implementation) | `ServiceImpl` | `MembershipServiceImpl` |
| Data access | `Repository` | `MemberRepository` |
| Database entity | (no suffix, singular name) | `Member`, `Membership` |
| Request body | `Request` | `CreateMemberRequest` |
| Response body | `Response` | `MemberResponse` |
| Entity ↔ DTO conversion | `Mapper` | `MemberMapper` |
| Domain event | `Event` | `MembershipExpiringEvent` (see [notifications.md](notifications.md)) |

Class names use the terms in [glossary.md](../glossary.md) exactly — `Member`, not `Customer`; `Tenant`, not `Business`.

`Request`/`Response` are always written as Java records (immutable, field list = constructor) — like `LoginRequest`, `LoginResponse`. Field validation in `Request`s is done with Bean Validation (`@Valid`, `@NotBlank`, `@Email`, etc.); manual `if` checks are not written inside controllers/services.

## Controller — never any business logic

A controller only binds the HTTP request (deserialize/validate) and delegates to the relevant service method — no logic such as key generation, condition checks, or recording state with try/catch is written in the controller. The test for this is simple: a controller method should be readable, without MockMvc, as a plain service call (generally a single line `return xService.y(request);`).

## Service layer — interface + impl separation

Every business logic service is written as an **interface** (`MembershipService`) and a **class** implementing it (`MembershipServiceImpl`, `@Service` goes here). The controller (and everywhere that calls the service) depends only on the interface — Spring knows which implementation to inject, and the caller does not need to know.

- This separation applies not to every `@Component`/`@Configuration` class but to **services that carry a business workflow** (such as `AuthenticationService`, `MembershipService`). Infrastructure classes (`JwtAuthenticationFilter`, `TenantAwareDataSource`, a `@ConfigurationProperties` class) remain a single concrete class — adding an interface to these would be an unnecessary layer.
- If the implementation starts to bloat (if a single class starts to carry multiple responsibilities), it is split into separate, focused helper classes (see [architecture-constraints.md](architecture-constraints.md)) — a `ServiceImpl` does not become a "god class" that solves everything inside itself.
- If necessary the repository is called directly from within `ServiceImpl`; no separate abstraction is needed.

## Lombok — reduces constructor/getter boilerplate

In classes that do dependency injection (service impls, controllers, config classes) the constructor is not written by hand; `@RequiredArgsConstructor` (Lombok) is used, and the fields stay `private final`. `@Getter` is used for getters.

```java
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final AuditWriter auditWriter;

    ...
}
```

`@RequiredArgsConstructor` is used only when the constructor's **only job** is assigning fields. If the constructor has an additional job (filling a static bridge field, calling `super(...)`, doing a validation) it is written by hand — Lombok cannot generate this, and instead of forcing it the constructor stays by hand (see `AuditWriter`, `TenantAwareDataSource`).

- `@Getter` is used on JPA entities, but **special constructors that carry business meaning are written by hand** (like `new User(tenantId, email, passwordHash, role)`) — Lombok's `@AllArgsConstructor`/`@RequiredArgsConstructor` cannot generate these, because they usually take a meaningful subset of fields rather than all of them. For the empty constructor JPA requires, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` is used.
- `@Setter` is not used on entities — state changes (like `freeze()`, `changeRole()`) are made with methods whose names express the business rule, not with a generic setter.

## Config values — never hardcoded

Every value that may vary by environment/deployment or that it is reasonable to make configurable (durations, thresholds, URLs, secrets) is written into a `@ConfigurationProperties` class (like `JwtProperties`, `LoginRateLimitProperties`) and read from `application*.properties` — it is not embedded in code as a `static final` constant or scattered through `@Value`. The class that needs it injects this properties class.

- Each logical group (JWT, rate limiting, ...) gets its own properties class — gathering them in a single giant "AppConfig" class goes against the "early abstraction" warning in [decision-tree.md](decision-tree.md) and brings together unrelated fields across groups.
- Values that are truly constant and never affected by the environment (e.g. the character set of a password generation algorithm) are outside this rule — the distinction is similar to the "real column or flexible field" logic in [decision-tree.md](decision-tree.md): if the answer to **"would someone want to change this per tenant/environment?"** is yes, it goes to config; if no, it stays a code constant.

## Mapper — Entity ↔ DTO conversion is not written by hand

If a controller/service converts an entity directly into a `Response` (copying field by field), this code is extracted to an `XMapper` interface, generated with MapStruct — just as Lombok generates the constructor/getter, MapStruct generates this conversion code with the same logic (an annotation processor, at compile time).

```java
@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
}
```

- `componentModel = "spring"` is mandatory — without it the generated class is not a Spring bean and cannot be `@Autowired`.
- The mapper lives in the relevant module's `mapper/` folder (like `com.saas.core.membership.mapper.MemberMapper`) — an extension of the in-package foldering pattern of [code-style.md](code-style.md).
- It is enough for simple, one-directional (entity → response) conversions; if a need arises on the request side too, like `Request` → entity, a `toEntity(...)` method is added to the same interface and is not extracted to a separate class.
- To be able to read the getters Lombok generates, MapStruct needs the `lombok-mapstruct-binding` dependency (build.gradle) — without it the order in which the two annotation processors run is not guaranteed.

## Language rule — code and documentation are always English

**Code (class names, field names, column names, variable names, endpoint paths) and all documentation are written in English — without exception.** The only things that are not English are the user-facing strings that are deliberately kept outside the code (see [localization.md](localization.md)):

- **Turkish (user-facing text only):** the contents of `tr.json`/`messages_tr.properties` (see [localization.md](localization.md)) — the product currently targets the Turkish market.
- **English:** class/method/variable names, database table/column names, API endpoint paths, commit messages, comments in code (if any), these documents, PR descriptions, issues, code review comments.

| Wrong (Turkish-language identifier) | Right (English identifier) |
|---|---|
| `ad`, `telefon` | `name`, `phone` |
| `sure`, `tarih` | `duration`, `date` / `scheduled_at` |
| `baslangic`/`bitis` | `start_at`/`end_at` |
| `renk`, `yontem`, `tutar` | `color`, `method`, `amount` |
| `durum`, `sebep` | `status`, `reason` |

**Retrofit note:** Earlier versions of this documentation set used Turkish column names in a few example SQL/JSON snippets (such as the Turkish words for name, phone, and source type) — these have been brought in line with this rule (see [modules/core.md](../modules/core.md), [data-model-overview.md](../data-model-overview.md)). The old Turkish source-type/source-id fields are now `source_type`/`source_id`.

## Logging discipline — no unnecessary logs

[../infra/observability.md](../infra/observability.md) defined structured logging, but "when NOT to log" had never been stated. The rule: **every line is a cost** (disk, search noise, increased PII leak risk) — a log line is written only in these cases:

- When an error/unexpected situation occurs (`WARN`/`ERROR`)
- When a business decision produces a side effect that needs to be traced later (e.g. payment status changed) — this is mostly the job of [audit-log.md](audit-log.md) already, and an `INFO` log is **not** written again in addition
- The start/result of an external service call (see [external-integration-resilience.md](external-integration-resilience.md))

**Banned:** "got here" style debug `INFO`/`DEBUG` logs are not committed into production code (they are used during development and cleaned up before they enter the PR); a log is not emitted on every iteration inside a loop; an event already written to the `audit_log` table is not written again to the application log (keeping the same information in two different places raises the question of which is correct).

## Comment discipline — comment-free by default

**Default: no comments are written.** A well-named class/method/variable already tells "what it does" — a comment that repeats this (a `// save the member` above a `saveMember()` call) is noise and must be deleted.

A comment is written **only** in these cases — none of them is "what does it do", all answer "why is it this way":

- If it is necessary to explain **why** the code was written this way (e.g. "`@Lazy` is not used here, see [architecture-constraints.md](architecture-constraints.md)")
- If a counter-intuitive business rule/constraint is applied (e.g. why the existing record is silently returned instead of a `ConflictException`, see the idempotency logic in [concurrency-and-idempotency.md](concurrency-and-idempotency.md))
- If the rationale of a known limitation/workaround is stated

Comments that describe what is done, reference the call/file/PR, or leave a "TODO" that is never returned to are flagged as findings in [code-review-checklist.md](../workflow/code-review-checklist.md).

## In-package foldering

Each module repeats the same substructure within itself:

```
com.saas.modules.fitness/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

This lets an agent guess "where to write code in the fitness module" by looking at another module's structure.

## Commit message format

```
<module>: <short summary, imperative mood>

e.g.:
fitness: add QR token generation endpoint
core: fix membership freeze logic
```

- The first word is always the affected module (`core`, `fitness`, `berber`, `platform`, `infra`, `docs`).
- The summary is in the imperative mood — whichever is chosen should be used consistently within the team.

## Frontend (React/TypeScript)

- Component files are `PascalCase.tsx` (`MemberList.tsx`), hooks are `useXxx.ts` (`useMembership.ts`).
- A single `api/` folder for API calls, with each module's own file (`api/fitness.ts`, `api/berber.ts`) — mirrors the URL scheme in [api-conventions.md](api-conventions.md) one to one.
- Colors, dates, and text are not written directly — see [theming.md](theming.md) (color tokens) and [localization.md](localization.md) (text/date files).

## Lint/format (formatting layer)

- Backend: the standard Spring Boot Checkstyle/Spotless rules (set up in Phase 0 along with the project skeleton).
- Frontend: ESLint + Prettier, default rules — if a need for customization arises it is added here.
- CI rejects a PR with lint errors (see [workflow/agent-pipeline.md](../workflow/agent-pipeline.md) — the reviewer agent catches this automatically).
- Beyond formatting (complexity, security) the static analysis layer is separate: see [static-analysis.md](static-analysis.md).

## Related files

- [glossary.md](../glossary.md) — the terms to use in naming
- [dev-setup.md](../dev-setup.md) — running lint/format in the local environment
- [static-analysis.md](static-analysis.md) — code quality/security static analysis
- [theming.md](theming.md), [localization.md](localization.md)
