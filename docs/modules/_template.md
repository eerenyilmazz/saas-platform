# Module: &lt;module_name&gt;

> This file is a template. When adding a new vertical (e.g. construction, hair salon, car wash), copy this file to `docs/modules/<module_name>.md`, fill in the headings, and delete this line.

**Package:** `com.saas.modules.<module_name>.*`
**Dependency:** `core` (auth, tenant, membership, billing) — dependency on any other module is forbidden
**Priority:** &lt;which phase this module will be developed in — see [roadmap/phases.md](../roadmap/phases.md)&gt;

## Responsibilities

- What **specific** business problem does this module solve? (not a general one, write what is specific to this industry)
- Which operations are specific to this module and not in `core`?

## Flow (if any)

Write the module's main process flow as a diagram/list of steps. Example format:

```
[Input] → [Operation] → [which check in core] → [Result]
```

## Data model (draft)

- `<table_name>` — id, tenant_id, ... (tenant_id is mandatory on every table)

## Relationship with core

- How does this module use `core.membership`? (what the membership/package concept corresponds to in this industry) — **if this industry has no membership concept at all** (e.g. an order-based vertical), do not force `membership`; define the module's own domain object (`order`, `reservation`, etc.).
- How does this module use `core.billing`? The `tenant_payment` table is kept generic through its `source_type`/`source_id` fields (see [core.md](core.md)) — even without a membership/appointment, the module's own domain object can be added here as a `source_type`, with no migration in `core`.
- Is a new `core` change needed? If so, open it as a separate task/PR for the core team/agent, and do not mix it into this module's PR.

## Commonalities with other modules (if any)

Is there another module with a similar business model (e.g. the appointment+membership model is shared by fitness and barber)? To avoid code duplication, note this commonality here, and consider moving the shared logic to `core` if needed.

## Out of scope

- Explicitly list things that belong to another module and will not be repeated here.

## Read before filling in

- [glossary.md](../glossary.md) — term names are fixed here, do not invent a new name (e.g. use "member", not "customer")
- [platform/code-style.md](../platform/code-style.md) — class/table naming rules
- After filling in, add this module's tables to [data-model-overview.md](../data-model-overview.md) (see [workflow/new-module-checklist.md](../workflow/new-module-checklist.md))
