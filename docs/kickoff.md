# Kickoff Instructions — For an Agent Newly Starting on This Project

> This file is designed to be **the first thing to do** for an agent (or human) seeing this repo for the first time. When the user opens a new session, they can write this as the first message: *"I cloned this repo, read `docs/kickoff.md` and get started."*

## Step 1 — Read the context (in order)

1. [README.md](README.md) — the index of the whole documentation set, read this first
2. [glossary.md](glossary.md) — the terms, do not invent a name outside these anywhere
3. [architecture-constraints.md](platform/architecture-constraints.md) + [domain-invariants.md](platform/domain-invariants.md) — the inviolable rules

Do not ask any questions or create any files before reading these three.

## Step 2 — Ask the open questions to the user

Open [open-questions.md](open-questions.md). Check the status of the **10 items** in it. According to the summary table at the end of the file:

- Ask the user the **blocking ones** (currently: item 1 domain/company name, item 4 onboarding model, item 8 hosting target) **one by one, in plain language** — do not paste the technical text in the file directly; ask by summarizing in 1-2 sentences so that the user can answer the "what does this mean" question.
- Do not try to ask the **non-blocking ones** (payment/SMS provider, panel threshold, pricing, e-Invoice, KVKK/ToS legal approval) in the same session — tell the user "these exist too but are not blocking now, we can talk later if you like", and skip them if they do not want to answer.

**How to ask — example:**

> "Before we start, 3 decisions are needed:
> 1. What will the company's/product's real name be? (It will be used in place of `com.saas.*` in the code packages)
> 2. Will you open the first customers manually yourself, or should they sign up on their own through a registration form?
> 3. Where should the server be hosted — a cheap VPS such as Hetzner/DigitalOcean, or something more enterprise-grade like AWS/GCP?"

## Step 3 — Record the answers in the files

When the user answers, **don't just save the answer, update the relevant files**:

| Question answered | Files to update |
|---|---|
| Domain/company name | [package-naming.md](package-naming.md), `com.saas.*` → the real name in all module files (bulk replacement) |
| Onboarding model | [platform/onboarding.md](platform/onboarding.md) — it already assumes "manual"; if the user says otherwise, this file is rewritten |
| Hosting target | [infra/environments.md](infra/environments.md), [infra/backup-recovery.md](infra/backup-recovery.md) |

For each answered item, update the **Status** line of the relevant section in [open-questions.md](open-questions.md) from `Open` → `Resolved (yyyy-mm-dd)`, and remove that row from the summary table.

## Step 4 — Move on to Phase 0

Once the 3 blocking questions are answered (or the user says "start even without these" — in which case defaults are used and a note is left in `open-questions.md`), start **Phase 0** in [roadmap/phases.md](roadmap/phases.md):

- Create the monorepo skeleton according to [repo-structure.md](repo-structure.md)
- Verify/update the commands in [dev-setup.md](dev-setup.md) against the real project
- Set up the skeleton with the versions in [tech-stack.md](platform/tech-stack.md) (Java 21, PostgreSQL, Flyway)

## Step 5 — On every following step

- Follow the role boundaries in [agent-pipeline.md](workflow/agent-pipeline.md)
- Follow the language rule in [code-style.md](platform/code-style.md) (**code and documentation are always in English**)
- Before opening a new issue, fill in the template and the Context Hydration list in [issue-management.md](workflow/issue-management.md)
- If you are working on a module, read only that module's spec file + [glossary.md](glossary.md), and check [docs-dependency-map.md](docs-dependency-map.md) to see the other affected files

## About this file

This is the **session-start** counterpart of the context hydration logic in [docs-versioning.md](docs-versioning.md) — issue-based narrowing is for after an issue has been opened, while this file defines what to do "when there is no issue, starting from scratch".
