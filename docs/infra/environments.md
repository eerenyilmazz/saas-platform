# Environments & Secrets Management

## Environments

| Environment | Purpose | Where it runs |
|---|---|---|
| `local` | Development | The developer's own machine; db+api+web come up with a single command via `infra/docker` |
| `staging` | Test/demo | An environment close to real data but not production, deployed automatically after a PR is merged |
| `production` | Live | The environment used by real tenants, deployed only from `main` |

## Secrets management

- **Local:** a `.env.local` file (in .gitignore); values like the DB password/JWT secret go here, real values are never committed.
- **Staging/Production:** injected as environment variables via GitHub Actions secrets — at a small team scale a separate secrets vault (Vault, AWS Secrets Manager) is not needed; this decision is revisited as the team/infrastructure grows.

## Deploy flow

```
PR merge → main → automatic deploy to staging → manual approval → deploy to production
```

Production is not deployed automatically; it is triggered manually after a short verification on staging.

## Out of scope (not in the MVP)

- Blue-green or canary deploys — start with a single instance, add when scale requires it.
- Multi-region infrastructure.
