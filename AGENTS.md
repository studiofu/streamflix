# AGENTS.md — StreamFlix Workspace

Microservices streaming platform: Spring Boot 4.0 (Java 25), Apollo GraphQL Federation, React 19.

## Repository Layout

```
admin-service/       Spring Boot Admin dashboard      (port 8085)
analytics-service/   Kafka consumer, Redis store       (port 8084)
catalog-service/     Movie catalog, MongoDB, Redis cache, Resilience4j CB (port 8081)
eureka-server/       Service discovery registry         (port 8761)
federation-gateway/  Apollo Federation Gateway (Node)   (port 4000)
k8s/                 Kustomize manifests (deploy from repo root)
otel-collector/      OpenTelemetry Collector config (OTLP → Tempo + Loki)
playback-service/    Watch progress, PostgreSQL         (port 8086)
rating-service/      Ratings, PostgreSQL + Kafka outbox  (port 8083)
scripts/             dev-up.ps1, dev-down.ps1, postgres/init/
streamflix-ui/       React 19 + Vite 8 SPA             (port 5173)
user-service/        Users + JWT auth, PostgreSQL       (port 8082)
```

No root `pom.xml` — each Java service is an independent Maven project. Build/test from the service directory.

## Build & Run Commands

### Infrastructure (docker-compose.yml — Postgres, Mongo, Redis, Kafka, OTel Collector, Tempo, Loki, cAdvisor, Prometheus, Grafana)

```bash
docker compose up -d --wait          # start infra; wait until healthy
docker compose down                  # stop infra
```

### Full stack in Docker (all services containerized + Nginx UI)

```bash
docker compose -f docker-compose-full.yml up --build   # UI at http://localhost:3000
```

### Kubernetes (Kustomize, from repo root)

```bash
kubectl apply -k .                   # deploys all services + infra into `streamflix` namespace
```

Images must be pre-built and loaded. See `k8s/README.md` for build commands and prerequisites.

### Local development (infra in Docker, apps on host)

```powershell
.\scripts\dev-up.ps1                 # docker compose up + mprocs (PowerShell only)
```

Or run manually — start Eureka first, then subgraphs, then gateway, then UI. `mprocs.yaml` defines all processes.

### Java / Spring Boot services (Maven)

```bash
mvn compile                          # compile only (from service directory)
mvn package -DskipTests              # package without tests
mvn spring-boot:run                  # run (from service directory)
mvn test                             # all tests for current service
mvn test -Dtest=CatalogServiceApplicationTests           # single test class
mvn test -Dtest=CatalogServiceApplicationTests#contextLoads  # single method
```

No Checkstyle, Spotless, PMD, or Jacoco plugins.

### Federation Gateway (Node.js)

```bash
npm install                          # first time (from federation-gateway/)
node index.js                        # start gateway
```

No tests or linter.

### StreamFlix UI (React + Vite)

```bash
npm run dev                          # dev server with HMR (from streamflix-ui/)
npm run build                        # production build
npm run lint                         # ESLint check
npm run preview                      # preview production build
```

No test runner configured. `VITE_GRAPHQL_URI` controls the GraphQL endpoint (default `http://localhost:4000/`); baked in at build time.

## Code Style — Java / Spring Boot

### Package structure

- Application class: `com.streamflix.<service_with_underscore>` (e.g. `com.streamflix.catalog_service`)
- Business code: `com.streamflix.<domain>.<layer>` (e.g. `com.streamflix.catalog.fetcher`)
- Every Application class uses `@SpringBootApplication(scanBasePackages = "com.streamflix")`

```
com.streamflix.<domain>/
  model/        Entities (JPA @Entity or Mongo @Document)
  repository/   Spring Data interfaces
  service/      Business logic @Service classes
  fetcher/      DGS GraphQL data fetchers (replaces controllers)
  config/       @Configuration classes (data seeders, etc.)
  dto/          Data transfer objects (Java records)
  auth/         Authentication helpers
```

### Naming conventions

| Type | Pattern | Example |
|---|---|---|
| Application | `XxxApplication` | `CatalogServiceApplication` |
| Data fetcher | `XxxDataFetcher` | `MovieDataFetcher` |
| Repository | `XxxRepository` | `MovieRepository` |
| Service | `XxxService` | `RatingService` |
| Entity/Document | Plain noun | `Movie`, `User`, `Rating` |
| DTO | Plain noun (record) | `AuthPayload` |
| Test | `XxxApplicationTests` | `CatalogServiceApplicationTests` |

### Imports

Group: (1) third-party/framework → (2) `com.streamflix.*` → (3) `java.*`, separated by blank lines.

### Formatting & conventions

- K&R braces, 4-space indent for business code
- No Lombok — write getters/setters manually; use Java `record` for DTOs
- Compact single-line getters/setters: `public String getId() { return id; }`
- **Field injection** with `@Autowired` (no constructor injection in this codebase)
- All API exposure via `@DgsComponent` / `@DgsQuery` / `@DgsMutation` / `@DgsEntityFetcher` — **no `@RestController`**
- Entities: `@Entity` + `@Table` (PostgreSQL), `@Document` (MongoDB)
- IDs: `@Id` + `@GeneratedValue(strategy = GenerationType.UUID)` for JPA

### Error handling & logging

- `RuntimeException` for business errors — no custom exception classes or `@ControllerAdvice`
- Some modules still use `System.err.println`; prefer **SLF4J** for new code

### Configuration

- `application.yml` only (not `.properties`); externalize with `${ENV_VAR:default}`
- GraphQL schemas in `src/main/resources/schema/schema.graphqls`
- Every service: Eureka client + Actuator + `spring-boot-starter-opentelemetry` (OTLP traces + logs)
- Key env vars: `OTLP_TRACES_ENDPOINT`, `OTLP_LOGS_ENDPOINT` (default `http://localhost:4318/v1/{traces,logs}`)

### Resilience / circuit breakers

- Only **catalog-service** uses a breaker (`mongoCatalog`). Dependency: `spring-cloud-starter-circuitbreaker-resilience4j`.
- **Programmatic** style (inject `Resilience4JCircuitBreakerFactory`) — avoids AOP conflicts with `@Cacheable`. Reads fallback to empty list/`Optional.empty()`; writes (`addMovie`) are unwrapped.
- For new breakers: use programmatic approach if the method is also `@Cacheable`/`@Transactional`; otherwise `@CircuitBreaker` annotation is acceptable.

## Code Style — StreamFlix UI (React)

- **ESM**, functional components only, single quotes, semicolons, 2-space indent
- Include `.jsx` extension in local imports: `import App from './App.jsx'`
- GraphQL operations as `const` with `gql` tag at top of file; `UPPER_SNAKE_CASE` naming
- Inline `style={{...}}` objects (no CSS framework)
- ESLint 9 flat config: `no-unused-vars` ignores names starting with uppercase or `_`

## Code Style — Federation Gateway (Node.js)

- **CommonJS** (`require` / `module.exports`), 2-space indent, semicolons
- `console.log` / `console.error` for logging; env vars via `process.env.VAR || 'default'`

## Architecture Notes

- All services communicate via **GraphQL** (Netflix DGS subgraphs federated by Apollo Gateway)
- Service discovery via **Eureka**; tracing via W3C Trace Context (OTLP → OTel Collector → Tempo + Loki)
- Database per service: MongoDB (catalog), PostgreSQL (user, rating, playback), Redis (analytics + catalog Spring Cache `moviesCatalog`)
- Kafka for events (rating → analytics via outbox pattern)
- JWT auth: gateway validates tokens, forwards `x-user-id` to subgraphs. Refresh tokens (`typ: refresh`) are rejected at gateway. UI uses proactive token refresh (`authRefresh.js`)
- `addRating`, `updatePlaybackProgress`, `recordPlay` require `x-user-id` header; `addMovie` does not
- Gateway subgraph URLs: `CATALOG_URL`, `USER_URL`, `RATING_URL`, `PLAYBACK_URL`; polling interval: `SUPERGRAPH_POLL_MS` (default 10000ms)
- `prometheus.yml` scrapes via `host.docker.internal` (local dev); `prometheus-docker.yml` uses compose hostnames (full-stack / k8s)

## Ports Quick Reference

| Service | Port | URL |
|---|---|---|
| Eureka | 8761 | http://localhost:8761 |
| Catalog | 8081 | http://localhost:8081/graphiql |
| User | 8082 | http://localhost:8082/graphiql |
| Rating | 8083 | http://localhost:8083/graphiql |
| Analytics | 8084 | http://localhost:8084 |
| Admin | 8085 | http://localhost:8085 |
| Playback | 8086 | http://localhost:8086/graphiql |
| Gateway | 4000 | http://localhost:4000 |
| UI (dev) | 5173 | http://localhost:5173 |
| UI (docker-full) | 3000 | http://localhost:3000 |
| OTLP gRPC / HTTP | 4317 / 4318 | traces: `/v1/traces`, logs: `/v1/logs` |
| Tempo | 3200 | http://localhost:3200 |
| Loki | 3100 | http://localhost:3100 |
| cAdvisor | 8099 | http://localhost:8099 |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3005 | http://localhost:3005 |
| Kafka UI | 8090 | http://localhost:8090 |
| RedisInsight | 8001 | http://localhost:8001 |
