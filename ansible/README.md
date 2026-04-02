# Ansible en este laboratorio

## Para qué lo añadimos

En este proyecto Ansible no sustituye a Kubernetes, Helm ni GitLab CI.
Se usa como capa de automatización para:

- validar prerrequisitos del entorno
- desplegar la plataforma base
- desplegar las aplicaciones con Helm
- comprobar que el laboratorio ha quedado sano

La separación queda así:

- `Kubernetes`: ejecuta los pods
- `Helm`: define y despliega releases
- `GitLab CI/CD`: compila, testea, publica imágenes y despliega
- `Ansible`: orquesta el laboratorio y automatiza tareas repetibles

## Por qué recomiendo ejecutarlo desde WSL

Ansible funciona mejor en Linux que en Windows. Como tu entorno principal es Windows
pero el laboratorio ya usa Docker, Minikube, Helm y `kubectl`, la opción más limpia es:

1. Instalar WSL con Ubuntu.
2. Instalar Ansible dentro de Ubuntu.
3. Ejecutar los playbooks desde el directorio del repo.

Así evitas pelearte con limitaciones del control node de Ansible en Windows.

## Instalación recomendada

Dentro de Ubuntu en WSL:

```bash
sudo apt update
sudo apt install -y ansible
```

Comprueba que funciona:

```bash
ansible --version
```

## Estructura

```text
ansible/
  ansible.cfg
  group_vars/all.yml
  inventories/local/hosts.yml
  playbooks/
    bootstrap.yml
    platform.yml
    apps.yml
    validate.yml
```

## Qué hace cada playbook

### `bootstrap.yml`

Sirve para validar que el entorno de trabajo tiene lo mínimo:

- `kubectl`
- `helm`
- `minikube`
- contexto de Kubernetes activo

No instala paquetes del sistema operativo. En esta primera versión lo usamos como
playbook de verificación de control node.

Ejecuta:

```bash
cd /ruta/al/repo/ansible
ansible-playbook playbooks/bootstrap.yml
```

### `platform.yml`

Despliega la plataforma base del laboratorio:

- namespace de observabilidad
- operador ECK
- Elasticsearch, Kibana y Filebeat
- Jaeger
- OpenTelemetry Collector
- Prometheus
- Grafana
- kube-state-metrics
- node-exporter
- GitLab
- GitLab Runner
- RBAC del runner

También puede crear el `imagePullSecret` del registry si rellenas estas variables en
`group_vars/all.yml` o en un fichero local no versionado:

- `registry_pull_username`
- `registry_pull_password`

Ejecuta:

```bash
cd /ruta/al/repo/ansible
ansible-playbook playbooks/platform.yml
```

### `apps.yml`

Despliega PostgreSQL y los cuatro microservicios usando los charts Helm locales y los
values de Minikube.

Ejecuta:

```bash
cd /ruta/al/repo/ansible
ansible-playbook playbooks/apps.yml
```

### `validate.yml`

Muestra un resumen operativo del estado del laboratorio:

- pods de observabilidad
- pods de GitLab
- pods del runner
- pods de aplicación
- servicios de aplicación

Ejecuta:

```bash
cd /ruta/al/repo/ansible
ansible-playbook playbooks/validate.yml
```

## Orden recomendado

Si partes de un entorno ya preparado, el flujo normal sería:

```bash
cd /ruta/al/repo/ansible
ansible-playbook playbooks/bootstrap.yml
ansible-playbook playbooks/platform.yml
ansible-playbook playbooks/apps.yml
ansible-playbook playbooks/validate.yml
```

## Qué gana el proyecto con esto

Antes:

- varios scripts PowerShell
- pasos manuales
- conocimiento repartido

Ahora:

- una capa declarativa adicional de automatización
- un punto único para levantar y validar el laboratorio
- una historia más fuerte para entrevistas: no solo desplegaste la plataforma, también
  la orquestaste de forma reproducible

## Límites de esta primera versión

Esta implementación es intencionadamente mínima:

- no instala Docker, Helm o Minikube en el host
- no reemplaza tus pipelines GitLab
- no reemplaza tus charts Helm
- no hace todavía smoke tests HTTP ni port-forward automático

Es la base correcta para crecer después sin duplicar lógica.
