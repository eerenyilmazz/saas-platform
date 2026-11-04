# Static Code Analysis

The "Lint/format" section in [code-style.md](code-style.md) covered only formatting (Checkstyle/Spotless/ESLint) — this document defines a separate layer that catches cases where code **is formatted correctly but is risky in logic/security**. In multi-agent development this layer is especially important: code an agent writes may be flawless in format but problematic in complexity/security, and relying on a human to catch this by hand on every PR is not reliable.

## Two separate kinds of static analysis

| Kind | Tool (suggested) | What it catches |
|---|---|---|
| **Code quality/complexity** | SonarQube (Community Edition, self-host) or SonarCloud | High cyclomatic complexity, code duplication, dead code, potential null pointers |
| **Security** | CodeQL (free on GitHub) or Semgrep | Patterns such as SQL injection, hardcoded secrets, insecure deserialization |

This is different from Dependabot in [dependency-management.md](dependency-management.md) — Dependabot scans **third-party** libraries for known vulnerabilities, while the tools here scan **the code we write ourselves**.

## CI integration

- Runs automatically on every PR (as a step in GitHub Actions, see [agent-pipeline.md](../workflow/agent-pipeline.md)).
- Findings are separated by severity: a **critical/high** finding blocks the PR (handled with the same seriousness as the reviewer agent's "boundary violation/security" category in [code-review-checklist.md](../workflow/code-review-checklist.md)), a **medium/low** finding is left as a comment and does not block.

## The agent checking SonarCloud findings through the API

Claude has no authority to log in to SonarCloud from a browser — in the past, to see a PR's findings the user had to share a screenshot, which was both slow and required the finding's details (rule, line, rationale) to be transferred by hand. Instead, SonarCloud's own REST API is used — it requires no login, only an API token is enough.

**Setup (once, on the user's own machine):**

1. A **User Token** is generated at [sonarcloud.io/account/security](https://sonarcloud.io/account/security).
2. The token is added to the user's shell profile (`~/.zshrc`) as an environment variable, **without ever being written** into the project repo or the conversation: `export SONAR_TOKEN="..."`.
3. This is not a project secret — the token stays entirely on the user's own machine, does not get mixed into git, and is not used in CI (CI already uses its own `SONAR_TOKEN` repo secret, see the `sonar` block of `build.gradle`).

**Usage:**

```bash
curl -s -u "$SONAR_TOKEN:" \
  "https://sonarcloud.io/api/issues/search?componentKeys=eerenyilmazz_saas-platform&pullRequest=<PR_NUMBER>&resolved=false&issueStatuses=OPEN,CONFIRMED"
```

JSON is returned (the `issues` array) — each finding's rule, file/line, severity, and message are read programmatically, with no screenshot or manual transfer needed.

**Scope:** This automates only **fetching the data** — which finding is real, whether it will be fixed, whether it stays "Won't Fix" or "Open" is still decided through conversation (the agent's suggestion + the user's approval). A fully autonomous flow that sees the findings and does automatic commit/push (see the next item) was deliberately not set up — at this project scale (a single developer, few PRs) the risk of unsupervised automation outweighs the speed it gains.

## Complexity threshold (guideline, not a strict rule)

- If a method/function's cyclomatic complexity exceeds a certain threshold (suggestion: 10), SonarQube gives a warning — this is not a "can never be exceeded" rule but a "probably should be split" signal. The reviewer (agent or human) decides based on context.
- The goal is not to comply with a mechanical number but to keep the code agents produce readable over time — especially when multiple agents contribute to the same codebase, complexity can grow uncontrolled.

## Periodic review of accepted findings

The CI/Quality Gate is an automatic gate against new code, but there is no automatic re-check for **old findings already marked "Won't Fix"/"Safe"** — once they have been justified and closed, if nobody looks again, the rationale can become invalid over time (e.g. CSRF being closed with the "stateless JWT API" rationale stops being true if the API one day adds a cookie/session-based flow).

- **Once a month**, all findings in "Won't Fix"/"Safe" status in SonarCloud are reviewed one by one: is the relevant code still the same, is the rationale still valid.
- If the rationale has become invalid (the code has changed, the assumption is no longer true) the finding is moved back to "Open" and handled through the normal PR flow (a code fix or closing again with a new rationale).
- This review is not ad hoc but is considered a recurring maintenance task, like the one in [pr-and-definition-of-done.md](../workflow/pr-and-definition-of-done.md) — who does it (agent/human) will become clearer as the team grows; in the MVP, whoever notices it does it.

## Out of scope (not in the MVP)

- Automatic PR rejection based on a code quality "score" (e.g. "automatic rejection if coverage drops below 80%") — as already stated in [testing-strategy.md](../workflow/testing-strategy.md), we do not set a numeric coverage target, which keeps this consistent.

## Related files

- [code-style.md](code-style.md) — the formatting layer (which this file does not cover)
- [dependency-management.md](dependency-management.md) — third-party library scanning
- [../workflow/code-review-checklist.md](../workflow/code-review-checklist.md)
