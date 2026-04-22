# Kubernetes (StreamFlix)

Manifests are built with [Kustomize](https://kustomize.io/) from the **repository root** (so `configMapGenerator` can include `tempo/`, `loki/`, `otel-collector/`, and other project files in one build).

## Prerequisites

- A cluster with a default `StorageClass` (for PVCs), or adjust PVCs and `storageClassName`.
- An **Ingress** controller if you use `k8s/base/ingress.yaml` (e.g. [ingress-nginx](https://kubernetes.github.io/ingress-nginx/)). Set `spec.ingressClassName` in that file to your controller’s `IngressClass` name, or create an `IngressClass` named `nginx`.
- Sufficient memory for all pods (roughly 8+ GiB for a single-node dev cluster).

## Build images

All StreamFlix workloads use the `streamflix/…:latest` image names. Build and (optionally) push them:

```bash
docker build -t streamflix/eureka-server:latest eureka-server
docker build -t streamflix/catalog-service:latest catalog-service
docker build -t streamflix/user-service:latest user-service
docker build -t streamflix/rating-service:latest rating-service
docker build -t streamflix/playback-service:latest playback-service
docker build -t streamflix/analytics-service:latest analytics-service
docker build -t streamflix/admin-service:latest admin-service
docker build -t streamflix/federation-gateway:latest federation-gateway
```

**UI:** the Vite app needs the public GraphQL URL at **build** time, matching how users reach the API (same host, or `api.…` from the browser):

```bash
docker build -t streamflix/streamflix-ui:latest \
  --build-arg VITE_GRAPHQL_URI="http://api.streamflix.local/" \
  streamflix-ui
```

If you do not set `VITE_GRAPHQL_URI`, the bundle defaults to `http://localhost:4000/`, which is wrong for a browser when the API is on another host.

**Minikube / kind (load local images):**

```bash
minikube image load streamflix/eureka-server:latest
# ... repeat for each image, or use minikube’s Docker daemon to build
```

## Secrets

Edit `k8s/base/secret.yaml` and replace `POSTGRES_PASSWORD`, `JWT_SECRET`, and Grafana admin credentials before production. Re-apply after changes.

## Deploy

From the **repository root**:

```bash
kubectl kustomize .   # print rendered YAML
kubectl apply -k .      # apply
```

`kubectl apply -f k8s/base` is **not** used; the root `kustomization.yaml` is required for ConfigMap generation and image tags.

## Ingress hosts

`k8s/base/ingress.yaml` uses:

- `app.streamflix.local` → `streamflix-ui` (port 80)
- `api.streamflix.local` → `federation-gateway` (port 4000)

Add these to your hosts file or use real DNS, and set `VITE_GRAPHQL_URI` in the UI image to `http://api.streamflix.local/` (or `https://…` with TLS).

## What is included

- **Data:** PostgreSQL, MongoDB, Redis, and a single KRaft Kafka broker (dev-oriented). For production, prefer managed databases and Kafka, or operators (e.g. Strimzi).
- **Observability:** Tempo, Loki, OpenTelemetry Collector (same `otel-collector/config.yaml` as Docker), Prometheus (scrapes the same in-cluster targets as `prometheus-docker.yml`), Grafana. cAdvisor is **not** included (see `observability-prometheus.yaml` comment); many clusters use other node/container metrics.
- **Apps:** Eureka, all Java subgraphs, Apollo gateway, and the UI.
- **Admin / observability** ports: map Services with `kubectl port-forward` (e.g. Grafana `grafana:3000`, Prometheus `9090`, Eureka `8761`).

## Uninstall

```bash
kubectl delete -k .
```

PersistentVolumeClaims may remain; delete them in the `streamflix` namespace if you want a full cleanup.
