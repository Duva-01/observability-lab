# Laboratorio de Observabilidad y CI/CD para Microservicios con Kubernetes y GitLab

## Portada

**Título del proyecto**  
Laboratorio de Observabilidad y CI/CD para Microservicios con Kubernetes y GitLab

**Tipo de documento**  
Memoria técnica extendida con enfoque didáctico

**Autoría**  
Proyecto personal de ingeniería orientado a observabilidad, plataforma y automatización DevOps

**Entorno de ejecución**  
Windows, Docker, Minikube, Kubernetes, Helm y GitLab self-managed

**Estado del documento**  
Versión extensa de trabajo, preparada para evolucionar hacia PDF final de entrega

**Propósito del documento**  
Explicar de forma progresiva, técnica y pedagógica cómo se diseñó, desplegó, observó y automatizó el laboratorio, dejando suficiente detalle como para que un perfil junior pueda estudiar no solo el resultado, sino también el razonamiento detrás de cada decisión.

> Nota editorial: este documento está escrito para cumplir un doble objetivo.  
> Por un lado, funcionar como memoria seria de proyecto. Por otro, servir como guía de aprendizaje para entender cómo se conectan Spring Boot, Kubernetes, Helm, OpenTelemetry, Prometheus, Grafana, Elasticsearch, Kibana y GitLab en un laboratorio local realista.

---

## Índice detallado

1. Introducción  
2. Objetivos del proyecto  
3. Glosario técnico mínimo  
4. Alcance y límites del laboratorio  
5. Arquitectura general del laboratorio  
6. Estructura del repositorio  
7. Construcción de la base de microservicios  
8. Persistencia real con PostgreSQL y Flyway  
9. Despliegue en Kubernetes con Helm  
10. Observabilidad de métricas y trazas  
11. Métricas de plataforma con Prometheus y Grafana  
12. Logging centralizado y logs estructurados  
13. Alertado por métricas y por logs  
14. GitLab self-managed dentro del laboratorio  
15. GitLab Runner en Kubernetes  
16. Pipeline CI con GitLab  
17. CD real con Container Registry y deploy por SHA  
18. Troubleshooting real: problemas y correcciones  
19. Resultados finales del laboratorio  
20. Decisiones de diseño y trade-offs  
21. Diferencias entre este laboratorio y un entorno de producción  
22. Troubleshooting resumido en formato operativo  
23. Valor del proyecto para un perfil junior DevOps u observabilidad  
24. Conclusiones  
25. Líneas futuras  
26. Recomendaciones para la versión final del documento  
27. Anexo A: comandos representativos  
28. Anexo B: mensaje final del proyecto

### Cómo leer esta memoria

El documento se puede recorrer de dos maneras:

- lectura técnica completa, de principio a fin, si se quiere entender el laboratorio como proyecto de ingeniería
- lectura selectiva por bloques, si se quiere profundizar solo en observabilidad, solo en GitLab CI/CD o solo en troubleshooting

Para un perfil junior, el orden recomendado es:

1. arquitectura general  
2. microservicios y persistencia  
3. observabilidad  
4. logging  
5. GitLab y CI/CD  
6. troubleshooting real  

---

## Resumen

Este documento describe el diseño, la implementación y la evolución de un laboratorio técnico orientado a microservicios, observabilidad y automatización DevOps. El proyecto parte de una base de cuatro microservicios Spring Boot y evoluciona hasta convertirse en una plataforma pequeña, pero realista, que integra despliegue en Kubernetes, persistencia con PostgreSQL, observabilidad completa de métricas, trazas y logs, alertado y una cadena CI/CD funcional con GitLab self-managed, GitLab Runner en Kubernetes y GitLab Container Registry.

El valor del laboratorio no está únicamente en haber desplegado herramientas, sino en haber resuelto problemas reales de operación: errores de migraciones con Flyway, dashboards mal parametrizados, logs poco útiles, correlación entre logs y trazas, problemas de permisos y autenticación en Kubernetes, y fallos internos de GitLab relacionados con Gitaly y con la autenticación del registry. Todo ello convierte el proyecto en una base sélida tanto para aprendizaje como para defensa técnica ante un puesto orientado a observabilidad o plataforma.

---

## 1. Introducción

En muchos proyectos de backend y plataforma aparece un problema recurrente: construir software es relativamente sencillo, pero operarlo correctamente es otra historia. Tener varios microservicios desplegados no garantiza poder observar el sistema, diagnosticar fallos, seguir una petición de extremo a extremo o automatizar despliegues con trazabilidad suficiente.

Este laboratorio nace precisamente para cubrir esa distancia entre una demo simple y una plataforma más madura. La idea no fue quedarse en una colección de APIs, sino crear un entorno donde se pudieran practicar de forma integrada las capacidades que suelen exigirse en roles cercanos a observabilidad, DevOps y plataforma:

- diseño y despliegue de microservicios
- orquestación con Kubernetes
- empaquetado con Helm
- persistencia con PostgreSQL y migraciones
- métricas con Prometheus
- visualización con Grafana
- trazas distribuidas con OpenTelemetry y Jaeger
- logs centralizados con Elasticsearch, Kibana y Filebeat
- CI/CD real con GitLab, Runner y Container Registry

El resultado final es un laboratorio reproducible, con problemas reales resueltos, que puede enseñarse como proyecto técnico, como base para una memoria estilo TFG o como material de aprendizaje para perfiles junior.

---

## 2. Objetivos del proyecto

### 2.1 Objetivo general

Diseñar e implementar un laboratorio reproducible de microservicios observables sobre Kubernetes, incluyendo despliegue, persistencia, métricas, trazas, logs, alertado y una cadena CI/CD completa con GitLab self-managed.

### 2.2 Objetivos específicos

- Construir una base de microservicios Spring Boot con dependencias reales entre servicios.
- Desplegar la aplicación sobre Minikube mediante Helm.
- Sustituir datos en memoria por persistencia real con PostgreSQL y migraciones con Flyway.
- Instrumentar los servicios con Spring Boot Actuator, Micrometer y OpenTelemetry.
- Centralizar métricas en Prometheus y visualizarlas en Grafana.
- Centralizar trazas distribuidas en Jaeger.
- Centralizar logs en Elasticsearch y consultarlos en Kibana.
- Estructurar los logs de aplicación en formato ECS JSON para facilitar búsquedas y correlación.
- Definir alertas de observabilidad tanto por métricas como por logs.
- Desplegar GitLab self-managed en el mismo laboratorio.
- Ejecutar pipelines en Kubernetes con GitLab Runner.
- Construir imágenes de contenedor, publicarlas en el GitLab Container Registry y desplegarlas por SHA de commit.

---

## 3. Glosario técnico mínimo

Antes de entrar en la implementación conviene fijar algunos términos que aparecen repetidamente en el documento.

| Término | Significado práctico en este proyecto |
|---|---|
| Microservicio | aplicación Spring Boot con responsabilidad acotada |
| Chart Helm | plantilla reutilizable para desplegar recursos Kubernetes |
| Release Helm | instancia desplegada de un chart |
| Rollout | proceso por el que Kubernetes sustituye una versión de un deployment por otra |
| Trace | recorrido completo de una petición entre varios componentes |
| Span | segmento individual de una traza |
| Registry | servicio donde se almacenan imágenes de contenedor |
| Runner | agente que ejecuta jobs de GitLab CI |
| `imagePullSecret` | credencial que usa Kubernetes para descargar imágenes privadas |
| OTLP | protocolo de exportación usado por OpenTelemetry |

Este glosario no pretende ser exhaustivo. Su objetivo es que un lector junior tenga un punto de apoyo para no perderse cuando aparezcan los mismos conceptos en capítulos posteriores.

---

## 4. Alcance y límites del laboratorio

Este laboratorio cubre una porción importante del ciclo de vida operativo de un backend moderno, pero mantiene un alcance deliberadamente contenido para seguir siendo comprensible y ejecutable en un entorno local.

### 3.1 Alcance funcional

- cuatro microservicios de negocio
- una librería compartida
- una base de datos PostgreSQL
- despliegue en un único clúster Minikube
- stack de observabilidad completo
- GitLab local con Runner y Registry

### 3.2 Límites conscientes

- no se ha buscado alta disponibilidad real
- no se han introducido méltiples clústeres ni méltiples entornos completos
- el storage y algunos componentes del chart de GitLab son vélidos para laboratorio, no para producción
- algunas decisiones de RBAC y seguridad se han simplificado por ser un entorno docente

Este enfoque es intencionado. El objetivo no es replicar una plataforma corporativa en toda su complejidad, sino construir un sistema suficientemente realista como para aprender de él y explicarlo con criterio.

---

## 5. Arquitectura general del laboratorio

La arquitectura final del proyecto puede entenderse como la superposición de cuatro capas:

1. capa de aplicación  
2. capa de datos  
3. capa de observabilidad  
4. capa de CI/CD y plataforma

### 4.1 Capa de aplicación

La aplicación está formada por los siguientes módulos:

- `api-gateway`
- `user-service`
- `product-service`
- `order-service`
- `essentials-lib`

El flujo principal de negocio es:

```text
Cliente -> api-gateway -> order-service -> user-service / product-service
```

Este flujo se eligió porque permite demostrar una cadena distribuida sencilla pero suficiente para practicar:

- trazabilidad entre servicios
- correlación de logs
- métricas por componente
- detección de fallos downstream

### 4.2 Capa de datos

La persistencia se resuelve con una instancia PostgreSQL desplegada en Kubernetes. Cada servicio trabaja sobre su propio esquema, mientras que las migraciones se controlan de forma independiente con Flyway.

### 4.3 Capa de observabilidad

La observabilidad se compone de:

- Prometheus para métricas
- Grafana para dashboards y alertas de métricas
- OpenTelemetry Collector como agregador de telemetría
- Jaeger para trazas distribuidas
- Filebeat para recolección de logs
- Elasticsearch para almacenamiento y consulta de logs
- Kibana para exploración y alertado basado en logs

### 4.4 Capa de CI/CD y plataforma

La automatización de plataforma se apoya en:

- GitLab CE desplegado dentro de Minikube
- GitLab Runner desplegado dentro de Kubernetes
- GitLab Container Registry como registro privado de imágenes
- pipeline GitLab CI para build, test, package, validación Helm, build de imágenes y despliegue manual

---

## 6. Estructura del repositorio

La raíz del repositorio contiene los elementos principales del laboratorio:

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
|-- .gitlab-ci.yml
|-- pom.xml
`-- roadmap.md
```

### 5.1 Módulos Maven

El proyecto se construye como multi-módulo Maven. Los módulos de aplicación comparten dependencias y errores comunes a través de `essentials-lib`.

### 5.2 Directorio `deploy`

`deploy/` concentra la infraestructura declarativa del laboratorio:

- `deploy/helm` para charts de aplicación y PostgreSQL
- `deploy/environments/minikube` para valores del entorno local
- `deploy/observability` para Prometheus, Grafana, Jaeger, OTel Collector y stack Elastic
- `deploy/gitlab` para la instalación de GitLab
- `deploy/gitlab-runner` para la instalación del runner y sus permisos

### 5.3 Directorio `scripts`

El directorio `scripts/` contiene automatizaciones prácticas para:

- construir imágenes locales o para Minikube
- desplegar aplicación
- desplegar observabilidad
- instalar ECK
- abrir y cerrar port-forwards
- validar endpoints

Esta capa de scripts es muy importante desde el punto de vista didáctico, porque muestra cómo convertir pasos manuales en operaciones repetibles.

---

## 7. Construcción de la base de microservicios

### 6.1 Diseño inicial

La primera fase del proyecto consistió en construir una base mínima sobre la que luego poder introducir observabilidad y automatización. Se definieron cuatro servicios:

- `user-service`: catálogo de usuarios
- `product-service`: catálogo de productos
- `order-service`: gestión de pedidos y dependencia de los catálogos
- `api-gateway`: punto de entrada unificado

Este diseño responde a una idea pedagógica clara: si los servicios son completamente independientes, la observabilidad distribuida pierde valor. En cambio, al introducir una cadena de dependencias, el proyecto gana interés real.

### 6.2 Evolución hacia una estructura más mantenible

Conforme el proyecto creció, los servicios evolucionaron hacia una estructura por capas:

- `api`
- `service`
- `service/impl`
- `repository`
- `model`

También se introdujeron:

- DTOs generados a partir de OpenAPI
- interfaces de servicio
- separación entre lógica de negocio y acceso a datos
- manejo común de errores desde la librería compartida

Desde el punto de vista de ingeniería, esto es importante porque evita que la base del laboratorio se convierta en una demo frágil. Una plataforma de observabilidad se beneficia de una aplicación razonablemente estructurada.

### 6.3 Ejemplo de responsabilidad en el gateway

El gateway no es un simple proxy ciego, sino un componente que centraliza acceso y facilita el seguimiento del flujo completo:

```java
public interface GatewayService {
    Object getOverview();
    Object getUsers();
    Object getProducts();
    Object getOrders();
}
```

Este tipo de interfaz permite:

- encapsular llamadas downstream
- instrumentar mejor el flujo
- registrar logs útiles
- centralizar puntos de fallo y latencia

### 6.4 Qué hace exactamente cada módulo

Una forma muy útil de entender el proyecto es separar responsabilidades por módulo. La siguiente tabla resume la función de cada uno y por qué existe.

| Módulo | Responsabilidad principal | Tecnologías destacadas | Por qué es importante |
|---|---|---|---|
| `api-gateway` | Punto de entrada unificado | Spring Web, Actuator, Prometheus, OTel, ECS logging | Permite observar llamadas agregadas y errores downstream |
| `user-service` | Gestión de usuarios | Spring Web, JPA, Flyway, PostgreSQL | Introduce persistencia y validaciones de negocio |
| `product-service` | Gestión de productos | Spring Web, JPA, Flyway, PostgreSQL | Aporta catálogo persistente y otro dominio de negocio |
| `order-service` | Gestión de pedidos y orquestación | Spring Web, JPA, Flyway, RestClient, OTel `@WithSpan` | Es el mejor punto para enseñar trazas distribuidas |
| `essentials-lib` | Código común | Excepciones comunes, manejo de errores | Evita duplicación y unifica respuesta de errores |

### 6.5 Por qué se eligió este flujo de negocio

El flujo `gateway -> order-service -> user-service/product-service` no es arbitrario. Desde un punto de vista docente, es ideal porque reúne varios escenarios frecuentes:

- un servicio frontal que recibe tráfico del cliente
- un servicio de negocio que agrega información
- dos servicios de catálogo consultados en tiempo de ejecución
- puntos claros donde medir latencia
- puntos claros donde registrar fallos downstream
- contexto suficiente para generar trazas distribuidas interesantes

Si todos los servicios fueran completamente autónomos, la parte de observabilidad distribuida sería menos expresiva. En cambio, con esta topología se puede responder a preguntas como:

- ¿cuánto tarda una petición completa?
- ¿qué servicio fue el más lento?
- ¿falló el servicio frontal o un servicio dependiente?
- ¿hay logs de error asociados al mismo `trace_id`?

### 6.6 Ejemplo real de llamada downstream en el gateway

El siguiente fragmento del gateway resume muy bien la filosofía del proyecto: hacer visibles las llamadas entre servicios y tratarlas como parte del sistema observable, no como una caja negra.

```java
private ResponseEntity<Object> forwardGet(String baseUrl, String path, Object... uriVariables) {
    String target = baseUrl + path;
    try {
        log.info("Forwarding GET request to {}", target);
        Object body = restClient.get()
                .uri(target, uriVariables)
                .retrieve()
                .body(Object.class);
        log.info("GET {} completed successfully", target);
        return ResponseEntity.ok(body);
    } catch (RestClientResponseException exception) {
        log.warn("GET {} returned downstream status {}", target, exception.getStatusCode().value());
        return buildDownstreamResponse(exception);
    } catch (RestClientException exception) {
        log.error("GET {} failed because downstream is unavailable", target, exception);
        return buildUnavailableResponse(exception);
    }
}
```

### 6.7 Qué enseña este fragmento

- `RestClient` encapsula la llamada HTTP al siguiente servicio.
- `log.info` permite seguir el camino feliz del flujo.
- `log.warn` captura respuestas erróneas del servicio downstream.
- `log.error` captura indisponibilidad real del downstream.
- la respuesta al cliente se construye de forma explícita, evitando excepciones opacas.

Desde el punto de vista de observabilidad, este código es valioso porque produce:

- métricas HTTP en el servicio
- logs semánticamente útiles
- trazas que se pueden seguir en Jaeger

### 6.8 Imagen recomendada en esta sección

Figura sugerida: `01-arquitectura-general.png`  
Qué debería mostrar:

- cliente o navegador
- `api-gateway`
- `order-service`
- `user-service`
- `product-service`
- PostgreSQL
- stack de observabilidad

La idea es que el lector vea la topología global antes de entrar en Prometheus, Jaeger o Elastic.

---

## 8. Persistencia real con PostgreSQL y Flyway

### 7.1 De datos en memoria a persistencia

Uno de los primeros saltos de madurez del proyecto fue abandonar el modelo puramente en memoria. Esto permitió acercar el laboratorio a un escenario mucho más realista, donde existen migraciones, estado persistente y problemas de compatibilidad entre entornos.

### 7.2 Despliegue de PostgreSQL en Kubernetes

Se creó un chart Helm propio para PostgreSQL, con plantillas para:

- `Deployment`
- `Service`
- `Secret`
- `PersistentVolumeClaim`

La presencia del `PersistentVolumeClaim` es clave porque garantiza continuidad del estado entre reinicios del pod.

### 7.3 Migraciones con Flyway

Cada servicio gestiona sus migraciones de forma independiente. El enfoque seguido fue:

- una única instancia PostgreSQL
- un esquema por microservicio
- Flyway como mecanismo de versionado de base de datos

Este diseño es simple y útil para laboratorio, porque permite practicar versionado de esquema sin multiplicar el número de bases.

### 7.4 Problemas reales con migraciones

Durante el desarrollo aparecieron problemas clásicos de evolución de base de datos:

- modificación de migraciones ya aplicadas
- divergencia entre historial y estado real
- necesidad de reparar el historial de Flyway

Este punto tiene mucho valor pedagógico, porque enseña una lección fundamental: las migraciones no son un detalle accesorio, forman parte del contrato operativo del sistema.

### 7.5 Tests con Testcontainers

Para evitar falsos positivos asociados a H2, las pruebas se alinearon más con el entorno real usando PostgreSQL efímero con Testcontainers. La ventaja principal es que el comportamiento de base de datos se acerca mucho más al de producción o preproducción.

### 7.6 Qué problema resuelve Flyway en este laboratorio

Cuando un proyecto usa JPA y una base de datos relacional, existe una tentación frecuente: dejar que Hibernate cree o altere el esquema automáticamente. Para un laboratorio pequeño puede parecer suficiente, pero tiene varias desventajas:

- no queda versión explícita del esquema
- no hay trazabilidad de cambios
- es difícil reproducir estados históricos
- aparecen diferencias entre entornos

Por eso aquí se eligió:

- `spring.jpa.hibernate.ddl-auto=validate`
- Flyway como responsable único de crear y evolucionar el esquema

Ese diseño separa claramente dos responsabilidades:

| Herramienta | Responsabilidad |
|---|---|
| Hibernate / JPA | Validar que el modelo Java encaja con el esquema |
| Flyway | Crear y versionar el esquema real |

### 7.7 Ejemplo real de configuración de base de datos

En `user-service` la configuración principal quedó así:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/ecommerce_platform}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:ecommerce}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:ecommerce123}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.default_schema=user_service
spring.flyway.create-schemas=true
spring.flyway.schemas=user_service
spring.flyway.default-schema=user_service
```

### 7.8 Explicación línea a línea

- `spring.datasource.url`: define el endpoint de PostgreSQL.
- `username` y `password`: desacoplan credenciales del código.
- `ddl-auto=validate`: obliga a que el esquema exista y sea compatible.
- `open-in-view=false`: evita extender el contexto de persistencia hasta la capa web.
- `default_schema=user_service`: separa el dominio de usuario del resto.
- `flyway.create-schemas=true`: permite que Flyway cree el esquema si no existe.
- `flyway.schemas` y `flyway.default-schema`: limitan la migración al esquema del servicio.

### 7.9 Por qué se usaron esquemas separados

En vez de levantar cuatro bases de datos distintas, el proyecto usa una sola instancia PostgreSQL con varios esquemas. Esto tiene ventajas claras para un laboratorio:

- menos componentes que operar
- menos consumo de recursos
- mismo motor y mismas credenciales base
- aislamiento lógico razonable entre dominios

Es un compromiso didáctico muy útil. Permite enseñar separación de dominios sin convertir el laboratorio en una instalación de base de datos excesivamente pesada.

### 7.10 Testcontainers en la práctica

La configuración de test de `user-service` muestra muy bien la idea:

```properties
spring.datasource.url=jdbc:tc:postgresql:16-alpine:///userdb
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
spring.datasource.username=test
spring.datasource.password=test
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.default_schema=user_service
spring.flyway.create-schemas=true
spring.flyway.schemas=user_service
spring.flyway.default-schema=user_service
otel.sdk.disabled=true
```

### 7.11 Qué enseña esta configuración de test

- `jdbc:tc:postgresql:16-alpine:///userdb` hace que Testcontainers levante PostgreSQL automáticamente.
- el driver `ContainerDatabaseDriver` abstrae el arranque del contenedor.
- el test sigue validando esquema y migraciones reales.
- `otel.sdk.disabled=true` evita ruido de telemetría en tests de contexto.

Este detalle es importante para la memoria: explica por qué la pipeline necesita acceso a Docker en la fase de tests.

### 7.12 Imagen recomendada en esta sección

Figura sugerida: `10-kubectl-get-pods-a.png`  
Qué debería mostrar:

- `postgres`
- los cuatro microservicios
- estado `Running`

Si quieres una captura más específica de esta sección, también encaja una captura de:

- `kubectl get pvc`
- o una tabla de esquemas en PostgreSQL desde un cliente SQL

---

## 9. Despliegue en Kubernetes con Helm

### 8.1 Motivación para usar Helm

Una vez que la aplicación dejó de ser local y pasí a ejecutarse en Kubernetes, era necesario un mecanismo para empaquetar recursos, parametrizarlos y reutilizarlos por entorno. Helm cubre exactamente esa necesidad.

### 8.2 Un chart por servicio

Cada microservicio dispone de su propio chart:

- `deploy/helm/api-gateway`
- `deploy/helm/user-service`
- `deploy/helm/product-service`
- `deploy/helm/order-service`
- `deploy/helm/postgres`

Este enfoque facilita:

- despliegues independientes
- configuración específica por servicio
- evolución aislada de recursos Kubernetes

### 8.3 Separación entre chart y entorno

Los charts contienen valores por defecto, mientras que el entorno Minikube se parametriza en:

- `deploy/environments/minikube/api-gateway-values.yaml`
- `deploy/environments/minikube/user-service-values.yaml`
- `deploy/environments/minikube/product-service-values.yaml`
- `deploy/environments/minikube/order-service-values.yaml`
- `deploy/environments/minikube/postgres-values.yaml`

Esta separación es muy útil porque evita duplicar charts y permite adaptar comportamiento por entorno sin modificar plantillas.

### 8.4 Automatización del despliegue

El laboratorio incluye scripts específicos como:

- `scripts/build-minikube-images.ps1`
- `scripts/deploy-minikube.ps1`

El primero sirve para construir imágenes directamente dentro de Minikube. El segundo empaqueta el despliegue completo con Helm y espera a que los rollouts terminen correctamente.

### 8.5 Aprendizaje principal

Kubernetes no debe entenderse solo como "levantar pods". La operación real exige:

- empaquetado
- parámetros por entorno
- restart controlado
- espera de readiness
- diagnóstico cuando un rollout falla

### 8.6 Anatomía de un chart Helm de microservicio

El chart de `api-gateway` es suficientemente simple como para explicarlo casi línea a línea. Su `values.yaml` contiene:

```yaml
replicaCount: 1

image:
  repository: mercadona/api-gateway
  tag: 0.0.1-SNAPSHOT
  pullPolicy: IfNotPresent
  pullSecrets: []

containerPort: 8080

service:
  type: ClusterIP
  port: 8080
  targetPort: 8080
  nodePort: null
```

### 8.7 Qué enseña este `values.yaml`

- `replicaCount`: número deseado de pods.
- `image.repository`: de dónde sale la imagen.
- `image.tag`: qué versión concreta se quiere ejecutar.
- `pullPolicy`: cuándo debe descargar la imagen.
- `pullSecrets`: credenciales para un registry privado.
- `service.type`: cómo se expondrá el servicio.

Este fichero es muy útil en una memoria docente porque muestra que Helm no "crea magia", sino que parametriza decisiones de despliegue explícitas.

### 8.8 Plantilla real del Deployment

El `deployment.yaml` del chart de `api-gateway` quedó así:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Chart.Name }}
spec:
  replicas: {{ .Values.replicaCount }}
  template:
    spec:
      {{- with .Values.image.pullSecrets }}
      imagePullSecrets:
{{ toYaml . | indent 8 }}
      {{- end }}
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
```

### 8.9 Cómo leer esta plantilla si eres junior

- `{{ .Chart.Name }}`: usa el nombre del chart como nombre del deployment y del contenedor.
- `{{ .Values.* }}`: inyecta valores desde `values.yaml` o desde un fichero de entorno.
- `with .Values.image.pullSecrets`: añade `imagePullSecrets` solo si hay credenciales definidas.
- `image.repository` y `image.tag`: permiten separar la plantilla del detalle de versión.

La gran enseñanza aquí es que Helm no reemplaza Kubernetes. Helm es una capa de parametrización y reutilización sobre Kubernetes.

### 8.10 Valores específicos de Minikube

El entorno de Minikube modifica el comportamiento del chart. Por ejemplo, `api-gateway-values.yaml` contiene:

```yaml
image:
  pullPolicy: IfNotPresent
  pullSecrets:
    - name: gitlab-registry-creds

service:
  type: NodePort
  nodePort: 30080

env:
  - name: USER_SERVICE_URL
    value: http://user-service:8080
```

### 8.11 Qué problema resuelve esta capa de values

Sin un fichero por entorno, el chart tendría que codificar detalles del laboratorio:

- `NodePort`
- nombres de servicios internos
- credenciales de registry
- URLs de servicios downstream

Ese sería un diseño pobre, porque mezclaría plantilla base con detalles del entorno. Separarlo mejora mucho la mantenibilidad.

### 8.12 Script de despliegue y lógica operativa

El script `deploy-minikube.ps1` es un buen ejemplo de automatización incremental. No se limita a aplicar YAML, sino que incorpora diagnóstico.

Fragmento representativo:

```powershell
function Wait-DeploymentRollout {
    param(
        [string]$Name,
        [int]$TimeoutSeconds = 180
    )

    $result = kubectl rollout status deployment/$Name --timeout="$($TimeoutSeconds)s" 2>&1
    if ($LASTEXITCODE -eq 0) {
        $result | Write-Host
        return
    }

    kubectl describe deployment $Name
    kubectl get pods -l app=$Name -o wide
    kubectl logs deployment/$Name --tail=120 --all-containers=true
    throw "Rollout failed for deployment $Name"
}
```

### 8.13 Qué enseña este script

- espera activa del rollout
- salida rápida cuando todo va bien
- captura de diagnóstico cuando algo falla
- uso de `describe` y `logs` como rutina de troubleshooting

Este patrón es muy valioso para juniors porque enseña una disciplina operativa concreta: automatizar no es solo ejecutar, también es saber diagnosticar.

### 8.14 Imagen recomendada en esta sección

Figura sugerida: `11-kubectl-get-svc.png`  
Qué debería mostrar:

- `api-gateway` como `NodePort`
- servicios internos como `ClusterIP`
- relación entre exposición externa e interna

---

## 10. Observabilidad de métricas y trazas

### 9.1 Instrumentación con Actuator y Micrometer

La primera capa de observabilidad se construyó con:

- Spring Boot Actuator
- Micrometer
- exposición del endpoint `/actuator/prometheus`

Con ello, cada servicio pudo empezar a publicar:

- métricas JVM
- métricas HTTP
- información básica de health y readiness

### 9.2 OpenTelemetry

Para las trazas distribuidas se introdujo OpenTelemetry, añadiendo contexto de servicio y entorno. Esto permitió que una llamada desde el gateway hasta los servicios downstream quedara representada como una traza completa.

### 9.3 OpenTelemetry Collector

El OTel Collector se desplegó como componente intermedio para:

- recibir spans
- procesarlos
- redirigirlos a Jaeger

Esta pieza es muy didáctica porque introduce la idea de canalizar telemetría a través de un punto de agregación y no enviar todo directamente desde la aplicación al backend final.

### 9.4 Jaeger

Jaeger se incorporó para visualizar las trazas distribuidas. Gracias a ello fue posible:

- seguir una petición entre varios servicios
- detectar llamadas downstream
- validar correlación con logs mediante `trace_id`

### 9.5 Ejemplo de valor operativo

En un laboratorio simple podría parecer suficiente ver tiempos de respuesta en Grafana. Sin embargo, las trazas aportan una capa distinta:

- qué servicio llamó a cuál
- cuánto tardó cada salto
- dónde fallí una petición concreta

Esa diferencia es crucial para un perfil de observabilidad.

### 9.6 Conceptos clave: traza, span y contexto

Para que esta sección sea realmente didáctica conviene detenerse en tres conceptos fundamentales:

| Concepto | Definición sencilla | Ejemplo en este proyecto |
|---|---|---|
| `Trace` | recorrido completo de una petición | una llamada al gateway que termina consultando pedidos y catálogos |
| `Span` | una unidad de trabajo dentro de una traza | la ejecución de `order.findById` |
| `Context` | metadatos que viajan entre servicios | identificadores de trace y span propagados entre llamadas HTTP |

En otras palabras:

- una `trace` cuenta la historia completa
- un `span` cuenta un capítulo de esa historia
- el `context` permite que cada servicio sepa a qué historia pertenece

### 9.7 Dependencias necesarias en Spring Boot

En los módulos de aplicación se añadieron dependencias como estas:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
</dependency>
```

### 9.8 Para qué sirve cada dependencia

- `spring-boot-starter-actuator`: expone endpoints operativos como `health` y `prometheus`.
- `micrometer-registry-prometheus`: publica métricas en formato compatible con Prometheus.
- `opentelemetry-spring-boot-starter`: activa instrumentación automática y exportación de trazas.

### 9.9 Configuración real de telemetría

La configuración de `api-gateway` es representativa:

```properties
management.endpoints.web.exposure.include=health,info,prometheus

otel.resource.attributes.service.name=api-gateway
otel.resource.attributes.service.namespace=ecommerce-platform
otel.resource.attributes.deployment.environment=minikube

otel.traces.exporter=otlp
otel.metrics.exporter=none
otel.logs.exporter=none

otel.exporter.otlp.endpoint=http://otel-collector.observability.svc.cluster.local:4318
otel.exporter.otlp.protocol=http/protobuf
```

### 9.10 Explicación detallada de esta configuración

- `management.endpoints.web.exposure.include`: decide qué endpoints operativos quedan accesibles.
- `otel.resource.attributes.service.name`: nombre lógico del servicio en Jaeger.
- `service.namespace`: agrupa servicios bajo una misma plataforma.
- `deployment.environment`: añade contexto de entorno.
- `otel.traces.exporter=otlp`: dice que las trazas se envían por OTLP.
- `otel.metrics.exporter=none`: evita duplicar exportación de métricas vía OTel.
- `otel.logs.exporter=none`: evita mezclar la estrategia de logs de OTel con Elastic.
- `otel.exporter.otlp.endpoint`: apunta al OTel Collector dentro del clúster.

Este diseño es importante. No todo se manda por OpenTelemetry. En este laboratorio se separan responsabilidades:

- métricas -> Prometheus
- trazas -> OpenTelemetry + Jaeger
- logs -> ECS JSON + Filebeat + Elasticsearch

### 9.11 Instrumentación manual en `order-service`

Aunque el starter de OpenTelemetry ya aporta instrumentación automática, se añadió también instrumentación manual en puntos de interés. Ejemplo:

```java
@Override
@Transactional(readOnly = true)
@WithSpan("order.findById")
public OrderDto findById(@SpanAttribute("order.id") Long id) {
    OrderDto order = toDto(loadOrder(id));
    log.info("Retrieved order with id {} and {} items", id, order.getItems().size());
    return order;
}
```

### 9.12 Qué enseña `@WithSpan`

- crea un span explícito con un nombre semántico
- hace visible una operación de negocio concreta en la traza
- permite enriquecer el span con atributos útiles como `order.id`

Esto tiene mucho valor didáctico porque enseña la diferencia entre:

- observabilidad automática
- observabilidad intencional

La automática es útil, pero la intencional hace que las trazas sean mucho más interpretables.

### 9.13 Configuración del OTel Collector

El collector se definió así:

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch: {}

exporters:
  debug:
    verbosity: detailed
  otlp:
    endpoint: jaeger.observability.svc.cluster.local:4317
    tls:
      insecure: true
```

### 9.14 Cómo leer esta configuración

- `receivers`: indica cómo recibe telemetría el collector.
- `grpc` y `http`: permiten varias formas de ingestión.
- `batch`: agrupa spans para enviarlos de forma más eficiente.
- `debug`: ayuda a diagnosticar en laboratorio.
- `otlp`: reenvía las trazas a Jaeger.

### 9.15 Flujo de una traza en este proyecto

El flujo real de una petición instrumentada es:

1. el cliente llama al `api-gateway`
2. Spring y OpenTelemetry generan contexto
3. el gateway llama a `order-service`
4. `order-service` crea spans propios y consulta `user-service` y `product-service`
5. los spans se envían al OTel Collector
6. el collector los reenvía a Jaeger
7. Jaeger permite reconstruir la traza completa

### 9.16 Imagen recomendada en esta sección

Figura sugerida: `30-jaeger-trace-overview.png`  
Qué debería mostrar:

- una traza completa
- spans de gateway y order-service
- relación temporal entre spans

Figura sugerida adicional: `31-jaeger-trace-by-id.png`  
Qué debería mostrar:

- búsqueda de una traza concreta por `trace_id`

---

## 11. Métricas de plataforma con Prometheus y Grafana

### 10.1 Prometheus como backend de métricas

Prometheus se desplegó con manifiestos propios y un scraping ajustado tanto a la aplicación como al clúster. Esto permitió unificar en el mismo backend:

- métricas de microservicios
- métricas del nodo
- métricas del clúster

### 10.2 Grafana como capa de visualización y alertado

Grafana se provisionó con:

- datasource Prometheus
- proveedores de dashboards
- dashboards importados y dashboards propios

Este punto tiene mucho valor práctico, porque enseña no solo a "instalar Grafana", sino a operarla correctamente como parte de una plataforma reproducible.

### 10.3 Problema de dashboards importados

Una incidencia relevante fue el fallo de dashboards importados que referenciaban `${DS_PROMETHEUS}`. El problema no estaba en Prometheus ni en Grafana como servicio, sino en el JSON importado.

La lección aquí es importante:

- muchas incidencias de observabilidad no vienen de la infraestructura base
- a menudo vienen de plantillas o dashboards no adaptados al datasource real

### 10.4 Métricas del clúster

Además de la aplicación se incorporaron:

- `metrics-server`
- `kube-state-metrics`
- `node-exporter`

Con esto el laboratorio dejó de ser solo observabilidad de backend y pasí a cubrir también la salud del clúster, que es una competencia esencial en roles de plataforma.

### 10.5 Configuración real de scraping en Prometheus

El `ConfigMap` de Prometheus contiene entradas muy expresivas del enfoque seguido:

```yaml
- job_name: user-service
  metrics_path: /actuator/prometheus
  static_configs:
    - targets: ["user-service.default.svc.cluster.local:8080"]

- job_name: kube-state-metrics
  static_configs:
    - targets: ["kube-state-metrics.observability.svc.cluster.local:8080"]

- job_name: node-exporter
  static_configs:
    - targets: ["node-exporter.observability.svc.cluster.local:9100"]
```

### 10.6 Qué significa esto en la práctica

Prometheus no distingue "mágicamente" entre aplicación y plataforma. Es el operador quien decide qué scrapea y cómo lo agrupa:

- los microservicios se scrapean por `/actuator/prometheus`
- `kube-state-metrics` aporta estado lógico de objetos de Kubernetes
- `node-exporter` aporta métricas del nodo

Esta mezcla es justo la que permite construir dashboards completos de plataforma.

### 10.7 Datasource provisionado en Grafana

El datasource quedó definido de forma declarativa:

```yaml
datasources:
  - name: Prometheus
    uid: prometheus
    type: prometheus
    access: proxy
    url: http://prometheus.observability.svc.cluster.local:9090
    isDefault: true
```

### 10.8 Por qué es importante el `uid`

Una lección muy útil del proyecto es que los dashboards importados suelen fallar si el datasource no tiene el `uid` esperado. Declararlo explícitamente hace que:

- los dashboards sean más reproducibles
- sea más fácil adaptar dashboards importados
- el aprovisionamiento sea estable entre reinicios

### 10.9 Qué dashboards merece la pena explicar en el informe

En el documento final conviene distinguir entre dos tipos de dashboard:

| Tipo | Qué demuestra |
|---|---|
| Dashboard de aplicación | latencia, throughput, errores y salud de los microservicios |
| Dashboard de plataforma | targets, estado de pods, series activas, salud del nodo y scraping |

De esta forma el lector entiende que observabilidad no es solo ver "peticiones por segundo", sino también comprobar si el clúster que ejecuta la aplicación está sano.

### 10.10 Imagen recomendada en esta sección

Figura sugerida: `20-grafana-dashboard-general.png`  
Qué debería mostrar:

- paneles de latencia, error rate, heap JVM y estado general

Figura sugerida adicional: `22-prometheus-targets.png`  
Qué debería mostrar:

- targets de Prometheus en estado `UP`

---

## 12. Logging centralizado y logs estructurados

### 11.1 Introducción del stack Elastic

Para cubrir la dimensión de logs se desplegaron:

- Elasticsearch
- Kibana
- Filebeat
- operador ECK

La elección respondió a la necesidad de practicar un stack de logging ampliamente utilizado y alineado con perfiles orientados a observabilidad.

### 11.2 Primer problema: tener logs no significa tener observabilidad

En las primeras iteraciones, Kibana recibía datos pero no resultaba útil para investigar flujo de negocio. Muchos eventos visibles eran puramente de infraestructura. El problema real no era Filebeat ni Elasticsearch, sino la calidad de los logs emitidos por la aplicación.

### 11.3 Introducción de logging de negocio

Se añadieron logs de negocio en servicios clave, especialmente en los puntos donde el flujo entra o donde existen dependencias downstream. Con ello empezaron a aparecer mensajes que sí describían acciones relevantes del sistema.

### 11.4 Limitaciones del texto plano

Aunque los logs de negocio mejoraron la visibilidad, seguían siendo difíciles de explotar:

- no era trivial filtrar por `service.name`
- no era trivial filtrar por `log.level`
- la correlación con trazas era pobre

Por eso se decidió migrar a logs ECS JSON.

### 11.5 ECS JSON

Los servicios incorporaron `logback-ecs-encoder`, lo que permitió emitir eventos estructurados con campos como:

- `@timestamp`
- `service.name`
- `service.environment`
- `log.level`
- `log.logger`
- `message`
- `trace_id`
- `span_id`

Este cambio marcó uno de los hitos más importantes del laboratorio: los logs pasaron de ser texto "legible" a ser datos realmente explotables.

### 11.6 Adaptación de Filebeat

Filebeat se ajustó para detectar y decodificar JSON dentro del campo `message`, expandiendo los campos y enriqueciendo el evento con metadatos de Kubernetes. De este modo Kibana pudo consultar directamente propiedades estructuradas.

### 11.7 Correlación Kibana -> Jaeger

Una vez disponible `trace_id` en logs, el flujo de troubleshooting ganó mucha potencia:

1. localizar un evento en Kibana  
2. extraer el `trace_id`  
3. abrir Jaeger  
4. buscar la traza exacta  

Este punto convierte el laboratorio en algo mucho más valioso que una simple agregación de logs.

### 11.8 Configuración real de `logback-spring.xml`

Los cuatro servicios comparten una estructura muy parecida para emitir ECS JSON. El patrón básico es:

```xml
<configuration>
    <springProperty scope="context" name="serviceName" source="spring.application.name"/>

    <appender name="ECS_JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="co.elastic.logging.logback.EcsEncoder">
            <serviceName>${serviceName}</serviceName>
            <serviceEnvironment>minikube</serviceEnvironment>
            <includeOrigin>true</includeOrigin>
        </encoder>
    </appender>

    <logger name="com.mercadona.devops" level="INFO"/>

    <root level="INFO">
        <appender-ref ref="ECS_JSON_CONSOLE"/>
    </root>
</configuration>
```

### 11.9 Qué hace cada bloque de este logback

- `springProperty`: reutiliza `spring.application.name` como nombre de servicio.
- `ConsoleAppender`: escribe en `stdout`, que es lo esperable en contenedores.
- `EcsEncoder`: transforma cada evento de log en un documento JSON con estructura ECS.
- `serviceEnvironment`: añade contexto de entorno.
- `includeOrigin=true`: agrega información de origen útil para debugging.

Esta configuración es un excelente ejemplo de una buena práctica moderna: en contenedores, lo habitual es escribir a consola y dejar que un agente externo recoja y enrute los logs.

### 11.10 Ejemplo de log útil en código de negocio

En `OrderServiceImpl` aparecen logs que sí ayudan a entender el comportamiento:

```java
log.info("Fetching user {} from user-service", userId);
...
log.error("User service unavailable while fetching user {}", userId, exception);
```

### 11.11 Por qué estos logs sí aportan valor

- describen una acción concreta
- indican a qué dependencia se está llamando
- diferencian éxito, degradación y fallo
- se correlacionan con spans y métricas

Un junior suele tender a loggear solo "entró en método X". En cambio, aquí el foco se puso en eventos de negocio y de integración.

### 11.12 Filebeat como puente entre contenedores y Elasticsearch

La configuración relevante de Filebeat es esta:

```yaml
filebeat.inputs:
  - type: filestream
    id: kubernetes-container-logs
    prospector.scanner.symlinks: true
    parsers:
      - container: ~
    paths:
      - /var/log/containers/*.log
processors:
  - decode_json_fields:
      when:
        regexp:
          message: '^\{'
      fields: ["message"]
      target: ""
      overwrite_keys: true
      add_error_key: true
      expand_keys: true
  - add_kubernetes_metadata: {}
```

### 11.13 Cómo se interpreta esta configuración

- `filestream`: lector moderno de logs en Filebeat.
- `paths`: apunta a logs de contenedores de Kubernetes.
- `parsers.container`: interpreta el formato de log del runtime.
- `decode_json_fields`: convierte el JSON emitido por Logback en campos de Elasticsearch.
- `add_kubernetes_metadata`: añade namespace, pod, labels y otra información útil.

Este paso es crucial. Sin `decode_json_fields`, el log llegaría como texto plano dentro de `message`, y se perdería gran parte de la utilidad del formato ECS.

### 11.14 Qué campos gana Kibana gracias a ECS

Cuando el pipeline de logging está bien montado, Kibana puede filtrar directamente por:

- `service.name`
- `service.environment`
- `log.level`
- `log.logger`
- `trace_id`
- `span_id`

Ese es el salto de calidad real del proyecto. No se trata solo de "guardar logs", sino de convertir los logs en datos consultables.

### 11.15 Imagen recomendada en esta sección

Figura sugerida: `40-kibana-discover-logs.png`  
Qué debería mostrar:

- eventos de varios microservicios
- campos enriquecidos de Kubernetes

Figura sugerida adicional: `41-kibana-service-name-log-level.png`  
Qué debería mostrar:

- filtros por `service.name`
- filtros por `log.level`
- presencia de `trace_id`

---

## 13. Alertado por métricas y por logs

### 12.1 Alerta de métricas en Grafana

Se implementó una alerta orientada a detectar caídas de microservicios, utilizando la métrica `up` de Prometheus. La validación se realizó provocando una situación real de no disponibilidad.

### 12.2 Alerta de logs en Kibana

Se implementó una regla que detecta logs `ERROR` emitidos por cualquiera de los microservicios. Esto permitió demostrar una línea de observabilidad complementaria:

- fallo funcional
- emisión de error
- almacenamiento en Elasticsearch
- visualización en Kibana
- activación de una regla

### 12.3 Aprendizaje principal

Métricas y logs no compiten: se complementan. Las métricas sirven para detectar comportamiento anómalo agregado; los logs ayudan a explicar qué ocurrió; las trazas muestran el recorrido de una petición concreta.

### 12.4 Ejemplo didáctico de alerta de métricas

La alerta `MicroserviceDown` se apoyó en una idea muy simple y potente: si Prometheus deja de ver un target, probablemente el servicio está caído o inaccesible.

Consulta representativa:

```promql
up{job=~"api-gateway|user-service|product-service|order-service"}
```

Interpretación:

- `up = 1`: Prometheus ha podido scrapear el target.
- `up = 0`: el scrape ha fallado.

Este ejemplo es excelente para un perfil junior porque enseña que una alerta efectiva no siempre necesita lógica compleja. A veces basta con entender bien una métrica base.

### 12.5 Ejemplo didáctico de alerta de logs

La regla de Kibana para errores se apoyó en una consulta del tipo:

```text
log.level : "ERROR" and service.name : ("api-gateway" or "user-service" or "product-service" or "order-service")
```

Qué enseña esta consulta:

- `log.level` solo existe porque el logging es estructurado.
- `service.name` solo es filtrable porque se ha normalizado el formato del log.
- sin ECS JSON, esta regla sería mucho más frágil.

### 12.6 Diferencia pedagógica entre ambas alertas

| Alerta | Fuente | Qué detecta | Qué no explica por sí sola |
|---|---|---|---|
| Grafana `MicroserviceDown` | Métricas | indisponibilidad o fallo de scrape | la causa concreta |
| Kibana `ErrorLogsByService` | Logs | errores de aplicación visibles en logs | salud global del target |

Esta comparación merece estar explícita en la memoria, porque enseña a elegir el backend adecuado según el tipo de señal que se quiere explotar.

### 12.7 Imagen recomendada en esta sección

Figura sugerida: `21-grafana-alerta-microservicedown.png`  
Figura sugerida adicional: `42-kibana-regla-activa.png`

---

## 14. GitLab self-managed dentro del laboratorio

### 13.1 Motivación

Una vez establecida la observabilidad, el siguiente salto natural fue incorporar una capa real de CI/CD. En lugar de depender de GitLab.com u otro SaaS, se optó por desplegar GitLab CE dentro del propio Minikube.

### 13.2 Configuración principal

La instalación se apoyó en:

- `deploy/gitlab/values-minikube.yaml`
- `deploy/gitlab/values-local.yaml`

La configuración local se orientó a:

- edición CE
- dominio basado en `nip.io`
- desactivación del Prometheus y Grafana incluidos en el chart
- uso de la instancia ya existente de observabilidad del laboratorio

### 13.3 Dificultades de acceso

El acceso a GitLab desde Windows en un entorno Minikube con driver Docker no fue inmediato. Hubo que combinar:

- `port-forward` del ingress controller
- entrada en `hosts`
- acceso por `https://gitlab.192.168.49.2.nip.io:8443`

Este punto es especialmente interesante en una memoria técnica porque muestra una idea importante: muchas dificultades de plataforma tienen que ver con red y exposición de servicios, no con la aplicación en sí.

### 13.4 Configuración local de GitLab

El override principal quedó en `deploy/gitlab/values-local.yaml`:

```yaml
global:
  edition: ce
  hosts:
    domain: 192.168.49.2.nip.io
    externalIP: 192.168.49.2
  grafana:
    enabled: false

prometheus:
  install: false

gitlab-runner:
  install: false
```

### 13.5 Qué significan estos valores

- `edition: ce`: usa GitLab Community Edition.
- `domain` y `externalIP`: definen cómo se construyen hosts públicos.
- `grafana.enabled: false`: evita duplicar Grafana porque el laboratorio ya tiene una.
- `prometheus.install: false`: evita desplegar otra pila de métricas dentro de GitLab.
- `gitlab-runner.install: false`: separa la instalación del runner del servidor GitLab.

Esta es una decisión importante desde el punto de vista de diseño: cuando un laboratorio ya tiene una capa de observabilidad propia, no conviene duplicarla dentro de GitLab salvo que el objetivo sea estudiar GitLab como producto en sí mismo.

### 13.6 Lección de infraestructura

Montar GitLab dentro de Minikube añade una complejidad apreciable:

- ingress
- certificados
- storage
- secretos internos
- componentes como webservice, registry, Gitaly y Redis

Eso es precisamente lo que hace valiosa esta fase del proyecto. El laboratorio deja de ser solo "la app" y pasa a incluir una plataforma de CI/CD real, con sus propios problemas operativos.

### 13.7 Imagen recomendada en esta sección

Figura sugerida: `50-gitlab-home-project.png`  
Qué debería mostrar:

- proyecto `Observability-Project`
- repositorio visible
- interfaz GitLab operativa

---

## 15. GitLab Runner en Kubernetes

### 14.1 Decisión de arquitectura

Una vez desplegado GitLab, se decidió no instalar el runner en Windows. En su lugar, se desplegó GitLab Runner dentro del clúster mediante Helm.

La ventaja de esta decisión es clara:

- mantiene GitLab y ejecución de jobs dentro del mismo laboratorio
- evita dependencia directa del host Windows
- acerca la solución a un patrón más cloud-native

### 14.2 Configuración relevante

El runner quedó definido en `deploy/gitlab-runner/values.yaml` con parámetros importantes:

```yaml
gitlabUrl: http://gitlab-webservice-default.gitlab.svc.cluster.local:8181/

runners:
  config: |
    [[runners]]
      clone_url = "http://gitlab-webservice-default.gitlab.svc.cluster.local:8181"
      request_concurrency = 2
      [runners.kubernetes]
        namespace = "gitlab-runner"
        service_account = "gitlab-runner"
        image = "alpine:3.20"
        privileged = true
```

### 14.3 Razón de cada decisión

- `gitlabUrl` interno evita depender del acceso público por `:8443`
- `clone_url` resuelve el clon interno del repositorio desde el clúster
- `service_account` permite aplicar RBAC específico a los jobs
- `privileged = true` facilita jobs con `docker:dind` y escenarios de build más complejos

### 14.4 Permisos para despliegue

Para el laboratorio se creó un `ClusterRoleBinding` amplio en `deploy/gitlab-runner/rbac-deployer.yaml`:

```yaml
kind: ClusterRoleBinding
metadata:
  name: gitlab-runner-cluster-admin
subjects:
  - kind: ServiceAccount
    name: gitlab-runner
    namespace: gitlab-runner
roleRef:
  kind: ClusterRole
  name: cluster-admin
```

No es una solución pensada para producción, pero sí una forma razonable de simplificar el laboratorio y centrar la atención en el flujo CI/CD.

### 14.5 Qué es exactamente un runner

Para un lector junior conviene aclarar este punto con precisión:

- GitLab no ejecuta jobs por sí mismo.
- GitLab actúa como servidor de control.
- el `runner` es el agente que recoge jobs y los ejecuta.

En este laboratorio, la secuencia es:

1. GitLab registra un pipeline
2. el runner consulta si hay trabajo pendiente
3. el runner crea un pod de job en Kubernetes
4. ese pod ejecuta el script del job
5. el resultado vuelve a GitLab

Sin esta explicación, es fácil que un lector piense que "GitLab hace todo". No es así. El runner es la pieza ejecutora.

### 14.6 Por qué se usí URL interna del servicio GitLab

Uno de los aprendizajes más importantes de esta fase fue diferenciar dos tipos de URL:

| Tipo de URL | Ejemplo | Quién la usa |
|---|---|---|
| URL humana | `https://gitlab.192.168.49.2.nip.io:8443` | navegador desde Windows |
| URL interna de clúster | `http://gitlab-webservice-default.gitlab.svc.cluster.local:8181/` | runner y pods dentro de Kubernetes |

Esta distinción es muy importante en Kubernetes. Una URL que funciona para el usuario no tiene por qué ser la correcta para los componentes internos del clúster.

### 14.7 Qué problema resolvió `clone_url`

En la práctica, el runner no solo necesitaba registrarse, sino también clonar el repositorio. Para eso se usí:

```toml
clone_url = "http://gitlab-webservice-default.gitlab.svc.cluster.local:8181"
```

Esto resolvió un problema muy didáctico:

- el runner podía contactar con GitLab
- pero los jobs intentaban clonar desde la URL pública
- esa URL pública no era vélida desde dentro del clúster

La solución fue enseñar explícitamente al runner cuál era la URL correcta de clon interno.

### 14.8 Imagen recomendada en esta sección

Figura sugerida: `51-gitlab-runner-online.png`  
Qué debería mostrar:

- runner online
- asociado al proyecto
- estado `idle` o equivalente

---

## 16. Pipeline CI con GitLab

### 15.1 Objetivo de la CI

La primera meta no fue desplegar, sino validar automáticamente:

- compilación
- tests
- empaquetado
- validez de charts Helm

### 15.2 Estructura de stages

La pipeline terminó evolucionando a:

```yaml
stages:
  - build
  - test
  - package
  - validate
  - image
  - deploy
```

Esta secuencia es pedagógicamente buena porque refleja el orden lógico de madurez:

1. comprobar que el código compila  
2. comprobar que el código pasa pruebas  
3. empaquetarlo  
4. validar manifiestos  
5. construir y publicar imágenes  
6. desplegar  

### 15.3 Validación Helm

La fase `validate_helm` no despliega, pero sí asegura que:

- los charts son sintácticamente correctos
- los `values` del entorno renderizan correctamente

Esta práctica aporta mucho valor, porque detecta errores antes de llegar a despliegue.

### 15.4 Fragmento real de la pipeline

La parte inicial de la pipeline está definida así:

```yaml
stages:
  - build
  - test
  - package
  - validate
  - image
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  MAVEN_CLI_OPTS: "-B -ntp"

build:
  stage: build
  image: maven:3.9.9-eclipse-temurin-17
  script:
    - mvn $MAVEN_CLI_OPTS -DskipTests clean compile
```

### 15.5 Qué se está enseñando con esta configuración

- el pipeline trabaja por stages bien definidos
- la caché Maven se controla explícitamente
- la imagen del job se fija de manera declarativa
- `-B -ntp` mejora la salida del build para CI

Un detalle aparentemente pequeño, como `MAVEN_OPTS`, también enseña una buena práctica: los jobs deben ser reproducibles y no depender de cachés globales del host.

### 15.6 Por qué la fase `test` necesita Docker

La fase `test` usa:

```yaml
services:
  - name: docker:27.5.1-dind
    alias: docker
variables:
  DOCKER_HOST: tcp://docker:2375
  DOCKER_TLS_CERTDIR: ""
```

Esto existe porque los tests usan Testcontainers y, por tanto, necesitan capacidad de arrancar contenedores durante la ejecución del job. Este detalle es importante para la memoria, porque muestra que las decisiones de testing afectan directamente a la infraestructura de CI.

### 15.7 Qué valida `validate_helm`

La fase `validate_helm` no es un lujo. Responde a una necesidad concreta: detectar errores de despliegue antes del despliegue.

Ejemplo:

```yaml
- helm lint deploy/helm/postgres
- helm lint deploy/helm/user-service
- helm template user-service deploy/helm/user-service -f deploy/environments/minikube/user-service-values.yaml > /tmp/user-service.yaml
```

Con esto se consigue:

- validación sintáctica del chart
- validación de render con valores reales del entorno
- detección temprana de errores en `values` o plantillas

### 15.8 Imagen recomendada en esta sección

Figura sugerida: `52-gitlab-pipeline-ci-green.png`  
Qué debería mostrar:

- jobs `build`, `test`, `package` y `validate_helm` en verde

---

## 17. CD real con Container Registry y deploy por SHA

### 16.1 De imágenes locales a registry privado

Al principio el laboratorio funcionaba con imágenes construidas dentro de Minikube. Esto era útil para iterar rápido, pero no representaba una cadena de despliegue real. El siguiente paso fue usar el GitLab Container Registry como fuente oficial de imágenes.

### 16.2 `imagePullSecrets`

Para permitir que Kubernetes descargase imágenes privadas del registry, se adaptaron los charts para soportar `imagePullSecrets`. El patrón quedó así:

```yaml
image:
  repository: mercadona/api-gateway
  tag: 0.0.1-SNAPSHOT
  pullPolicy: IfNotPresent
  pullSecrets: []
```

Y en el `Deployment`:

```yaml
{{- with .Values.image.pullSecrets }}
imagePullSecrets:
{{ toYaml . | indent 8 }}
{{- end }}
```

Este cambio es pequeño, pero muy importante conceptualmente. Enseña cómo se conecta el mundo de CI con el runtime de Kubernetes.

### 16.3 Build de imágenes con Kaniko

Para construir imágenes dentro del clúster se eligió Kaniko. La razón principal es que encaja mejor que Docker-in-Docker en un runner Kubernetes.

Ejemplo simplificado del template de build:

```yaml
.build_image_template:
  stage: image
  image:
    name: gcr.io/kaniko-project/executor:v1.23.2-debug
    entrypoint: [""]
  script:
    - mkdir -p /kaniko/.docker
    - printf '{"auths":{"%s":{"username":"%s","password":"%s"}}}' "$CI_REGISTRY" "$CI_REGISTRY_USER" "$CI_REGISTRY_PASSWORD" > /kaniko/.docker/config.json
    - /kaniko/executor ...
```

### 16.4 Tag de imagen por commit

Las imágenes se etiquetan con:

- `CI_COMMIT_SHORT_SHA`
- y opcionalmente `main`

Esto aporta trazabilidad directa entre:

- commit
- pipeline
- imagen
- despliegue

### 16.5 Deploy manual por SHA

El job `deploy_minikube` quedó como job manual. Es una decisión sensata para laboratorio porque evita despliegues automáticos accidentales y permite validar primero el artefacto construido.

Ejemplo simplificado:

```yaml
helm upgrade --install user-service deploy/helm/user-service \
  --namespace default \
  -f deploy/environments/minikube/user-service-values.yaml \
  --set image.repository="${CI_REGISTRY_IMAGE}/user-service" \
  --set image.tag="${CI_COMMIT_SHORT_SHA}"
```

El efecto práctico es muy importante: el clúster no despliega "la última imagen local", sino una imagen inmutable asociada a un commit concreto.

### 16.6 Autenticación contra el registry desde CI

Kaniko necesita autenticarse para hacer `push` al registry. Para ello se genera un `config.json` de Docker dentro del job:

```yaml
- mkdir -p /kaniko/.docker
- printf '{"auths":{"%s":{"username":"%s","password":"%s"}}}' "$CI_REGISTRY" "$CI_REGISTRY_USER" "$CI_REGISTRY_PASSWORD" > /kaniko/.docker/config.json
```

### 16.7 Qué representa este fragmento

- `CI_REGISTRY`: host del GitLab Container Registry
- `CI_REGISTRY_USER` y `CI_REGISTRY_PASSWORD`: credenciales inyectadas por GitLab CI
- `/kaniko/.docker/config.json`: ubicación que Kaniko espera para autenticarse

Este es un buen ejemplo de cómo una pipeline traduce conceptos abstractos de CI/CD en archivos y rutas concretas dentro de un contenedor de job.

### 16.8 Autenticación contra el registry desde Kubernetes

Una vez publicada la imagen, el clúster también necesita autenticarse para hacer `pull`. Para eso se usa un `imagePullSecret`, que conceptualmente responde a esta idea:

```text
Kubernetes no puede descargar una imagen privada si no conoce credenciales vélidas para el registry.
```

Por eso en los charts se añadió soporte a `image.pullSecrets`, y en el namespace de despliegue se creó un secret del tipo `docker-registry`.

### 16.9 Problemas reales de esta fase

Esta parte del proyecto fue especialmente rica en troubleshooting. Entre los problemas resueltos destacan:

- errores al construir el `config.json` de Kaniko
- autenticación del registry contra GitLab web
- resolución DNS incorrecta dentro del clúster
- necesidad de confiar en la CA del registry desde el nodo Minikube
- pods bloqueados en `ErrImageNeverPull`

Este bloque es muy valioso en una memoria didáctica porque enseña que el CD real tiene varias capas:

1. construir la imagen  
2. empujarla al registry  
3. permitir que Kubernetes pueda descargarla  
4. conseguir que el deployment haga rollout correctamente  

### 16.10 Imagen recomendada en esta sección

Figura sugerida: `54-gitlab-container-registry.png`  
Qué debería mostrar:

- imágenes por microservicio
- tags por SHA

Figura sugerida adicional: `60-k8s-image-by-sha-user-service.png`  
Qué debería mostrar:

- imagen final del deployment apuntando al SHA del commit

---

## 18. Troubleshooting real: problemas y correcciones

Uno de los mayores valores de este proyecto está en los problemas que obligó a resolver. A continuación se resumen algunos de los más relevantes.

### 17.1 Flyway y migraciones

- modificación de migraciones ya aplicadas
- necesidad de reparar historial
- alineación de pruebas con PostgreSQL real

### 17.2 Dashboards con datasource incorrecto

- dashboards importados de Grafana con `${DS_PROMETHEUS}`
- necesidad de adaptar datasource y UID reales

### 17.3 Kibana y TLS

- acceso erróneo por HTTP
- necesidad de usar HTTPS y aceptar certificado autofirmado

### 17.4 Logs poco útiles

- primeros logs demasiado centrados en infraestructura
- introducción posterior de logs de negocio
- migración a ECS JSON

### 17.5 Problemas de CI/CD

- problemas de clon del runner a la URL pública
- errores 413 en upload de artifacts
- RBAC insuficiente para despliegues
- sintaxis incorrecta en creación de `config.json` para Kaniko
- problemas de resolución DNS entre runner, GitLab web y registry
- errores `ErrImageNeverPull`
- necesidad de instalar la CA del registry en Minikube

### 17.6 Problemas internos de GitLab

En una fase avanzada apareció una incidencia especialmente interesante: algunas pantallas de GitLab devolvían error `500`. El diagnóstico mostró que el problema real no estaba en el navegador, sino en Gitaly.

El init container `configure` de Gitaly fallaba al copiar:

```text
.gitlab_shell_secret -> /init-secrets/shell/.gitlab_shell_secret
```

por un `Permission denied`.

La solución mínima consistió en forzar el init container de Gitaly a ejecutarse como `root`:

```yaml
gitlab:
  gitaly:
    init:
      containerSecurityContext:
        runAsUser: 0
        runAsGroup: 0
        runAsNonRoot: false
```

Este episodio tiene gran valor pedagógico porque ilustra una lección central de plataforma: un error visible en la UI puede tener su origen en un backend interno completamente distinto.

### 17.7 Método de troubleshooting usado en el laboratorio

Más allí de cada incidencia concreta, el proyecto ha servido para consolidar una forma de trabajar muy útil para perfiles junior:

1. observar el síntoma visible  
2. identificar el componente exacto que falla  
3. descartar falsos culpables  
4. localizar el error real en logs o eventos  
5. aplicar el cambio mínimo que resuelve el problema  
6. volver a validar de extremo a extremo  

### 17.8 Ejemplo aplicado a GitLab web

El síntoma visible fue:

- la UI de GitLab devolvía `500`

El error real no estaba en:

- el navegador
- el port-forward
- el ingress

El error estaba en:

- `gitlab-webservice` llamando a Gitaly
- Gitaly fallando en su init container

Este patrón es una lección muy valiosa. En operación real, la causa de un error visible suele estar varias capas por debajo del punto donde se manifiesta.

---

## 19. Resultados finales del laboratorio

En su estado actual, el laboratorio permite demostrar de forma práctica:

- diseño y evolución de microservicios Spring Boot
- persistencia real con PostgreSQL y Flyway
- empaquetado y despliegue con Helm
- operación en Kubernetes con Minikube
- métricas de aplicación y de clúster
- dashboards y alertas en Grafana
- trazas distribuidas con OpenTelemetry y Jaeger
- logs centralizados y estructurados con Elastic
- correlación real entre logs y trazas
- GitLab self-managed dentro del propio laboratorio
- GitLab Runner en Kubernetes
- pipeline CI funcional
- construcción de imágenes con Kaniko
- publicación en GitLab Container Registry
- despliegue por SHA de commit

Esto ya sitúa el proyecto muy por encima de una demo básica. Lo convierte en una plataforma pequeña, pero defendible técnicamente.

---

## 20. Decisiones de diseño y trade-offs

Esta sección es importante porque transforma el documento de una simple narración de hechos en una memoria con criterio técnico. No basta con decir qué se hizo; también conviene explicar por qué se eligió una alternativa y no otra.

### 20.1 Microservicios acoplados por HTTP frente a servicios aislados

Se eligió un flujo con dependencias HTTP reales entre servicios. La razón no fue puramente funcional, sino observacional.

| Opción | Ventaja | Inconveniente |
|---|---|---|
| Servicios aislados | simplicidad | menos valor para trazas y correlación |
| Servicios con dependencias HTTP | más realismo y observabilidad distribuida | más complejidad operativa |

En este laboratorio interesaba más el valor didáctico de la segunda opción.

### 20.2 PostgreSQL único con esquemas frente a méltiples bases

Se eligió una única instancia PostgreSQL con varios esquemas.

Razones:

- menos consumo de recursos en local
- menos complejidad de operación
- aislamiento lógico suficiente para laboratorio

Trade-off:

- no reproduce al cien por cien una separación completa por base de datos
- sí reproduce suficientemente bien versionado, migraciones y persistencia real

### 20.3 Helm frente a manifiestos Kubernetes sueltos

Helm añade una capa más de complejidad que `kubectl apply`, pero aporta ventajas decisivas:

- parametrización por entorno
- plantillas reutilizables
- despliegues repetibles
- mejor integración con CI/CD

Para un proyecto con varios microservicios, Helm es una decisión razonable incluso en laboratorio.

### 20.4 OpenTelemetry solo para trazas

Aunque OpenTelemetry puede participar en métricas y logs, en este laboratorio se decidió separar responsabilidades:

- trazas -> OpenTelemetry
- métricas -> Prometheus
- logs -> ECS JSON + Filebeat + Elasticsearch

La razón es didáctica y operativa:

- simplifica el modelo mental
- evita duplicidad de señales
- permite enseñar mejor qué backend resuelve cada problema

### 20.5 GitLab self-managed dentro de Minikube

Esta decisión añade complejidad, pero también mucho valor formativo.

Ventajas:

- laboratorio autosuficiente
- CI/CD dentro del propio stack
- aprendizaje real de GitLab, Runner y Registry

Costes:

- mayor consumo de recursos
- más troubleshooting interno
- más puntos de fallo que una solución SaaS

### 20.6 Runner en Kubernetes frente a runner en Windows

Se probó conceptualmente la idea de runner en host, pero la mejor decisión para el laboratorio fue runner en Kubernetes.

Ventajas:

- menor dependencia del entorno local Windows
- mejor integración con el clúster
- patrón más cercano a plataforma cloud-native

Inconvenientes:

- requiere resolver RBAC, networking y acceso a registry

### 20.7 GitLab Container Registry frente a Harbor o Nexus

Para este laboratorio se eligió GitLab Container Registry porque:

- ya existía GitLab
- la integración con CI es directa
- reduce piezas de infraestructura

Harbor o Nexus podrían tener sentido en otros contextos:

- Harbor, si el foco fuera un registry dedicado como producto de plataforma
- Nexus, si el foco fuera repositorio universal de artefactos y no solo imágenes

---

## 21. Diferencias entre este laboratorio y un entorno de producción

Una memoria madura debe dejar claro qué partes son vélidas como aprendizaje y qué partes, en cambio, son simplificaciones de laboratorio.

### 21.1 Aspectos simplificados por ser entorno local

- un único nodo Minikube
- storage simplificado
- certificados autofirmados
- `ClusterRoleBinding` amplio para el runner
- port-forwards y entradas locales en `hosts`
- GitLab y observabilidad dentro del mismo clúster

### 21.2 Qué cambiaría en un entorno real

| área | En laboratorio | En producción |
|---|---|---|
| Kubernetes | Minikube de un nodo | clúster multi-nodo gestionado |
| Storage | PVCs simples | storage class y backups gestionados |
| GitLab | self-managed en el mismo lab | instancia dedicada o SaaS |
| Certificados | self-signed | PKI o certificados públicos |
| RBAC | permisos amplios por simplicidad | permisos mínimos por función |
| Despliegue | deploy manual por pipeline | integración GitOps o políticas por entorno |
| Observabilidad | único stack local | separación por entornos, retención y sizing reales |

### 21.3 Qué sí es transferible a producción

No todo es "solo de laboratorio". Hay aprendizajes totalmente transferibles:

- estructura de microservicios
- uso de Helm
- separación de señales de observabilidad
- estrategia de logs estructurados
- uso de GitLab Runner en Kubernetes
- build por SHA y despliegue versionado
- troubleshooting por capas

### 21.4 Qué gana el lector con esta comparación

Esta sección evita un error frecuente en memorias técnicas: presentar un entorno local como si fuera producción. Lo correcto es distinguir:

- qué es concepto transferible
- qué es implementación de laboratorio
- qué habría que rediseñar si el sistema creciera

---

## 22. Troubleshooting resumido en formato operativo

Además de la narración completa, conviene dejar una tabla compacta que sirva como referencia rápida.

| Síntoma | Causa real | Cómo se diagnosticó | Solución aplicada |
|---|---|---|---|
| Dashboards importados fallaban en Grafana | datasource no resuelto | revisión del JSON y del `uid` del datasource | fijar `uid: prometheus` y adaptar dashboards |
| Kibana no abría correctamente | acceso HTTP sobre servicio TLS | prueba directa a la URL y comportamiento del navegador | usar HTTPS y aceptar certificado |
| Filebeat no aportaba logs útiles | logs de aplicación poco estructurados | análisis en Discover y campos disponibles | introducir logging de negocio y ECS JSON |
| Tests requerían más infraestructura en CI | uso de Testcontainers | revisión de `src/test/resources/application.properties` | runner con soporte de Docker |
| Kaniko fallaba autenticando | `config.json` incorrecto o resolución DNS mala | logs del job de imagen | corregir generación del config y mapeos de host |
| Deploy bloqueado en `ErrImageNeverPull` | `pullPolicy`/secret incorrectos | `kubectl describe pod` | forzar `IfNotPresent` e `imagePullSecrets` |
| Pull desde registry fallaba por TLS | nodo no confiaba en la CA | logs del pod e imagen pull | instalar CA del registry en Minikube |
| GitLab web devolvía `500` | Gitaly roto internamente | logs de webservice y pod de Gitaly | elevar permisos del init container de Gitaly |

### 22.1 Cómo usar esta tabla

Esta tabla está pensada para dos usos:

- como resumen rápido en una defensa oral
- como referencia práctica para un lector que quiera repetir el laboratorio

En una presentación, esta sección suele tener mucho impacto porque demuestra método, no solo resultado.

---

## 23. Valor del proyecto para un perfil junior DevOps u observabilidad

Este laboratorio es especialmente útil para perfiles junior porque conecta teoría con práctica. Un lector que recorra el proyecto aprende no solo qué herramientas existen, sino:

- para qué se usan
- cómo se conectan entre sí
- qué errores aparecen en la realidad
- cómo se diagnostican y corrigen

Desde una perspectiva formativa, el proyecto enseña varias ideas fundamentales:

1. observar no es solo instalar herramientas  
2. automatizar no es solo tener un YAML  
3. desplegar no es solo crear pods  
4. el troubleshooting es parte central del trabajo  

Esa combinación es justamente la que diferencia una práctica superficial de una experiencia más próxima a un entorno profesional.

---

## 24. Conclusiones

La construcción de este laboratorio demuestra que un proyecto de ingeniería útil no se mide por el número de herramientas instaladas, sino por la coherencia del sistema resultante y por la capacidad de explicar cómo y por qué funciona.

La plataforma final integra desarrollo backend, despliegue, observabilidad y automatización dentro de un mismo entorno. A lo largo del proceso se resolvieron problemas reales de red, permisos, TLS, CI/CD, logging, trazado y componentes internos de GitLab. Esa acumulación de experiencia práctica es precisamente lo que da valor al proyecto.

Como memoria técnica, el laboratorio permite defender:

- decisiones de arquitectura
- decisiones de operación
- evolución por fases
- troubleshooting real
- resultados medibles

Como material docente, permite enseñar a un perfil junior no solo un conjunto de tecnologías, sino la lógica que existe detrás de una plataforma observable y automatizada.

---

## 25. Líneas futuras

Aunque el proyecto ya está en un punto muy sélido, existen varias extensiones razonables:

- añadir smoke tests post-deploy
- introducir rollback manual o automatizado
- versionar dashboards y alertas como código de forma más estricta
- añadir anotaciones de despliegue con SHA, pipeline y versión
- incorporar dashboards específicos por servicio y tipo de error
- explorar una integración GitOps con Argo CD
- introducir un registry o un patrón alternativo como Harbor en una rama experimental

Estas líneas futuras no son necesarias para validar el laboratorio, pero sí servirían para ampliarlo hacia un ecosistema todavía más cercano a entornos corporativos complejos.

---

## 26. Recomendaciones para la versión final del documento

Para convertir esta memoria en una versión final de entrega o presentación, conviene incorporar capturas en puntos concretos:

- arquitectura general del sistema
- pods y servicios en Kubernetes
- dashboard principal de Grafana
- traza en Jaeger
- logs estructurados en Kibana
- alerta `Firing` en Grafana
- alerta activa en Kibana
- GitLab local operativo
- runner online
- pipeline completa en verde
- Container Registry con imágenes por SHA
- deployments usando imágenes del registry

También sería recomendable generar una versión PDF final con:

- portada formal
- Índice automático
- numeración de secciones
- figuras y tablas numeradas

---

## 27. Anexo A: comandos representativos

### Despliegue de aplicación

```powershell
.\scripts\deploy-minikube.ps1
```

### Despliegue de observabilidad

```powershell
.\scripts\deploy-observability.ps1
```

### Instalación de ECK

```powershell
.\scripts\install-eck.ps1
```

### Estado de pods

```powershell
kubectl get pods -A
```

### Rollout de deployments

```powershell
kubectl rollout status deployment/user-service -n default
kubectl rollout status deployment/api-gateway -n default
```

### Pipeline GitLab

La pipeline principal está definida en:

```text
.gitlab-ci.yml
```

e incluye build, test, package, validación Helm, build de imágenes y deploy manual.

---

## 28. Anexo B: mensaje final del proyecto

La idea central de este laboratorio puede resumirse así:

> construir una plataforma pequeña, pero suficientemente realista, para aprender a desplegar, observar, diagnosticar y automatizar microservicios de forma integral.

Ese objetivo se ha cumplido. El siguiente paso ya no es "tener más herramientas", sino documentar bien lo construido, enseñarlo con claridad y defender técnicamente las decisiones tomadas.

