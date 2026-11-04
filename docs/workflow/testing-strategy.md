# Testing Strategy

What the tester agent runs and what level of testing is expected is defined here — without this being settled, the "tests passed/failed" decision in the agent pipeline would be meaningless.

## Test pyramid

| Level | Scope | Tooling | Expectation |
|---|---|---|---|
| Unit test | Domain logic (membership calculation, QR token validity, appointment conflict check) | JUnit 5 | Mandatory in every module, especially for classes containing business rules |
| Integration test | API endpoints, real DB behavior | Spring Boot Test + Testcontainers (real PostgreSQL) | At least one happy path + one error scenario for every new endpoint |
| Tenant isolation test | One tenant cannot access another tenant's data | Integration test, at `core` level | **Mandatory gate** — runs on every PR, the PR is not merged if it fails |
| E2E test | Critical user flows (QR check-in, appointment creation) | Few in number, used selectively because they are slow | Only for flows involving "money/access" |

## Coverage expectation

- `core` (auth, tenant, membership, billing): high coverage is targeted because it is critical, especially for tenant isolation and payment/membership calculation logic.
- `modules/*`: at a minimum, a happy-path test for every endpoint is mandatory; comprehensive coverage is not required — so as not to slow down MVP speed.
- We do not set a numeric percentage target (e.g. "80% coverage") — a coverage percentage alone is not a quality indicator; the existence of critical-path tests takes priority.

## Relationship with the agent pipeline

The `tester` agent in [workflow/agent-pipeline.md](agent-pipeline.md) runs in this order:
1. Run unit tests
2. Run integration tests (spins up a real DB with Testcontainers)
3. If the tenant isolation test fails, automatically reject the PR — it should not even go to human approval
4. Write the result to the PR as a report

## Related files

- [performance-testing.md](performance-testing.md) — load/performance testing that this pyramid does not cover (a scale question, not correctness)
