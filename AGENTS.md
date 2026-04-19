# AGENTS.md — StreamFlix Workspace

This is a microservices streaming platform built with Spring Boot (Java 25), Apollo GraphQL Federation, and React.

## Repository Layout

```
admin-service/       Spring Boot Admin dashboard      (port 8085)
analytics-service/   Kafka consumer, Redis store       (port 8084)
catalog-service/     Movie catalog, MongoDB, Redis cache (port 8081)
eureka-server/       Service discovery registry         (port 8761)
federation-gateway/  Apollo Federation Gateway (Node)   (port 4000)
rating-service/      Ratings, PostgreSQL + Kafka outbox  (port 8083)
playback-service/    Simulated watch progress, PostgreSQL   (port 8086)
scripts/             dev-up.ps1, dev-down.ps1
streamflix-ui/       React 19 + Vite 8 SPA             (port 5173)
user-service/        Users + JWT auth, PostgreSQL       (port 8082)
```

## Build & Run Commands

### Infrastructure (Postgres, MongoDB, Redis, Kafka, Zipkin)

```bash
docker compose up -d --wait          # start infra; wait until healthy
docker compose down                  # stop infra
```

### Local development (all services)

```powershell
.\scripts\dev-up.ps1                 # docker compose up + mprocs
.\scripts\dev-down.ps1               # docker compose down
```

### Java / Spring Boot services (Maven)

```bash
# Build a single service
mvn compile                          # compile only
mvn package -DskipTests              # package without tests

# Run a single service (from the service directory)
mvn spring-boot:run

# Tests
mvn test                             # run all tests for a service
mvn test -Dtest=CatalogServiceApplicationTests   # run a single test class
mvn test -Dtest=CatalogServiceApplicationTests#contextLoads  # single method
```

No Checkstyle, Spotless, PMD, or Jacoco plugins are configured.

### Federation Gateway (Node.js)

```bash
node index.js                        # start gateway (from federation-gateway/)
```

No tests or linter configured for this project.

### StreamFlix UI (React + Vite)

```bash
npm run dev                          # dev server with HMR
npm run build                        # production build
npm run lint                         # ESLint check
npm run preview                      # preview production build
```

No test runner (Jest/Vitest) is configured. No `npm test` script exists.

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

Group imports in this order (separated by blank lines):
1. Third-party / framework (`org.springframework.*`, `com.netflix.*`, `jakarta.*`)
2. Project-internal (`com.streamflix.*`)
3. Java standard library (`java.util.*`)

### Formatting

- K&R brace style (opening brace on same line)
- 4-space indentation preferred for business code
- Compact single-line getters/setters: `public String getId() { return id; }`
- No Lombok — write getters/setters manually; use Java `record` for DTOs

### Annotations & DI

- **Field injection** with `@Autowired` (no constructor injection in this codebase)
- All API exposure via `@DgsComponent` / `@DgsQuery` / `@DgsMutation` / `@DgsEntityFetcher`
- **No `@RestController`** — everything is GraphQL via Netflix DGS
- Entities: `@Entity` + `@Table` for PostgreSQL, `@Document` for MongoDB
- IDs: `@Id` + `@GeneratedValue(strategy = GenerationType.UUID)` for JPA

### Error handling

- Throw `RuntimeException` with descriptive messages for business errors
- No custom exception classes or `@ControllerAdvice` in the current codebase
- Logging is inconsistent across modules: e.g. **catalog** `MovieDataFetcher` uses **SLF4J**; some paths still use **`System.err.println`** (e.g. parts of rating/analytics). Prefer SLF4J for new Java code.

### Configuration

- Use `application.yml` (not `.properties`)
- Externalize with `${ENV_VAR:default}` pattern
- Every service includes Eureka client + Actuator + Zipkin tracing config
- GraphQL schemas in `src/main/resources/schema/schema.graphqls`

## Code Style — StreamFlix UI (React)

### Conventions

- **ESM** (`import`/`export`, `"type": "module"` in package.json)
- **Functional components only** with React hooks
- **Single quotes**, **semicolons**, **2-space indentation**
- Include `.jsx` extension in local imports: `import App from './App.jsx'`
- GraphQL operations defined as `const` with `gql` tag at top of file
- Styling via inline `style={{...}}` objects (no CSS framework)

### Naming

- Components: PascalCase (`App`)
- Functions/variables: camelCase
- GraphQL constants: UPPER_SNAKE_CASE (`GET_MOVIES`, `LOGIN_USER`, `ADD_RATING`, `ADD_MOVIE`)

### ESLint (flat config, ESLint 9)

- Extends: `js.configs.recommended`, `react-hooks`, `react-refresh`
- `no-unused-vars`: error, ignoring names starting with uppercase or `_`
- Run with `npm run lint` from `streamflix-ui/`

## Code Style — Federation Gateway (Node.js)

- **CommonJS** (`require` / `module.exports`)
- 2-space indentation, semicolons
- `console.log` / `console.error` for logging
- Environment variables with `process.env.VAR || 'default'` fallbacks

## Architecture Notes

- All services communicate via **GraphQL** (Netflix DGS on Java subgraphs, Apollo Gateway federates them)
- Service discovery via **Eureka**; distributed tracing via **Zipkin** + W3C Trace Context
- Database per service: MongoDB (catalog), PostgreSQL (user, rating, playback), Redis (analytics state **and** catalog **Spring Cache** for the `movies` query, cache name `moviesCatalog`)
- Kafka for event-driven communication (rating → analytics via outbox pattern)
- Apollo Federation `@key` / `@extends` directives for cross-service entity resolution
- JWT auth: tokens verified at gateway, `x-user-id` header forwarded to subgraphs when `context.userId` is set (valid non-refresh JWT); UI uses **`authRefresh.js`** + Apollo links in **`main.jsx`** for proactive refresh and 401 retry
- **Catalog vs rating mutations:** `addRating` in **rating-service** requires **`x-user-id`** (`@RequestHeader`, throws if missing). **`updatePlaybackProgress`** and **`recordPlay`** in **playback-service** use the same header pattern. **`User.playHistory`** on **playback-service** only returns entries when the requested user id matches **`x-user-id`** (otherwise an empty list), mirroring **`continueWatching`**. **`addMovie`** on **catalog-service** has no such check; **streamflix-ui** only renders the add-movie form when logged in. Gateway **supergraph** polling: `SUPERGRAPH_POLL_MS` (default 10000) in `federation-gateway/index.js`.

## Ports Quick Reference

| Service | Port | URL |
|---|---|---|
| Eureka | 8761 | http://localhost:8761 |
| Catalog | 8081 | http://localhost:8081/graphiql |
| User | 8082 | http://localhost:8082/graphiql |
| Rating | 8083 | http://localhost:8083/graphiql |
| Playback | 8086 | http://localhost:8086/graphiql |
| Analytics | 8084 | http://localhost:8084 |
| Gateway | 4000 | http://localhost:4000 |
| UI | 5173 | http://localhost:5173 |
| Zipkin | 9411 | http://localhost:9411 |
