# Inventario recomendado de capturas

Esta carpeta debe almacenar las capturas y diagramas que acompañarán la memoria principal `docs/observability-lab-project.md`.

## Convención recomendada

- usar nombres cortos y estables
- evitar espacios
- preferir formato `png`
- numerar por bloque temático

## Capturas sugeridas

### Arquitectura

- `01-arquitectura-general.png`
- `02-estructura-repo.png`

### Kubernetes

- `10-kubectl-get-pods-a.png`
- `11-kubectl-get-svc.png`
- `12-rollout-ok.png`

### Grafana y Prometheus

- `20-grafana-dashboard-general.png`
- `21-grafana-alerta-microservicedown.png`
- `22-prometheus-targets.png`

### Jaeger

- `30-jaeger-trace-overview.png`
- `31-jaeger-trace-by-id.png`

### Elastic y Kibana

- `40-kibana-discover-logs.png`
- `41-kibana-service-name-log-level.png`
- `42-kibana-regla-activa.png`

### GitLab

- `50-gitlab-home-project.png`
- `51-gitlab-runner-online.png`
- `52-gitlab-pipeline-ci-green.png`
- `53-gitlab-pipeline-deploy-green.png`
- `54-gitlab-container-registry.png`

### CD real

- `60-k8s-image-by-sha-user-service.png`
- `61-k8s-image-by-sha-api-gateway.png`

## Relación rápida entre figura y capítulo

| Figura | Capítulo recomendado |
|---|---|
| `01-arquitectura-general.png` | Arquitectura general del laboratorio |
| `10-kubectl-get-pods-a.png` | Persistencia y despliegue base |
| `11-kubectl-get-svc.png` | Helm y exposición de servicios |
| `20-grafana-dashboard-general.png` | Métricas con Prometheus y Grafana |
| `21-grafana-alerta-microservicedown.png` | Alertado de métricas |
| `30-jaeger-trace-overview.png` | OpenTelemetry y trazas |
| `31-jaeger-trace-by-id.png` | Correlación logs-trazas |
| `40-kibana-discover-logs.png` | Logging centralizado |
| `41-kibana-service-name-log-level.png` | ECS JSON y explotación de logs |
| `42-kibana-regla-activa.png` | Alertado por logs |
| `50-gitlab-home-project.png` | GitLab self-managed |
| `51-gitlab-runner-online.png` | GitLab Runner en Kubernetes |
| `52-gitlab-pipeline-ci-green.png` | CI en GitLab |
| `53-gitlab-pipeline-deploy-green.png` | CD real |
| `54-gitlab-container-registry.png` | Registry privado |
| `60-k8s-image-by-sha-user-service.png` | Despliegue por SHA |

