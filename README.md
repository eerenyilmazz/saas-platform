# saas-platform

Modüler SaaS — berber + fitness modülleriyle başlayan, monorepo altında Spring Boot + React ile geliştirilen, git bağlantılı agent workflow'uyla ilerleyen bir platform.

**Yeni buradaysan:** [docs/kickoff.md](docs/kickoff.md)'u oku — açık soruların nasıl sorulacağı ve nereden başlanacağı orada.

**Tüm dokümantasyon:** [docs/README.md](docs/README.md)

## Yerel geliştirme

```bash
docker compose -f infra/docker/docker-compose.yml up -d
cd apps/api && SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Detay: [docs/dev-setup.md](docs/dev-setup.md)
