# Bounded Context Matrix

[repo-structure.md](../repo-structure.md) described the dependency rule in prose ("modules depend on core, not on each other"). This file gives the same rule as a **scannable table** — so that as the number of modules grows (fitness, barber, restaurant, car wash, hair salon...), it is enough to look at this table instead of reading prose.

## Access matrix

| | core | platform | fitness | berber |
|---|---|---|---|---|
| **core** can see | — | ❌ | ❌ | ❌ |
| **platform** can see | ✅ | — | ❌ | ❌ |
| **fitness** can see | ✅ | ❌ | — | ❌ |
| **berber** can see | ✅ | ❌ | ❌ | — |

Reading direction: whether the row **can see** the column. Example: "fitness row, core column = ✅" → fitness can import core. "fitness row, berber column = ❌" → fitness can never import berber.

**When a new module is added** (see [new-module-checklist.md](../workflow/new-module-checklist.md)), a new row/column is added to this table — the new module's `core` column is always ✅, and all other module columns are always ❌. This never changes and is not open to debate.

## Same name, different entity rule

`fitness` and `berber` both have an "appointment" concept but they are **never forced to be the same entity**:

- `fitness.training_appointment` and `berber.appointment` are two separate tables, two separate Java classes.
- The similarity of their names ("appointment") is a coincidence, not an obligation to share — the "Commonalities with other modules" section in [modules/_template.md](../modules/_template.md) exists to **make a note** of this, not to merge automatically.
- If a field/concept repeats in a third module too (if fitness + barber + restaurant all take appointments), then moving it to `core` is considered — but this is **not done before the third repetition** (see the early abstraction warning in [modules/_template.md](../modules/_template.md)).

## Package ownership (summary)

| Module | Package prefix | Who can change it |
|---|---|---|
| `core` | `com.saas.core.*` | Any agent, but the change is opened as a separate issue/PR (see [new-module-checklist.md](../workflow/new-module-checklist.md)) |
| `platform` | `com.saas.platform.*` | Only tasks assigned to the platform — an agent developing a module never touches this |
| `modules/fitness` | `com.saas.modules.fitness.*` | The agent assigned to the fitness task |
| `modules/berber` | `com.saas.modules.berber.*` | The agent assigned to the barber task |

This table uses the concept of "who owns" not as a human team, but in the sense of **which task/issue grants permission to touch which package** — it matches the `module:` label in [issue-management.md](../workflow/issue-management.md) one to one.

## Related files

- [../repo-structure.md](../repo-structure.md) — the prose rule this matrix is based on
- [architecture-constraints.md](architecture-constraints.md) — how violating this matrix is prevented (ArchUnit)
- [../workflow/new-module-checklist.md](../workflow/new-module-checklist.md)
