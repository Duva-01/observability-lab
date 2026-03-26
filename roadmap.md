# Roadmap del proyecto `ecommerce-platform`

## Addendum GitLab local

En una fase posterior del laboratorio se incorporÃ³ tambiÃ©n GitLab self-managed dentro del mismo clÃºster Minikube, con el objetivo de cubrir la parte de CI/CD desde una plataforma propia y no depender de un servicio externo.

### Objetivo de esta ampliaciÃ³n

- desplegar un GitLab local dentro del laboratorio
- almacenar el repositorio del proyecto en ese GitLab
- preparar el entorno para ejecutar pipelines sobre el propio stack
- ampliar el proyecto desde observabilidad hacia plataforma DevOps

### ConfiguraciÃ³n incorporada al repo

Se aÃ±adieron estos ficheros:

- [values-minikube.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/gitlab/values-minikube.yaml)
- [values-local.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/gitlab/values-local.yaml)

La configuraciÃ³n local se orientÃ³ a:

- `edition: ce`
- dominio basado en `nip.io`
- desactivar el Grafana interno del chart
- desactivar el Prometheus interno del chart
- aplazar la instalaciÃ³n del runner del chart para una fase posterior

### InstalaciÃ³n realizada

```powershell
kubectl create namespace gitlab
helm upgrade --install gitlab gitlab/gitlab `
  --namespace gitlab `
  --create-namespace `
  --timeout 1200s `
  -f .\deploy\gitlab\values-minikube.yaml `
  -f .\deploy\gitlab\values-local.yaml
```

La instalaciÃ³n devolviÃ³ el release en estado `deployed`, aunque el servicio no estuvo listo de forma inmediata.

### Problemas reales encontrados

- GitLab no estuvo accesible inmediatamente tras el `helm install`
- varios pods crÃ­ticos tardaron bastante en quedar `Running`
- la URL basada en la IP directa de Minikube no era accesible desde Windows en este setup con driver Docker
- fue necesario usar `port-forward` del `ingress-nginx-controller`
- fue necesario aÃ±adir una entrada al fichero `hosts` de Windows
- el acceso final se realizÃ³ con HTTPS y certificado no confiable de laboratorio
- Git no pudo empujar al primer intento por el certificado self-signed y por el flujo de credenciales de Git Credential Manager

### SoluciÃ³n de acceso adoptada

Se terminÃ³ accediendo mediante:

- `port-forward` del ingress controller
- entrada local en `hosts`
- URL final:

```text
https://gitlab.192.168.49.2.nip.io:8443
```

con esta entrada:

```text
127.0.0.1 gitlab.192.168.49.2.nip.io
```

### AutomatizaciÃ³n auxiliar

Se ampliÃ³ el script de port-forwards para dar soporte tambiÃ©n a GitLab:

- [port-forward-services.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/port-forward-services.ps1)

Se aÃ±adieron forwards de ingress para:

- `http://gitlab.192.168.49.2.nip.io:8088`
- `https://gitlab.192.168.49.2.nip.io:8443`

### Resultado conseguido

Al final de esta fase se consiguiÃ³ acceder a GitLab CE desde navegador, autenticar como `root`, crear el proyecto `Observability-Project`, inicializar Git en el proyecto local y subir el primer commit del repositorio al GitLab local.

### Siguiente paso recomendado desde este punto

El siguiente paso con mÃ¡s valor ya no es aÃ±adir mÃ¡s componentes, sino conectar un runner al GitLab local y hacer funcionar la pipeline bÃ¡sica del proyecto con `build`, `test`, `package` y validaciÃ³n Helm.

### Fase siguiente completada: GitLab Runner en Kubernetes

Tras dejar GitLab accesible, se completó también la incorporación del runner dentro del propio clúster.

#### Objetivo

- evitar depender de un runner instalado en Windows
- mantener GitLab y ejecución de pipelines dentro del mismo laboratorio
- preparar una CI real sobre Kubernetes

#### Decisión técnica

Se eligió:

- mantener GitLab como servidor CI/CD
- desplegar `gitlab-runner` dentro de Kubernetes mediante Helm
- conectar el runner a GitLab usando la URL interna del servicio web de GitLab

La URL elegida para el runner fue:

```text
http://gitlab-webservice-default.gitlab.svc.cluster.local:8181/
```

Esto evitó depender de:

- `port-forward`
- `hosts` de Windows
- certificados self-signed para el tráfico interno runner -> GitLab

#### Runner creado en GitLab

Se creó en la UI un runner de proyecto:

- descripción: `k8s-minikube-runner`
- asociado al proyecto `Observability-Project`
- configurado para recoger jobs sin tags

Durante esta fase se detectó una limitación práctica:

- la UI de GitLab redirigía a URLs sin `:8443`
- esto impedía navegar correctamente por algunas pantallas del runner desde navegador
- el token de autenticación del runner había que copiarlo en el momento de creación porque luego no era recuperable desde la UI

#### Ficheros incorporados

Se añadió:

- [values.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/gitlab-runner/values.yaml)

#### Configuración del runner

La configuración del chart se orientó a:

- `gitlabUrl` apuntando al servicio interno de GitLab
- `runnerToken` con el token `glrt-...` del runner creado en la UI
- `rbac.create: true`
- `concurrent: 2`
- `checkInterval: 30`
- `privileged = true` en el executor Kubernetes

El modo privilegiado se dejó activado para facilitar:

- `docker:dind`
- tests con Testcontainers
- ejecución de jobs más cercanos a la `.gitlab-ci.yml` actual

#### Instalación realizada

```powershell
kubectl create namespace gitlab-runner
helm repo add gitlab https://charts.gitlab.io
helm repo update
helm upgrade --install gitlab-runner gitlab/gitlab-runner `
  --namespace gitlab-runner `
  --create-namespace `
  -f .\deploy\gitlab-runner\values.yaml
```

#### Comprobaciones realizadas

Se verificó:

- registro correcto del runner contra GitLab
- persistencia de la configuración del runner
- arranque del deployment del runner
- pod del runner en estado `1/1 Running`
- `readinessProbe` superada tras el arranque inicial

Los logs mostraron mensajes clave como:

- `Runner registered successfully`
- `Configuration ... was saved`
- `Starting multi-runner`
- `Initializing executor providers`

#### Interpretación de los warnings

Durante el arranque aparecieron avisos como:

- `Running in user-mode`
- `Long polling issues detected`
- `listen_address not defined`

Estos avisos no bloquearon la operación del runner y se consideraron aceptables para el laboratorio actual.

#### Estado alcanzado al final de esta fase

Al final de esta fase se consiguió:

- tener GitLab Runner desplegado en Kubernetes
- runner registrado correctamente contra el GitLab local
- pod listo para recoger jobs
- infraestructura preparada para lanzar la primera pipeline CI

#### Siguiente paso natural

El siguiente paso ya no es de instalación, sino de validación funcional:

1. lanzar la primera pipeline del proyecto
2. observar qué jobs pasan y cuáles fallan
3. ajustar la `.gitlab-ci.yml` según el comportamiento real del runner
4. decidir después si se añade también un job de despliegue hacia Minikube

## 1. Objetivo del proyecto

Este proyecto nace como laboratorio práctico para estudiar:

- microservicios con Spring Boot
- despliegue en Kubernetes con Minikube
- empaquetado y parametrización con Helm
- observabilidad completa de métricas, trazas y logs
- automatización básica con scripts
- patrones cercanos a un puesto orientado a observabilidad / plataforma

La intención no ha sido solo "levantar micros", sino construir progresivamente una plataforma pequeña pero realista sobre la que practicar:

- instrumentación de aplicaciones
- despliegue reproducible
- troubleshooting
- diseño de dashboards
- recolección de métricas, trazas y logs
- operación de componentes de observabilidad en Kubernetes

---

## 2. Visión general actual

Estado funcional actual:

- 4 microservicios Spring Boot:
  - `api-gateway`
  - `user-service`
  - `product-service`
  - `order-service`
- 1 librería compartida:
  - `essentials-lib`
- despliegue en Minikube con Helm
- PostgreSQL desplegado con Helm y persistencia
- separación por esquemas por microservicio
- Flyway por servicio
- Prometheus operativo
- Grafana operativa con datasource provisionado y dashboards autoimportados
- OpenTelemetry Collector operativo
- Jaeger operativo para trazas
- `kube-state-metrics` operativo
- `node-exporter` operativo
- `metrics-server` habilitado en Minikube
- Elasticsearch + Kibana + Filebeat operativos para logs
- logs de aplicación estructurados en ECS JSON

---

## 3. Estructura del repositorio

Elementos principales del repo:

- `api-gateway`
- `user-service`
- `product-service`
- `order-service`
- `essentials-lib`
- `deploy`
- `scripts`
- `apuntes`

### 3.1 Módulos Maven

Definidos en [pom.xml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/pom.xml):

- `essentials-lib`
- `api-gateway`
- `user-service`
- `order-service`
- `product-service`

### 3.2 Despliegue

En `deploy/` existen dos áreas principales:

- `deploy/helm`
- `deploy/observability`

### 3.3 Scripts

En `scripts/` existen automatizaciones para:

- build de imágenes para Minikube
- despliegue con Helm
- despliegue de observabilidad
- instalación de ECK
- port-forward local
- pruebas básicas de endpoints y métricas

---

## 4. Fase 1: Base inicial de microservicios

### 4.1 Punto de partida

El proyecto empezó como una base sencilla de 4 microservicios para practicar DevOps y observabilidad:

- `user-service`: expone usuarios
- `product-service`: expone productos
- `order-service`: expone pedidos y consume `user-service` y `product-service`
- `api-gateway`: centraliza el acceso

El flujo principal que se ha usado como base del proyecto es:

`api-gateway -> order-service -> user-service / product-service`

Ese flujo se eligió porque permite practicar:

- trazabilidad distribuida
- correlación de logs
- métricas por servicio
- observabilidad de dependencias

### 4.2 Evolución de arquitectura interna

Posteriormente los microservicios se refactorizaron a una estructura más seria por capas:

- `api`
- `service`
- `service/impl`
- `repository`
- `model`

También se introdujeron:

- interfaces de servicio
- implementaciones separadas
- DTOs generados desde OpenAPI
- mejor separación de responsabilidades

Resultado:

- el backend quedó más cercano a una arquitectura empresarial real
- la base ya no era solo una demo simple, sino una plataforma útil para seguir iterando

---

## 5. Fase 2: Persistencia real con PostgreSQL y Flyway

### 5.1 Cambio desde datos en memoria

En una fase posterior se dejó atrás el modelo puramente en memoria y se introdujo persistencia real con PostgreSQL.

### 5.2 Qué se montó

- chart Helm propio para PostgreSQL en `deploy/helm/postgres`
- PVC para persistencia
- `Secret` para credenciales
- `Service` para exponer la base de datos dentro del clúster

Archivos clave:

- [deployment.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/helm/postgres/templates/deployment.yaml)
- [persistentvolumeclaim.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/helm/postgres/templates/persistentvolumeclaim.yaml)
- [service.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/helm/postgres/templates/service.yaml)
- [secret.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/helm/postgres/templates/secret.yaml)

### 5.3 Decisiones de diseño

- una única instancia PostgreSQL para el laboratorio
- separación por esquemas por microservicio
- Flyway gestionando migraciones en cada servicio

### 5.4 Incidencias resueltas

Durante la evolución del proyecto apareció una incidencia importante:

- había cambios en migraciones ya aplicadas
- Flyway marcaba incompatibilidades con el histórico
- se tuvo que reparar el histórico local para que el rollout volviera a arrancar correctamente

También se ajustaron pruebas para evitar problemas con H2:

- tests de datos pasaron a usar PostgreSQL efímero con Testcontainers

Resultado:

- persistencia real funcional
- migraciones versionadas
- pruebas más alineadas con el entorno real

---

## 6. Fase 3: Kubernetes + Helm

### 6.1 Objetivo

Se empaquetó cada microservicio en su propio chart Helm para conseguir:

- despliegues repetibles
- parametrización por entorno
- separación entre manifiesto base y valores de Minikube

### 6.2 Charts existentes

En `deploy/helm`:

- `api-gateway`
- `user-service`
- `product-service`
- `order-service`
- `postgres`

Cada chart contiene:

- `Chart.yaml`
- `values.yaml`
- `templates/deployment.yaml`
- `templates/service.yaml`

Valores específicos de Minikube:

- `deploy/environments/minikube/api-gateway-values.yaml`
- `deploy/environments/minikube/user-service-values.yaml`
- `deploy/environments/minikube/product-service-values.yaml`
- `deploy/environments/minikube/order-service-values.yaml`
- `deploy/environments/minikube/postgres-values.yaml`

### 6.3 Script de build

Se creó [build-minikube-images.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/build-minikube-images.ps1) para construir imágenes directamente dentro de Minikube con:

- `minikube image build`

Esto evita depender de un registry externo en el laboratorio.

### 6.4 Script de despliegue

Se creó [deploy-minikube.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/deploy-minikube.ps1) para:

- instalar / actualizar PostgreSQL con Helm
- instalar / actualizar los 4 microservicios con Helm
- reiniciar deployments
- esperar rollout
- mostrar diagnósticos si algo falla

### 6.5 Validaciones

Se verificó el correcto despliegue y funcionamiento mediante:

- `kubectl get pods`
- `kubectl get svc`
- llamadas al gateway
- comprobación de endpoints de negocio

CRUD verificado en el proyecto:

- `GET /api/orders/5001`
- `GET /api/users`
- `GET /api/products`
- `POST /api/users`
- `DELETE /api/users/{id}`

---

## 7. Fase 4: Instrumentación base y observabilidad inicial

### 7.1 Micrometer / Actuator

Se introdujo instrumentación con:

- Spring Boot Actuator
- Micrometer
- endpoint `/actuator/prometheus`

### 7.2 OpenTelemetry

Se instrumentó el sistema con OpenTelemetry para trazas distribuidas.

Se definieron atributos como:

- `service.name`
- `service.namespace`
- `deployment.environment`

### 7.3 OTel Collector y Jaeger

Se desplegaron:

- OpenTelemetry Collector
- Jaeger

Archivos clave:

- [configmap.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/otel-collector/configmap.yaml)
- [deployment.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/otel-collector/deployment.yaml)
- [deployment.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/jaeger/deployment.yaml)

Resultado:

- trazas end-to-end a través del flujo entre microservicios

---

## 8. Fase 5: Prometheus y Grafana

### 8.1 Prometheus

Se desplegó Prometheus con manifiestos propios:

- namespace de observabilidad
- deployment
- service
- configmap de scraping

Archivos clave:

- [namespace.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/prometheus/namespace.yaml)
- [deployment.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/prometheus/deployment.yaml)
- [configmap.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/prometheus/configmap.yaml)

### 8.2 Grafana

Se desplegó Grafana con:

- datasource Prometheus provisionado
- provisioning de dashboards
- dashboard custom de control de plataforma

Archivos clave:

- [configmap-datasource.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/grafana/configmap-datasource.yaml)
- [configmap-dashboard-providers.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/grafana/configmap-dashboard-providers.yaml)
- [configmap-dashboards.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/grafana/configmap-dashboards.yaml)
- [deployment.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/grafana/deployment.yaml)

### 8.3 Dashboard custom creado

Se creó un dashboard propio con métricas como:

- servicios UP
- número de targets activos
- series activas en Prometheus
- requests por segundo
- errores 5xx
- latencia media por servicio
- heap JVM por servicio
- duración de scrape

### 8.4 Problema detectado con dashboards importados

Al importar dashboards de Grafana.com aparecía el error:

- `Datasource ${DS_PROMETHEUS} was not found`

Diagnóstico realizado:

- no era un problema de Minikube
- no era un problema del `port-forward`
- el problema estaba en el JSON importado del dashboard
- los dashboards traían placeholders tipo `${DS_PROMETHEUS}` que no quedaban resueltos correctamente

Solución y aprendizaje:

- usar un `uid` estable para el datasource Prometheus
- adaptar los dashboards importados para que apunten al `uid` real
- en varios casos se explicó cómo corregir manualmente `panels[].datasource`, `targets[].datasource` y variables

### 8.5 Métricas de Kubernetes

Se decidió ampliar la observabilidad del clúster y no quedarse solo con la app.

Se añadió:

- `metrics-server` como addon de Minikube
- `kube-state-metrics`
- `node-exporter`

Archivos añadidos:

- [values.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/kube-state-metrics/values.yaml)
- [values.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/node-exporter/values.yaml)

Script adaptado:

- [deploy-observability.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/deploy-observability.ps1)

Incidencia resuelta con `metrics-server`:

- durante el arranque aparecía `Metrics API not available`
- no era un error definitivo
- simplemente necesitaba terminar el arranque y el primer ciclo de recogida de métricas

Resultado:

- `kubectl top nodes`
- `kubectl top pods -A`
- métricas `kube_*`
- métricas `node_*`

---

## 9. Fase 6: Logging centralizado con Elastic

### 9.1 Motivación

Se decidió introducir logs centralizados porque el objetivo del proyecto se alinea con un rol orientado a:

- observabilidad
- backends tipo Elastic
- logging
- tracing
- dashboards
- alertado

### 9.2 Elección del stack

Se optó por:

- ECK como operador
- Elasticsearch
- Kibana
- Filebeat

En vez de cambiar de stack en ese momento a OpenSearch.

### 9.3 Instalación de ECK

Para dejar constancia en el repo y fijar versión del operador:

- se creó [kustomization.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/elastic/eck/kustomization.yaml)
- se creó [install-eck.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/install-eck.ps1)

La instalación quedó reproducible mediante:

- `kubectl apply -k .\deploy\observability\elastic\eck`

### 9.4 Recursos de Elastic creados

En `deploy/observability/elastic`:

- [elasticsearch.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/elastic/elasticsearch.yaml)
- [kibana.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/elastic/kibana.yaml)
- [filebeat.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/elastic/filebeat.yaml)

### 9.5 Incidencias de Kibana

Problema detectado:

- `port-forward` correcto
- navegador devolvía `ERR_EMPTY_RESPONSE`

Diagnóstico:

- Kibana bajo ECK estaba servido por HTTPS/TLS
- se estaba intentando entrar por `http://127.0.0.1:5601`

Solución:

- usar `https://127.0.0.1:5601`
- aceptar el certificado autofirmado

### 9.6 Incidencias de Filebeat

Problema detectado:

- pod de Filebeat en `Error`

Diagnóstico:

- `Filebeat` intentaba conectar con Kibana por el bloque `kibanaRef`
- esa parte era innecesaria para el objetivo principal, que era mandar logs a Elasticsearch

Solución:

- eliminar `kibanaRef` del manifiesto

Resultado:

- Filebeat estable
- creación del data stream `filebeat-*`
- logs visibles en Kibana

### 9.7 Problema funcional detectado en logs

Inicialmente Kibana no mostraba claramente logs de negocio de los microservicios.

Diagnóstico:

- los micros apenas generaban logs útiles
- muchos logs visibles eran de infraestructura
- la validación por `Available fields` o por `kubernetes.namespace` no era suficiente

Aprendizaje:

- no bastaba con "tener Elastic"
- había que mejorar la calidad y estructura de los logs emitidos por la aplicación

---

## 10. Fase 7: Logging de negocio y logs estructurados ECS

### 10.1 Logging de negocio con Lombok

Se añadieron logs de negocio con `Lombok @Slf4j` en puntos clave:

- llamadas del gateway
- operaciones CRUD
- consumo downstream
- eventos relevantes en `order-service`

Ficheros afectados:

- `GatewayServiceImpl`
- `UserServiceImpl`
- `ProductServiceImpl`
- `OrderServiceImpl`

Resultado:

- empezaron a aparecer mensajes útiles como:
  - `Forwarding GET request...`
  - `Retrieved order with id...`
  - `Retrieved user with id...`
  - `Retrieved product with id...`

### 10.2 Motivo para pasar a JSON

Aunque esos logs ya eran mejores, seguían siendo texto plano.

Limitaciones del texto plano:

- Kibana no extrae bien `service.name`
- Kibana no extrae bien `log.level`
- es más difícil filtrar por microservicio
- es más difícil correlacionar con trazas

Decisión tomada:

- migrar a logs estructurados en ECS JSON

### 10.3 Implementación ECS JSON

Se añadió la dependencia:

- `co.elastic.logging:logback-ecs-encoder`

Se añadió la propiedad:

- `ecs.logging.version=1.7.0`

Se crearon ficheros `logback-spring.xml` en:

- `api-gateway`
- `user-service`
- `product-service`
- `order-service`

Cada servicio empezó a escribir a `stdout` con campos ECS como:

- `@timestamp`
- `service.name`
- `service.environment`
- `log.level`
- `log.logger`
- `message`
- `trace_id`
- `span_id`

### 10.4 Adaptación de Filebeat

Se cambió [filebeat.yaml](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/deploy/observability/elastic/filebeat.yaml) para:

- detectar mensajes JSON en `message`
- decodificarlos con `decode_json_fields`
- expandir claves a campos reales
- mantener además el enriquecimiento con metadatos de Kubernetes

### 10.5 Verificación final

Tras reconstruir imágenes, redeployar y reaplicar Filebeat:

- los pods emitían JSON ECS real
- Elasticsearch indexaba campos estructurados
- Kibana empezó a mostrar en `Available fields` elementos como:
  - `service.name`
  - `service.environment`
  - `log.level`
  - `log.logger`

Validación visual conseguida:

- filtro por `service.name`
- filtro por `log.level`
- mensajes del flujo entre `api-gateway`, `order-service`, `user-service` y `product-service`
- presencia de `trace_id` y `span_id` en los eventos de aplicación

### 10.6 Correlación entre Kibana y Jaeger

Tras estructurar los logs y exponer `trace_id`, se validó la correlación entre logs y trazas.

Flujo operativo validado:

1. localizar un evento de aplicación en Kibana
2. copiar el campo `trace_id`
3. abrir Jaeger
4. usar `Lookup by Trace ID`
5. acceder a la traza exacta asociada al log

Aprendizajes relevantes:

- el campo `Tags` de Jaeger no sirve para pegar un `trace_id` directamente
- la forma correcta de buscar una traza concreta es `Lookup by Trace ID`
- también funciona el acceso directo por URL `/trace/<trace_id>`

Esto ha sido uno de los hitos más importantes del proyecto, porque convierte el stack de logs en algo realmente explotable.

---

## 10 bis. Fase 8: Alertas de métricas y logs

### 10 bis.1 Alerta de métricas en Grafana

Se implementó y validó una primera alerta operativa en Grafana:

- `MicroserviceDown`

Objetivo:

- detectar si alguno de los microservicios deja de responder a Prometheus

Configuración validada:

```promql
up{job=~"api-gateway|user-service|product-service|order-service"}
```

Condición:

- `last() is below 1`

Validación realizada:

- se escaló `user-service` a `0` réplicas
- Prometheus mostró el target con `up=0`
- Grafana pasó la alerta a estado `Firing`

Aprendizaje:

- una primera versión de la regla con `== 0` y condición `above 0` no era correcta
- la lógica buena consistía en evaluar la serie `up` directamente y comprobar si bajaba de `1`

### 10 bis.2 Alerta de logs en Kibana

Se implementó y validó una regla basada en Elasticsearch:

- `ErrorLogsByService`

Objetivo:

- detectar errores de aplicación a partir de logs estructurados

Configuración validada:

- data view: `filebeat-*`
- query:

```text
log.level : "ERROR" and service.name : ("api-gateway" or "user-service" or "product-service" or "order-service")
```

- ventana temporal: `last 5 minutes`
- condición: `count() is above 0`
- frecuencia: `every 1 minute`

Validación realizada:

- se provocó un fallo real apagando temporalmente un microservicio dependiente
- se generaron logs `ERROR`
- Discover mostró los documentos esperados
- la regla pasó a estado `Active`
- el historial de la regla mostró ejecuciones con `Active alerts = 1`

Valor conseguido:

- el proyecto ya dispone de alertado tanto por métricas como por logs
- se puede demostrar un circuito completo de observabilidad:
  - fallo en un microservicio
  - log `ERROR` en Kibana
  - alerta activa en Kibana
  - trazabilidad a Jaeger mediante `trace_id`

---

## 11. Scripts operativos relevantes

### 11.1 Build

[build-minikube-images.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/build-minikube-images.ps1)

Sirve para:

- construir las imágenes Docker directamente en Minikube

### 11.2 Deploy de aplicación

[deploy-minikube.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/deploy-minikube.ps1)

Sirve para:

- desplegar PostgreSQL
- desplegar los microservicios
- forzar restart de deployments
- esperar rollouts
- obtener diagnóstico si algo falla

### 11.3 Deploy de observabilidad

[deploy-observability.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/deploy-observability.ps1)

Sirve para:

- aplicar manifiestos de observabilidad base
- instalar o actualizar `kube-state-metrics`
- instalar o actualizar `node-exporter`
- reiniciar componentes
- esperar rollouts

### 11.4 Instalación de ECK

[install-eck.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/install-eck.ps1)

Sirve para:

- instalar el operador de Elastic mediante `kustomization.yaml`

### 11.5 Port-forward

[port-forward-services.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/port-forward-services.ps1)

Sirve para:

- abrir port-forwards locales
- mantener estado
- arrancar / parar / comprobar forwards

### 11.6 Test de servicios

[test-services.ps1](C:/Users/Usuario/Desktop/Personal/Proyectos/Devops/Mercadona%20Project/scripts/test-services.ps1)

Sirve para:

- validar endpoints
- validar `health`
- validar formato Prometheus

---

## 12. Principales problemas encontrados y resueltos

Resumen de incidentes relevantes del proyecto:

- Flyway bloqueado por histórico de migraciones cambiado
- tests incompatibles con H2 y migraciones reales
- dashboards importados de Grafana con `${DS_PROMETHEUS}`
- `metrics-server` no listo al primer momento
- Kibana inaccesible por usar HTTP en vez de HTTPS
- Filebeat fallando por `kibanaRef`
- logs no útiles por falta de logging de negocio
- logs no filtrables por ser texto plano
- necesidad de estructurar logs en ECS JSON
- primera lógica de alerta en Grafana mal planteada y corregida
- necesidad de distinguir entre pantalla de reglas y pantalla de alertas en Kibana
- uso correcto de `Lookup by Trace ID` en Jaeger frente a búsquedas por tags

Estos puntos son muy valiosos de cara a un informe o a una entrevista, porque muestran trabajo real de:

- troubleshooting
- diagnóstico
- corrección progresiva
- mejora iterativa del diseño

---

## 13. Estado actual del proyecto

Actualmente el proyecto ya permite demostrar:

- diseño y despliegue de microservicios Spring Boot
- despliegue en Kubernetes con Helm
- PostgreSQL persistente con Flyway
- métricas de aplicación con Micrometer y Prometheus
- dashboards en Grafana
- trazas distribuidas con OTel + Jaeger
- métricas de clúster con `kube-state-metrics`, `node-exporter` y `metrics-server`
- logs centralizados con Filebeat + Elasticsearch + Kibana
- logs estructurados por microservicio con nivel y contexto
- correlación práctica entre logs y trazas mediante `trace_id`
- una alerta de métricas funcional en Grafana
- una alerta de logs funcional en Kibana

En este punto el proyecto ya sirve como base seria para:

- un informe técnico detallado
- una demo funcional
- una entrevista orientada a observabilidad / plataforma

---

## 14. Próximos pasos recomendados

Siguientes líneas de evolución con más valor:

- paneles específicos por servicio y por error
- versionado de reglas y alertas como código
- automatización operativa adicional sobre el stack actual
- pipeline CI/CD
- posible integración futura de RUM o recolección más avanzada

Orden recomendado:

1. crear dashboards de errores por servicio
2. decidir si se quieren versionar reglas y alertas en el repo
3. añadir CI/CD
4. ampliar automatización y operación del stack
5. valorar integraciones adicionales según el foco del informe

Nota:

- Spring Batch se consideró como posible ampliación porque aparecía de forma explícita en la oferta objetivo
- no es imprescindible para cerrar bien este proyecto
- si ya existe experiencia previa con Spring Batch, aporta más valor seguir profundizando en observabilidad, alertado, dashboards y automatización sobre la plataforma actual

---

## 15. Valor del proyecto para el objetivo profesional

Este proyecto ya cubre una parte importante del perfil buscado en un puesto orientado a observabilidad:

- Spring Boot
- Micrometer
- Grafana
- Prometheus
- Elastic
- diseño de dashboards
- logging
- tracing
- OpenTelemetry
- Kubernetes
- Helm
- contenedores
- automatización con scripts

Lo más importante es que no se ha quedado en teoría:

- se han desplegado componentes reales
- se han resuelto fallos reales
- se ha iterado desde una base simple hasta una plataforma observable mucho más madura

---

## 16. Nota para el informe final

Este archivo debe servir como guía base para redactar después un informe más largo con:

- contexto
- arquitectura
- decisiones técnicas
- problemas encontrados
- soluciones aplicadas
- estado final
- líneas futuras

Conviene que el informe final incluya también capturas de:

- pods y servicios en Kubernetes
- dashboards de Grafana
- trazas en Jaeger
- Discover en Kibana con `service.name` y `log.level`
- ejemplos de logs estructurados y métricas
