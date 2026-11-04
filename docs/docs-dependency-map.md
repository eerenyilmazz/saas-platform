# Documentation Dependency Map

There are 65 files, but nowhere did it say which file's change could "invalidate" which others. This file gives a "who is affected when it changes" map for **the core/binding files only** — producing a full graph of all 65 files would itself become a maintenance burden and would be misleading if not kept up to date, so the scope was deliberately kept narrow.

## Impact map (core files → affected)

| Changed file | Likely affected |
|---|---|
| **glossary.md** | All `modules/*.md`, all `platform/*.md` — if a term changes, there is a risk of inconsistency everywhere |
| **tenant-model.md** | `roles-and-access.md`, `billing-model.md`, `event-catalog.md`, `api-conventions.md`, `database-conventions.md`, all `modules/*.md` |
| **database-conventions.md** | `data-model-overview.md`, `tenant-isolation-enforcement.md`, the "Data model" sections in all `modules/*.md` |
| **api-conventions.md** | `error-handling.md`, `mobile-compatibility.md`, `api-documentation.md`, all module controller designs |
| **event-catalog.md** | `notifications.md`, `audit-log.md`, `external-integration-resilience.md` |
| **architecture-constraints.md** | `code-review-checklist.md`, `anti-patterns.md`, `bounded-context-matrix.md` |
| **domain-invariants.md** | `concurrency-and-idempotency.md`, `error-handling.md`, the relevant module specs |
| **billing-model.md** | `pricing-packaging.md`, the membership section in `tenant-model.md`, `open-questions.md` item 10 (e-Invoice) |

## Usage rule

When an agent (planner) opens an issue that touches one of the files on the left above, it also automatically adds the files in the list on the right to the "Related documents" field in [issue-management.md](workflow/issue-management.md) — a human/agent remembering this is not relied on; the table exists for that purpose.

## How this map grows

When a new core file is added (e.g. if a new module brings a concept close to `core`), a row is added to this table. Files left out of scope (not in the table) — roadmap, ADRs, workflow procedures — do not affect each other to this degree, so they are not here.

## Related files

- [docs-versioning.md](docs-versioning.md) — how this map is used in context drift detection
- [workflow/change-impact-analysis.md](workflow/change-impact-analysis.md)
