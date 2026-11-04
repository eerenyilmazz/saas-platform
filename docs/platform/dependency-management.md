# Dependency Management

The modular monolith will be developed by many agents/people — if each of them tries to add a library that makes their own job easier, unaudited dependencies that conflict with each other will quickly pile up.

## Rule when adding a new dependency

- Adding a new third-party library is **always** done in a separate small PR, not hidden inside a feature PR — so that the reviewer can evaluate it separately (see [code-review-checklist.md](../workflow/code-review-checklist.md)).
- **A coder agent cannot add a new dependency on its own** — independent of the maturity levels in [agent-pipeline.md](../workflow/agent-pipeline.md), adding a dependency is always a category requiring human approval (it is not left to the approver). The agent's reflex of "I'll quickly add a library" for a simple string/date operation is the most common deviation, so this is kept without exception.
- The library to be added must pass these questions: (1) is it actively maintained (when was the last commit), (2) does it have known security vulnerabilities, (3) is another already-added library doing the same job (e.g. two different HTTP client libraries are not added together), (4) does the standard library (Java `java.time`, `Optional`, etc.) already do this job — most of the time it does.
- The version is pinned (loose ranges such as `^` or `~` are not used) — to prevent an unexpected minor/patch update from silently changing behavior.

## Automatic update scanning

- Dependabot (or Renovate) is connected to CI and automatically opens PRs for dependencies with security vulnerabilities — these PRs go through the normal review process and are not merged automatically.
- If a critical update (with a CVE) comes out, this PR takes priority over the others (with the `priority:critical` label, see [issue-management.md](../workflow/issue-management.md)).

## Dependency repetition across modules

If multiple modules try to solve the same function (e.g. date handling, an HTTP client) with different libraries, this is flagged as a finding in category 2 (naming/consistency) of [code-review-checklist.md](../workflow/code-review-checklist.md) — **one** library is used per job across the project, and this choice is fixed where it is first used.

## Mandatory centralized version management (BOM)

**No module can define an independent library version in its own `build.gradle`/`pom.xml` file.** All third-party library versions are defined at a single central point in the root project (a `platform`/version catalog in Gradle, `dependencyManagement`/BOM in Maven); modules use a library only **by name** and do not write a version number.

**Why so strict:** The module isolation in [architecture-constraints.md](architecture-constraints.md) (`modules/fitness`, `modules/berber` do not know about each other) works **in reverse** for dependency versions — they are all combined in the same JVM, in the same runnable application (`apps/api`). If one module wants Jackson `2.15` and another module wants `2.17`, the two cannot work together in the same JAR (jar hell / `ClassNotFoundException` risk) — the modules are isolated as code but **a single process at runtime**, so dependency versions are **exempt** from inter-module isolation, central and single.

- An agent that needs a new library first looks at the central catalog — if it is already there it uses it, if not (human-approved, see the rule above) it is added to the central catalog **from a single place**, and the using module does not determine its own version.
- This is checked under category 1 (boundary violation) of [code-review-checklist.md](../workflow/code-review-checklist.md) — if a module is seen defining its own version number, the PR is rejected automatically.

## Related files

- [tech-stack.md](tech-stack.md) — core framework/tool decisions (for third-party helper libraries, which this file does not cover)
- [../workflow/code-review-checklist.md](../workflow/code-review-checklist.md)
