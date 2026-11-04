# Branch & PR Flow

Every task originates from an [issue](issue-management.md) and lives on a short-lived branch.

## Naming

```
feature/<module>-<feature>    e.g. feature/fitness-qr-checkin
fix/<module>-<topic>          e.g. fix/berber-appointment-conflict
```

The module name is mandatory in the branch name — both humans and agents can tell from the name which folder will be touched. Carrying the issue number is not mandatory; it is linked in the PR description with `Closes #<number>` (see [issue-management.md](issue-management.md)).

## Flow

1. **Open a branch from the issue** — `feature/fitness-qr-checkin`
2. **Coder agent** — writes the code, commits
3. **PR is opened** — against `main`, using the template in [pr-and-definition-of-done.md](pr-and-definition-of-done.md)
4. **Reviewer agent** — checks the 6 categories in [code-review-checklist.md](code-review-checklist.md), leaves comments on the PR
5. **Tester agent** — runs the test suite, reports the result
6. **Human approval** — only for critical changes (payments, membership logic, core changes)
7. **Merge → main** — if every item in the Definition of Done is checked, the branch is deleted and the issue closes automatically

## Rule that reduces conflicts

- If branches are kept within module boundaries (a branch touching only `modules/fitness`), it will not conflict with another branch working in parallel.
- The real risk is changes that touch `core` — these should be reviewed quickly and merged early so other branches do not fall "behind".
- Branches are not kept open for long; if `core` has changed, the relevant branch pulls `main` immediately (rebase/merge).

## main protection

- Direct pushes to `main` are forbidden, merge only via PR.
- Merging requires passing review + tests.
