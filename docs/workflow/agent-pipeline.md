# Agent Pipeline

GitHub Actions captures git events (PR opened, push made) and triggers the relevant agent. The approval/merge step is not autonomous — a human stays in the loop for critical changes.

## Role breakdown

| Role | Trigger | Responsibility |
|---|---|---|
| `planner` | New task / issue | Breaks the work into tasks, determines which module will be touched, **fills in the narrowed list of documents the coder will read** (context hydration, see [issue-management.md](issue-management.md)) |
| `coder` | Task assignment | Opens a branch, reads the relevant module's `docs/modules/*.md` spec, writes the code, commits |
| `reviewer` | When a PR is opened | Checks the 6 categories in [code-review-checklist.md](code-review-checklist.md) in order — automatically rejects boundary violations/security findings, leaves the rest as comments |
| `tester` | When a PR is opened | Runs the test suite and reports the result |
| `approver` | After review + tests pass | If every item in [pr-and-definition-of-done.md](pr-and-definition-of-done.md) is checked, merges non-critical changes and hands critical ones to a human |

## Role boundaries — who cannot do what

Each agent does only its own role's job; stepping outside the boundary is not a finding but outright an error:

- `planner` cannot write code, it only splits up tasks.
- `coder` cannot review/approve its own code.
- `reviewer` **cannot modify** code, it only leaves comments/rejections — the `coder` always makes the fix.
- `tester` cannot change code other than tests (production code).
- `coder` may write migration files (this is part of its job) but can never modify a migration that has already been merged (see [database-conventions.md](../platform/database-conventions.md)).

## Maturity levels — human approval decreases over time

The pipeline does not run fully automatically from the start — as trust grows, human approval is withdrawn step by step. The transition is left **neither to the calendar/phase number alone nor to a vague "feeling"** — each level has both a **phase reference** (for planning, so you know when to expect it) and a **concrete evidence requirement** (the real transition condition; controls are not loosened automatically just because a phase ended):

| Stage | Phase reference | Concrete transition evidence | Where human approval sits |
|---|---|---|---|
| **Initial** | Phase 0-1 | — (default starting point) | **After the planner output, before the coder starts** + before merge — both checkpoints are with a human |
| **Intermediate** | After Phase 2 is complete (fitness MVP) | None of the last **10 PRs** produced a finding in category 1 (boundary violation/security) of [code-review-checklist.md](code-review-checklist.md) | Only before merge; planning passes automatically |
| **Mature** | After Phase 4 is complete (barber MVP — second module, cross-module consistency proven) | The procedure in [ai-failure-recovery.md](ai-failure-recovery.md) has not been triggered in the last 20 PRs | Human approval only for changes marked "critical" in [pr-and-definition-of-done.md](pr-and-definition-of-done.md) (payments, membership logic, core); the rest is left to the approver |

**Rule:** Reaching the phase number is **not sufficient on its own** for a transition — the concrete evidence requirement must also be met. If a phase number has been passed without the evidence requirement being met, the next level is not entered, the current level is kept, and why the transition was not possible (which finding came up in which PR) is evaluated according to the root cause classification in [ai-failure-recovery.md](ai-failure-recovery.md).

## Spec-driven working principle

Before starting a task, each agent reads **only the relevant module's spec file** (e.g. `docs/modules/fitness.md` for a fitness task) and does not go beyond that scope. If `core` needs to be touched, this is opened as a separate task/PR; module boundaries are not mixed within the same PR.

## Information compression — role-based baseline reading set

Context Hydration in [issue-management.md](issue-management.md) narrowed the context **per issue**. In addition, each role has a **baseline reading set** that always applies regardless of the issue — the two mechanisms work together (baseline set + issue-specific extra files):

| Role | Baseline reading set (always) |
|---|---|
| `planner` | [glossary.md](../glossary.md), [decision-tree.md](../platform/decision-tree.md), [docs-dependency-map.md](../docs-dependency-map.md), [roadmap/phases.md](../roadmap/phases.md) |
| `coder` | [glossary.md](../glossary.md), [code-style.md](../platform/code-style.md), [architecture-constraints.md](../platform/architecture-constraints.md), [domain-invariants.md](../platform/domain-invariants.md) + the issue-specific module spec |
| `reviewer` | [code-review-checklist.md](code-review-checklist.md), [anti-patterns.md](../platform/anti-patterns.md), the banned words in [glossary.md](../glossary.md) |
| `tester` | [testing-strategy.md](testing-strategy.md), [domain-invariants.md](../platform/domain-invariants.md) (every invariant must have a test) |

The baseline set is kept small and fixed (not all 63+ files) — the goal is for each agent to know **by heart** the files **specific to its role that it constantly needs**, and to take the rest issue by issue.

## Relationship with GraphAI

GraphAI does not process markdown directly — each spec file is loaded as the **static input** of the relevant node in the GraphAI graph (e.g. before the `coder` node runs, it reads `docs/modules/fitness.md` and adds it to the prompt). GraphAI acts here as the pipeline that manages data flow between nodes (spec → coder → PR → reviewer → tester); the agents that make the decisions (LLM calls) are separate from the engine that connects the flow.

## Cost note

It is recommended to start with only the `reviewer` + `tester` agents; full-automation layers such as `planner`/`approver` can be left for later.
