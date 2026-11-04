# Release Roadmap

A single application version is maintained in the monorepo (SemVer, `v0.x` before release, `v1.0` the first live release). Modules are not versioned separately — maturity is tracked with a label (experimental / beta / stable), and a git tag is created on every production deploy.

| Version | Scope | Module | Status |
|---|---|---|---|
| v0.1 | Repo skeleton, core (auth + tenant) | core | planned |
| v0.2 | Fitness — QR check-in MVP | fitness | planned |
| v0.3 | Admin panel — member/membership management | core | planned |
| v0.4 | Barber — appointment MVP | berber | planned |
| v1.0 | First live release (web, one/a few pilot tenants, manual onboarding) | core | planned |
| v1.1 | Capacitor mobile wrapping, store launch | fitness | planned |
| v1.2 | Platform admin panel + self-service onboarding | platform | planned |
| v1.3 | Automatic payment integration (platform-billing + tenant-billing) | platform | planned |
