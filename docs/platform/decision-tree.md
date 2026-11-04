# Decision Tree — Where Does This New Thing Belong?

When a new need arises (such as "add a special option to the appointment reminder message", "support a new industry"), more than one reasonable solution may exist for the same problem — if an agent picks one at random, similar needs get solved in inconsistent ways over time. This decision tree reduces the right place to a single answer through questions asked in order.

## Decision flow

```
A new need has arrived
        │
        ▼
1) Will this work the same way in ALL tenants/modules?
        │
   ┌────┴────┐
  YES        NO
   │          │
   ▼          ▼
"add to    2) Does it belong to a brand-new industry (vertical),
 core"         or is it part of an existing module?
   │                  │
   │           ┌──────┴──────┐
   │       NEW INDUSTRY   PART OF AN EXISTING MODULE
   │            │                  │
   │            ▼                  ▼
   │      "open a new        3) Should it be switchable on/off
   │       module following     from tenant to tenant
   │       new-module-          (some tenants want it,
   │       checklist.md"        some do not)?
   │                                  │
   │                          ┌───────┴───────┐
   │                         YES              NO
   │                          │                │
   │                          ▼                ▼
   │                  "feature flag,      "the module's own
   │                   add under          fixed behavior,
   │                   tenant.settings"   no config needed"
```

## Distinguishing criteria (in table form)

| Question | Answer → Place |
|---|---|
| Is it a need that is **the same in every industry**, such as auth, tenant isolation, membership, payments, notifications? | `core` |
| Is it a business rule specific to a particular industry? | The relevant `modules/<name>` |
| Is it a completely different industry (e.g. construction, restaurant)? | A new module — [new-module-checklist.md](../workflow/new-module-checklist.md) |
| Within the same module, might some tenants want it on and others off? | Feature flag (`tenant.settings.modules.<name>.<feature>`) — see [tenant-model.md](tenant-model.md) |
| Is it fixed across all tenants but may vary by environment/deploy (e.g. the SMS provider API key)? | Config/environment variable — see [environments.md](../infra/environments.md) |
| Is it for your own (platform_owner) business, or for the tenants? | The `platform` layer |

## Cases that remain ambiguous

If the tree above produces a case that falls on two different branches with equal probability (e.g. "it resembles both a new industry and an existing module"), this becomes an [ADR](../adr/_template.md) topic — the decision is not something a single agent makes on its own, it is recorded with its rationale.

## Related files

- [tenant-model.md](tenant-model.md) — the feature flag mechanism
- [../workflow/new-module-checklist.md](../workflow/new-module-checklist.md)
- [../adr/_template.md](../adr/_template.md)
