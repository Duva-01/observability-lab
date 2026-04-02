# observability-lab

Laboratorio de microservicios con Spring Boot orientado a observabilidad, Kubernetes y CI/CD.

## Qué incluye

- 4 microservicios Spring Boot: `api-gateway`, `user-service`, `product-service`, `order-service`
- PostgreSQL para persistencia
- Helm para despliegue en Kubernetes / Minikube
- Métricas con Actuator, Micrometer, Prometheus y Grafana
- Trazas con OpenTelemetry, OpenTelemetry Collector y Jaeger
- Logs estructurados con ECS, Filebeat, Elasticsearch y Kibana
- GitLab CE self-managed, GitLab Runner y Container Registry
- Pipeline CI/CD con build, test, package, validación Helm, build de imágenes y deploy
- Automatización auxiliar con Ansible

## Requisitos

- Java 17
- Maven 3.9+
- Docker
- Minikube
- kubectl
- Helm

## Estructura principal

```text
.
|-- api-gateway
|-- user-service
|-- product-service
|-- order-service
|-- essentials-lib
|-- deploy
|-- scripts
|-- docs
`-- ansible
```

## Comandos útiles

Compilar y testear:

```powershell
mvn clean test
```

Desplegar observabilidad:

```powershell
.\scripts\deploy-observability.ps1
```

Desplegar microservicios en Minikube:

```powershell
.\scripts\deploy-minikube.ps1
```

Levantar port-forwards:

```powershell
.\scripts\port-forward-services.ps1 -Action start
```

## Documentación

La memoria técnica y sus recursos están en:

- `docs/observability-lab-project.latex.tex`
- `docs/assets/`

## Objetivo

El objetivo del proyecto es construir una plataforma local reproducible para practicar diseño, instrumentación y operación de una solución integral de observabilidad sobre microservicios cloud native.
