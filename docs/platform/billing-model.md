# Billing Model — Two Separate Money Flows

In earlier documents there was a single "billing" concept, which was misleading. In fact there are **two completely independent flows** — if they are mixed, your revenue and the tenant's revenue get mixed in the same table.

## 1. Platform-billing — your revenue

`com.saas.platform.billing`

The salon owner (tenant) pays you **for using your SaaS** — a module-based subscription.

```
tenant → platform (you)   [monthly subscription: number of active modules × module price]
```

- **MVP decision:** No automatic payment integration. Manual tracking — the `platform_subscription` table holds the tenant, plan, payment status (paid/overdue), and you collect by bank transfer/manual invoice.
- **Why:** Building automation for the first 1-2 tenants is unnecessary engineering load. When the number of tenants reaches 10+ (see [roadmap/versions.md](../roadmap/versions.md) v1.2), automatic recurring payment (the iyzico/PayTR subscription API) is added.

### Data model (draft)

- `platform_subscription` — id, tenant_id, plan (which modules are included), monthly_fee, status (`active`/`overdue`/`cancelled`), next_payment_at

## 2. Tenant-billing — the tenant's own revenue

`com.saas.core.billing`

The tenant's **own customer** (gym member, barber client) pays the tenant. In this flow you only provide the infrastructure, the money does not pass through you (it does not enter your account) — the tenant uses its own payment provider (POS, bank, cash).

```
tenant's customer → tenant   [membership fee, service fee]
```

- This flow works together with `core.membership`: a membership sale both opens a `membership` record and creates a collection record in `core.billing`.
- Not every tenant has an equivalent of "membership" (e.g. an order-based vertical) — that is why `tenant_payment` is linked not to a membership but to **a generic source** (see below).
- In the MVP this too is for **record keeping** — actual payment collection (online payment link, card reading) can be left until after v1; in the first release the tenant takes payment its own way and only enters the "paid" information into the system.

### Data model (draft)

- `tenant_payment` — id, tenant_id, source_type (`membership`/`appointment`/`order`/...), source_id, amount, method (`cash`/`card`/`transfer`), paid_at

> Why `membership_id` is not a fixed column: a module's "unit of sale" is not always a membership or an appointment (e.g. an order). With `source_type`/`source_id`, each module can link its own domain object to tenant-billing without a new migration in `core`. This generalization is identical to the data model in [modules/core.md](../modules/core.md).

## Distinction table

| | Platform-billing | Tenant-billing |
|---|---|---|
| Who pays whom | tenant → you | tenant's customer → tenant |
| Package | `platform.billing` | `core.billing` |
| Who can see it | only `platform_owner` | `tenant_admin`, its own data |
| Automation in the MVP | no, manual tracking | no, record keeping |
| Later | iyzico/PayTR subscription | the tenant can connect its own payment provider (v2+) |

## Open question

Which payment provider to recommend to the tenant in tenant-billing (iyzico, PayTR, Param) depends on your existing business relationships — see the open questions list at the end of the conversation.

## Related files

- [pricing-packaging.md](pricing-packaging.md) — the tier structure of `platform_subscription` (without figures)
