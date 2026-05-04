# Federation Gateway

Node.js **Apollo Federation Gateway** for StreamFlix. It composes four GraphQL subgraphs (DGS on Spring Boot) into one federated supergraph, validates JWT access tokens, forwards identity and tracing headers to subgraphs, and exposes a single GraphQL endpoint for clients (including the StreamFlix UI).

## Role in the stack

| Responsibility | Detail |
|----------------|--------|
| Federation | Uses `@apollo/gateway` with `IntrospectAndCompose` to merge catalog, user, rating, and playback subgraphs. |
| Auth | Verifies `Authorization: Bearer <JWT>` with the same secret as **user-service** (`JWT_SECRET`). Puts `userId` into context and sends `x-user-id` to subgraphs on downstream requests. |
| Refresh tokens | Tokens with claim `typ: "refresh"` are **not** used for authorization at the gateway (aligned with gateway policy elsewhere). |
| Tracing | Forwards W3C `traceparent` / `tracestate` from the client into subgraph requests so Java/OpenTelemetry can continue the trace. |

Subgraphs that expect an authenticated user (for example mutations like ratings or playback) rely on `x-user-id` set here after a valid access token.

## Requirements

- **Node.js** 20+ (Docker image uses `node:20-alpine`).
- Subgraphs reachable at the URLs you configure (defaults assume everything on `localhost`).

## Install and run (local)

```bash
cd federation-gateway
npm install
node index.js
```

The gateway listens on **port 4000** by default (`http://localhost:4000/graphql` with Apollo Server standalone).

There is no test runner or ESLint in this package; see repo root `AGENTS.md` for workspace conventions.

## Environment variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `JWT_SECRET` | `super-secret-streamflix-key` | Must match **user-service** signing secret so access tokens verify here. |
| `CATALOG_URL` | `http://localhost:8081/graphql` | Catalog subgraph GraphQL HTTP endpoint. |
| `USER_URL` | `http://localhost:8082/graphql` | User subgraph endpoint. |
| `RATING_URL` | `http://localhost:8083/graphql` | Rating subgraph endpoint. |
| `PLAYBACK_URL` | `http://localhost:8086/graphql` | Playback subgraph endpoint. |
| `SUPERGRAPH_POLL_MS` | `10000` | How often to re-introspect subgraphs and refresh the composed schema (picks up new fields without restarting the gateway). |

In Docker Compose / Kubernetes, point these URLs at service names or internal hosts instead of `localhost`.

## Federated subgraphs

Configured names and env-driven URLs (see `index.js`):

- **catalog** → `CATALOG_URL`
- **user** → `USER_URL`
- **rating** → `RATING_URL`
- **playback** → `PLAYBACK_URL`

## Docker

```bash
docker build -t streamflix-federation-gateway .
docker run --rm -p 4000:4000 \
  -e JWT_SECRET=your-secret \
  -e CATALOG_URL=http://host.docker.internal:8081/graphql \
  ...
  streamflix-federation-gateway
```

The `Dockerfile` copies `package*.json`, runs `npm install`, copies `index.js`, exposes **4000**, and starts with `node index.js`.

## Dependencies (high level)

- `@apollo/server` — HTTP server and Apollo integration.
- `@apollo/gateway` — Federation gateway, introspection-based supergraph composition.
- `graphql` — GraphQL core.
- `jsonwebtoken` — JWT verification for the `Authorization` header.

## Related docs

- Repo root `README.md` and `AGENTS.md` — full stack layout, ports, and JWT/trace notes.
- UI: set `VITE_GRAPHQL_URI` to this gateway (default in dev is often `http://localhost:4000/`).
