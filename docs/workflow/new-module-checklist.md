# New Module Checklist

This document defines the fixed process to follow when adding a new vertical (industry) to the platform. The goal: when someone says tomorrow "I'm going to build a construction app", to be able to integrate by simply following these steps, without rethinking the architecture.

## Steps

1. **Create the spec file**
   Copy [modules/_template.md](../modules/_template.md) to `docs/modules/<module_name>.md` and fill it in.
   Example: `docs/modules/construction.md`

2. **Determine the package name**
   `com.saas.modules.<module_name>.*` — see [package-naming.md](../package-naming.md), add the new row there.

3. **Create the folder**
   `packages/modules/<module_name>/` — see [repo-structure.md](../repo-structure.md)

4. **Register in the tenant model**
   Add a feature flag under `tenant.settings.modules.<module_name>` — see [platform/tenant-model.md](../platform/tenant-model.md)

5. **Clarify the relationship with core**
   How will this module use `membership`/`billing`? Fill in the "relationship with core" section of `_template.md` according to this question. If something is missing in core (e.g. a new field is needed), open it as a separate core task — do not touch core in the new module's first PR.

6. **Add to the roadmap**
   Add a new row to the table in [roadmap/versions.md](../roadmap/versions.md): which version it will arrive in.
   Add it to [roadmap/phases.md](../roadmap/phases.md) as a new phase (append at the end without disturbing the existing phases).

7. **Update the README index**
   Add the new file to the table of contents in [README.md](../README.md).

8. **Update the data model overview**
   Add the new module's tables under their own section in [data-model-overview.md](../data-model-overview.md).

9. **Open the first branch**
   `feature/<module_name>-mvp` — see [branch-strategy.md](branch-strategy.md)

## Example: if a "construction" module is added

| Step | Result |
|---|---|
| Spec | `docs/modules/construction.md` |
| Package | `com.saas.modules.construction.*` |
| Folder | `packages/modules/construction/` |
| Feature flag | `tenant.settings.modules.construction.enabled` |
| Relationship with core | Probably not "membership" but a "project" concept — decided according to the question in `_template.md` |
| Roadmap | A new row in `versions.md` (e.g. v1.4), a new phase appended to `phases.md` (without disturbing the existing Phases 0–6) |
| Branch | `feature/construction-project-tracking-mvp` |

This table is illustrative — when a real module is added, the `construction.md` file is filled in separately based on `_template.md`; the architecture does not change.

## Invariant rules (apply to every new module)

- A module cannot depend on any module other than `core`.
- Every table carries `tenant_id`.
- Payments/invoicing go through `core.billing` and are not rewritten.
- A module's first PR touches only its own folder, not `core`.
