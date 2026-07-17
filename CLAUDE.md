# saas-platform — Working Rules

This file is loaded automatically at the start of every session in this repo. You do not need to repeat these instructions in chat — follow them by default.

## First action, every session

Read [docs/kickoff.md](docs/kickoff.md) if you have not already in this conversation. It defines the open questions to ask, the doc index, and the phase sequence.

## Default pipeline for any feature/bugfix request

Unless the user explicitly asks for something narrower (a quick question, a one-line fix, exploratory discussion), treat every "implement X" request as going through these stages **in order, without being asked**:

1. **Plan** — identify which module/phase this belongs to ([docs/platform/decision-tree.md](docs/platform/decision-tree.md)), which docs are relevant ([docs/docs-dependency-map.md](docs/docs-dependency-map.md)), whether it needs a new ADR or touches a `domain-invariants.md` rule.
2. **Code** — follow [docs/platform/code-style.md](docs/platform/code-style.md) (English identifiers, no unnecessary comments/logging) and [docs/platform/architecture-constraints.md](docs/platform/architecture-constraints.md) (layer rules, no cross-module imports, no tenant-ID conditionals, memory isolation) without exception.
3. **Self-review** — before presenting the result, check it against [docs/workflow/code-review-checklist.md](docs/workflow/code-review-checklist.md)'s 6 categories yourself. Fix what you find; don't present unreviewed code as done.
4. **Test** — write/run tests per [docs/workflow/testing-strategy.md](docs/workflow/testing-strategy.md). A feature isn't done without its tests, per [docs/workflow/pr-and-definition-of-done.md](docs/workflow/pr-and-definition-of-done.md).

Report which stage you're in as you go (briefly) so the user can see the pipeline happening without asking for it.

## Always-binding, no matter the task size

- [docs/glossary.md](docs/glossary.md) — terms and forbidden words
- [docs/platform/domain-invariants.md](docs/platform/domain-invariants.md) — business rules that must never break
- [docs/platform/tenant-isolation-enforcement.md](docs/platform/tenant-isolation-enforcement.md) — every tenant-scoped table gets RLS

## When stronger role separation is actually needed

Self-narrating "now I'm the reviewer" inside one long conversation is weaker than a fresh, isolated pass — context can drift over a long session. If the user asks for a genuine second opinion on something already built in this conversation, prefer spawning a subagent (Agent tool) or asking in a new session over continuing to role-play in the same context.

## Git

Never run **any** git command yourself — not `commit`, not `push`, not `checkout -b`/`branch`, not `merge`, nothing. The user runs every git operation, including branch creation. Always give the exact command(s) for the user to run instead, and wait for them to paste back the result. This applies even mid-task and even when a similar operation was approved earlier in the session — nothing generalizes forward. `git status`/`git log`/other read-only inspection is fine if genuinely needed for diagnosis, but default to asking the user to run and paste it rather than running it yourself.
