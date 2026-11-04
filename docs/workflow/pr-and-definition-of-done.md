# PR Template & Definition of Done

Step 3 "PR is opened" in [branch-strategy.md](branch-strategy.md) was defined, but **what a PR should contain** and **when a piece of work counts as truly done** had not been.

## PR description template

```markdown
## What changed
Short summary (one sentence is enough, the details are already in the commits)

## Why
Closes #<issue number>

## How it was tested
- [ ] Unit tests passed
- [ ] Integration tests passed
- [ ] (if any) manual test steps

## Module touched
`module:<core|fitness|berber|platform>` — per the conflict rule in [branch-strategy.md](branch-strategy.md), the PR should touch only this module's folder
```

## Definition of Done — when a PR is considered truly ready

A PR is not merged until all of the following are met:

- [ ] The code complies with the naming rules in [code-style.md](../platform/code-style.md)
- [ ] The terms used are consistent with [glossary.md](../glossary.md) (no new term invented)
- [ ] Every new/changed endpoint complies with the URL/error format in [api-conventions.md](../platform/api-conventions.md)
- [ ] Every new table carries `tenant_id` (see [tenant-model.md](../platform/tenant-model.md))
- [ ] If it touches critical tables (membership, payment, role), it goes through the audit log mechanism (see [audit-log.md](../platform/audit-log.md))
- [ ] Unit + integration tests are written and passing (see [testing-strategy.md](testing-strategy.md))
- [ ] The tenant isolation test is not broken (the CI gate already enforces this)
- [ ] The PR touches only its own module's folder, and does not touch `core` without notice
- [ ] If it is a new module: [data-model-overview.md](../data-model-overview.md) is updated (see [new-module-checklist.md](new-module-checklist.md))
- [ ] Reviewer agent approval + (if critical) human approval is in place — see [code-review-checklist.md](code-review-checklist.md)

## Max Iteration = 3 rule (reviewer↔coder loop)

The coder agent writes code → the reviewer rejects → the coder fixes → the reviewer gets stuck on something else again — this loop can grow uncontrollably between agents, and every round is an API cost.

**Rule:** If the reviewer-coder loop on the same PR exceeds **3 rounds** (the reviewer rejects/requests changes a 3rd time), the pipeline stops automatically and the PR is marked with a "human review required" label — the `approver` or `platform_owner` steps in and either makes the decision (accept/change) or re-scopes the issue. The coder does not make a 4th attempt.

- This counter is kept visible in the PR description (like `review-iteration: 2/3`).
- The goal is not to punish the coder — a disagreement that cannot be resolved in 3 rounds is usually a sign that the rule itself (an item in code-review-checklist.md) is ambiguous, in which case the checklist itself is reviewed.

## Small PR rule

A PR is kept small enough to close a single issue. Changes that go beyond the issue's scope, in the style of "while I was at it I also fixed this", become a separate issue/PR — the same rationale as the short-lived branch principle in [branch-strategy.md](branch-strategy.md): a small PR speeds up both human and agent review and reduces conflict risk.

## Related files

- [issue-management.md](issue-management.md)
- [code-review-checklist.md](code-review-checklist.md)
