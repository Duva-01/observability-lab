# E-commerce Base para practicar DevOps

Base mínima con 4 microservicios Spring Boot para levantar en Minikube:

- `api-gateway`
- `user-service`
- `product-service`
- `order-service`

La intención de esta primera versión es que tengas un entorno muy simple sobre el que luego podamos añadir:

- observabilidad con Spring Actuator
- métricas con Prometheus
- trazas con OpenTelemetry
- visualización con Grafana y Jaeger

## Qué hace cada servicio

- `user-service`: expone usuarios en memoria.
- `product-service`: expone productos en memoria.
- `order-service`: expone pedidos y llama a `user-service` y `product-service`.
- `api-gateway`: centraliza el acceso a los otros servicios.

Ese flujo `api-gateway -> order-service -> user-service/product-service` es útil porque luego nos permite practicar trazabilidad distribuida de verdad.

## Requisitos

- Java 17
- Maven 3.9+
- Docker
- Minikube
- kubectl

## Estructura

```text
.
|-- api-gateway
|-- apuntes
|-- deploy
|   |-- environments
|   |   `-- minikube
|   `-- helm
|-- user-service
|-- product-service
|-- order-service
`-- scripts
```

## Estructura Helm

Cada microservicio tiene su propio chart Helm:

- `deploy/helm/user-service`
- `deploy/helm/product-service`
- `deploy/helm/order-service`
- `deploy/helm/api-gateway`

Dentro de cada chart tienes:

- `Chart.yaml`: metadatos del chart.
- `values.yaml`: valores por defecto.
- `templates/deployment.yaml`: plantilla del Deployment.
- `templates/service.yaml`: plantilla del Service.

Además, los valores específicos para Minikube están en:

- `deploy/environments/minikube/user-service-values.yaml`
- `deploy/environments/minikube/product-service-values.yaml`
- `deploy/environments/minikube/order-service-values.yaml`
- `deploy/environments/minikube/api-gateway-values.yaml`

## Apuntes

He anadido una carpeta `apuntes/` con documentacion mas desarrollada para estudiar el proyecto con calma.

Empieza por:

- `apuntes/00-indice.md`
- `apuntes/01-microservicios-spring-boot.md`
- `apuntes/02-kubernetes-minikube.md`
- `apuntes/03-helm-y-despliegue.md`
- `apuntes/04-observabilidad-y-prometheus.md`

## Compilar en local

```powershell
mvn clean test
```

## Construir imágenes dentro de Minikube

Antes de desplegar, asegúrate de tener Minikube arrancado:

```powershell
minikube start
```

Después construye las imágenes:

```powershell
.\scripts\build-minikube-images.ps1
```

## Desplegar en Kubernetes con Helm

```powershell
.\scripts\deploy-minikube.ps1
```

Ese script ejecuta `helm upgrade --install` para cada microservicio usando su chart y sus valores de Minikube.

## Probar el gateway

Obtén la URL del servicio:

```powershell
minikube service api-gateway --url
```

Ejemplos de prueba:

```powershell
Invoke-RestMethod http://127.0.0.1:30080/api/overview
Invoke-RestMethod http://127.0.0.1:30080/api/users
Invoke-RestMethod http://127.0.0.1:30080/api/products
Invoke-RestMethod http://127.0.0.1:30080/api/orders
Invoke-RestMethod http://127.0.0.1:30080/api/orders/5001
```

Si `minikube service --url` te devuelve otra URL, usa esa en lugar de `127.0.0.1:30080`.

## Endpoints principales

- `GET /api/overview`
- `GET /api/users`
- `GET /api/users/{id}`
- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/orders`
- `GET /api/orders/{id}`
- `GET /actuator/health`

## Qué no he metido todavía

Para mantener esta fase limpia, no he añadido:

- base de datos
- colas o mensajería
- service discovery
- config server
- Spring Cloud Gateway
- OpenTelemetry
- Prometheus / Grafana / Jaeger

## Siguiente paso recomendado

El siguiente paso con mejor retorno para tu objetivo es instrumentar primero `user-service` y `product-service` con Actuator y Prometheus, y luego meter trazas en `api-gateway` y `order-service`.
