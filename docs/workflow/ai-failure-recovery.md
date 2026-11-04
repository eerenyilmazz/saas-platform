# AI Failure Recovery Procedure

The "Max Iteration = 3" rule in [pr-and-definition-of-done.md](pr-and-definition-of-done.md) governs a PR getting stuck **before it is merged**. This document covers a different scenario: **a faulty change has already been merged** — the planner misunderstood, the coder wrote it wrong, the reviewer missed it, the tests somehow passed. What happens now was not defined.

## Why this differs from human error recovery

A classic "bug fix" issue is not enough, because the real question is not fixing the code: it is **why the pipeline failed to catch this error**. If you only fix the code and move on, the same class of error can be produced again by the next agent — the root cause may be a gap in the documentation/rules.

## Procedure

```
1) Detection
   → How the error was noticed (production log, user complaint, noticed in a later PR)

2) Rollback
   → Quickly reverted with git revert (see the main protection in branch-strategy.md)
   → This step is UNQUESTIONABLY the first thing done; it does not wait for root cause analysis

3) Root cause classification — one of four:
   a) The spec was missing (no document had this rule)
   b) The spec existed but was not given to the agent in context hydration (the list in issue-management.md was incomplete)
   c) The spec existed, the agent read it but misinterpreted it (the spec was ambiguous)
   d) The spec and context were correct, the agent simply made a mistake (a random error, not systemic)

4) Action depending on the case
   a) → a new domain-invariants.md / architecture-constraints.md item is added
   b) → docs-dependency-map.md is updated, the "Related documents" list for that issue type is corrected
   c) → the relevant spec file is clarified (the ambiguous sentence is replaced with a concrete example)
   d) → no action needed, but if it recurs (same agent/task type), go back to (c) and investigate

5) Record
   → In case (a) or (c), the change is tied to an ADR ("this rule was added after this incident")
   → In case (b), docs-dependency-map.md itself will have been updated, no separate record needed
```

## What should not be confused with "just fix the code"

If the error falls only into category (d) (a one-off, non-systemic agent error), the whole of this procedure is not run — a normal `fix/` branch is enough. This procedure exists especially for cases **(a), (b), (c)**: where the source of the error lies in documentation/process before it lies in code.

## Related files

- [pr-and-definition-of-done.md](pr-and-definition-of-done.md) — the pre-merge loop limit (the situation this file prevents)
- [../docs-versioning.md](../docs-versioning.md) — how context drift relates to category (b)
- [../adr/_template.md](../adr/_template.md) — recording
