# Documentation Versioning & Context Drift Detection

## Problem

When an issue is opened, it is planned according to a particular state of `architecture-constraints.md`. The coder picks up that issue days later and writes the code — in the meantime `architecture-constraints.md` may have changed. The reviewer examines the PR at yet another moment. **None of them made a mistake but all three worked against different "realities"** — the system can produce inconsistent results even without individual errors.

## Decision: git commit SHA, not manual semver

A manually updated version number for `docs/` (like `v1.2.4`) is **not used** — manually bumping it on every documentation change would itself be the very "forgetting" risk we are trying to prevent. Instead, the git history that already exists is used:

- Whenever an issue is opened, a field is added to the template in [workflow/issue-management.md](workflow/issue-management.md): **`Spec Commit: <the current git commit SHA of the docs/ folder>`**
- When the coder starts a task, they know they are working with the documentation as it was at that SHA.
- When the PR is opened, **the current SHA at the time of opening is compared with the SHA in the issue**: if one of the files the issue references changed in the commits in between, an automatic warning is added to the PR: *"This PR was opened against a version of `architecture-constraints.md` that is N commits old, check the change in between."*

## Context Drift Detection — the concrete mechanism

```
issue is opened → Spec Commit: abc123 is recorded
        ↓
coder works (days may pass)
        ↓
PR is opened → current docs/ SHA: def456
        ↓
git diff abc123..def456 -- docs/<files in the issue's "Related documents" list>
        ↓
if there is a difference → automatic PR comment: "⚠️ Context drift: these files changed: [...]"
```

- This check is a simple CI script (a few lines of git commands) — no separate tool/service is needed, and since it is already a git repository it is free.
- Finding a difference does not block the PR (most of the time it is an unrelated change), it is only brought to the attention of the reviewer/human.

## Which files are subject to this tracking

Not every file — only files that **contain binding rules** (see [docs-dependency-map.md](docs-dependency-map.md)): `architecture-constraints.md`, `domain-invariants.md`, `glossary.md`, `tenant-model.md`, `database-conventions.md`, `api-conventions.md`, `event-catalog.md`. Files that "tell history" such as the roadmap/ADRs are not included in this tracking; they are already designed not to change (ADRs do not change once they are "Accepted", a new one is opened).

## Related files

- [docs-dependency-map.md](docs-dependency-map.md) — which files are affected when a file changes
- [workflow/issue-management.md](workflow/issue-management.md) — adding the Spec Commit field to the template
- [workflow/change-impact-analysis.md](workflow/change-impact-analysis.md) — the process to follow when a file is changed deliberately
