# Issue Management

[branch-strategy.md](branch-strategy.md) describes what happens **after a task has been turned into a branch** — this document defines **how a task is defined** (the issue) and how it is linked to a branch.

## Issue types

| Type | Label | When it is opened |
|---|---|---|
| `feature` | `type:feature` | A new feature/module piece |
| `bug` | `type:bug` | Existing behavior is wrong |
| `chore` | `type:chore` | Work that involves no code writing (dependency updates, documentation) |
| `spike` | `type:spike` | Work that needs research/a decision and will not be turned into code yet — its output is usually an [ADR](../adr/_template.md) |

## Required labels

Every issue carries at least these three labels:

- **Module**: `module:core`, `module:fitness`, `module:berber`, `module:platform` — states up front which folder will be touched, matches the branch naming in [branch-strategy.md](branch-strategy.md) one to one.
- **Priority**: `priority:critical` (affects production), `priority:high`, `priority:normal`, `priority:low`
- **Type**: the table above

## Issue template

```markdown
## Context
Why is this work needed? (a user complaint, a roadmap item, see [roadmap/phases.md](../roadmap/phases.md))

## Expected behavior
What should happen — if possible, reference the relevant module spec (e.g. [modules/fitness.md](../modules/fitness.md))

## Out of scope
What will not be done in this issue (to prevent scope creep)

## Related documents (the coder agent reads ONLY these)
- [ ] docs/modules/<module>.md
- [ ] docs/platform/<related files, if any>
- [ ] docs/glossary.md (always)

## Definition of Done
- [ ] Code written
- [ ] Tests written (see [testing-strategy.md](testing-strategy.md))
- [ ] Related documentation updated (if any)
```

## Context Hydration — give the agent narrowed context, not the whole documentation

As the 63-file documentation set grows, having a coder agent read all of it on every task is both costly and carries a "context drift" risk (an agent working on `fitness` could miss a small change in `core`). So **part of the planner role's job** is to fill in the "Related documents" list above when opening an issue — when a task is given to the coder agent, only this narrowed list + [glossary.md](../glossary.md) (always mandatory, for term consistency) is provided, not all 63 files.

- Minimum mandatory context on every task: the relevant module's spec file + [glossary.md](../glossary.md) + [code-style.md](../platform/code-style.md).
- If the change touches `core` (opened as a separate issue, see "Issues that touch core" below), that issue's context list includes `core.md` + the relevant `platform/*` files.
- If this list is filled in incompletely (a file the agent actually needs is not on the list) and this shows up as an inconsistency in [code-review-checklist.md](code-review-checklist.md), the planner is expected to correct it in the next issue — this list is also something learned over time and does not have to be perfect the first time.

## Issue → branch → PR link

```
issue #42 (module:fitness, type:feature) → branch: feature/fitness-qr-checkin → "Closes #42" in the PR description
```

- The branch name reflects the issue content but does not have to carry the issue number (the `feature/<module>-<feature>` format in branch-strategy.md is preserved).
- The PR description references the issue with `Closes #<number>` — when the PR is merged the issue closes automatically, which provides audit/traceability (which need a code change came from can always be traced back).

## Issues that touch core

If an issue affects both a module and `core` (e.g. a new module wants a field that is missing in core), **two separate issues** are opened: one `module:core`, one `module:<relevant module>` — consistent with the "open as a separate task for core" rule in [new-module-checklist.md](new-module-checklist.md), the two are not mixed in a single PR.

## Related files

- [branch-strategy.md](branch-strategy.md)
- [pr-and-definition-of-done.md](pr-and-definition-of-done.md)
