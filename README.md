# Cloud Native App

## Semana 03 - Sistema de Gestion de Pedidos y Guias de Despacho

Este proyecto reutiliza la base tecnica de las entregas anteriores para cumplir la entrega de la Semana 03.
La API final queda centrada en la gestion de guias de despacho, generacion de archivos PDF, almacenamiento temporal en EFS y almacenamiento definitivo en AWS S3.

## Objetivo de Semana 03

La aplicacion representa a una empresa transportista que necesita:

- registrar el pedido logistico asociado a cada guia
- registrar guias de despacho
- generar archivos PDF de cada guia
- guardar temporalmente esos archivos en una ruta compatible con EFS
- subir las guias a S3 en carpetas organizadas por fecha y transportista
- descargar guias desde S3 validando un codigo de permiso
- actualizar y eliminar guias
- consultar el historial por transportista y fecha

## Tecnologias usadas

- Java 17
- Spring Boot 4.0.6
- Spring Web
- Spring Data JPA
- Spring Validation
- Oracle Database
- H2 para pruebas
- AWS SDK for Java 2.x
- Apache PDFBox
- Docker
- Docker Compose
- GitHub Actions
- Docker Hub
- Amazon EC2
- Amazon EFS
- Amazon S3

## Endpoints finales

Base principal:

```text
/api/guias
```

Endpoints:

- `GET /api/guias`
- `GET /api/guias?transportista=Transportes del Norte&fecha=2026-06-07`
- `GET /api/guias/{id}`
- `POST /api/guias`
- `POST /api/guias/{id}/subir-s3`
- `GET /api/guias/{id}/descargar-s3?codigoPermiso=PERMISO-123`
- `PUT /api/guias/{id}`
- `DELETE /api/guias/{id}`

## Flujo funcional principal

1. Se crea una guia de despacho.
2. La aplicacion genera un archivo PDF temporal.
3. El PDF queda almacenado en una ruta compatible con EFS.
4. Cuando se invoca el endpoint de subida, el archivo se publica en S3.
5. La key de S3 se organiza por fecha y transportista normalizado.
6. La descarga desde S3 requiere `codigoPermiso`.

## Formato del PDF

Cada guia se genera con extension `.pdf` y contiene como minimo:

- titulo `Guia de Despacho`
- numero de guia
- numero de pedido
- transportista
- fecha
- origen
- destino
- destinatario
- descripcion de carga
- estado

Nombre del archivo:

```text
guia-{id}.pdf
```

Ejemplo:

```text
guia-1.pdf
```

## Uso de EFS como almacenamiento temporal

Propiedad usada por la aplicacion:

```properties
app.efs.guias-path=${GUIAS_EFS_PATH:archivos/guias}
```

Rutas esperadas:

- Local: `archivos/guias`
- EC2 y Docker: `/app/efs/guias`

Montaje en EC2:

```text
/mnt/efs:/app/efs
```

Antes de desplegar en EC2, el EFS debe estar montado manualmente en:

```text
/mnt/efs
```

## Estructura S3

La guia se almacena en S3 con la estructura:

```text
{fechaGuia}/{transportista-normalizado}/guia-{id}.pdf
```

Ejemplo:

```text
2026-06-07/transportes-del-norte/guia-1.pdf
```

## Variables de entorno

Variables principales:

- `GUIAS_EFS_PATH`
- `AWS_S3_BUCKET_NAME`
- `AWS_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SESSION_TOKEN`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`

Ejemplo local:

```bash
export DB_URL=jdbc:oracle:thin:@servidor:1521/servicio
export DB_USERNAME=usuario
export DB_PASSWORD=clave
export SERVER_PORT=8080
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=tu_access_key
export AWS_SECRET_ACCESS_KEY=tu_secret_key
export AWS_SESSION_TOKEN=tu_session_token
export AWS_S3_BUCKET_NAME=tu_bucket
export GUIAS_EFS_PATH=archivos/guias
```

## Ejemplo de creacion de guia

`POST /api/guias`

```json
{
  "numeroGuia": "GD-001",
  "numeroPedido": "PED-001",
  "transportista": "Transportes del Norte",
  "fechaGuia": "2026-06-07",
  "origen": "Santiago",
  "destino": "Antofagasta",
  "destinatario": "Cliente Demo",
  "descripcionCarga": "Equipos electricos",
  "estado": "En transito",
  "codigoPermisoDescarga": "PERMISO-123"
}
```

## Ejemplo de respuesta

```json
{
  "id": 1,
  "numeroGuia": "GD-001",
  "numeroPedido": "PED-001",
  "transportista": "Transportes del Norte",
  "fechaGuia": "2026-06-07",
  "origen": "Santiago",
  "destino": "Antofagasta",
  "destinatario": "Cliente Demo",
  "descripcionCarga": "Equipos electricos",
  "estado": "En transito",
  "rutaTemporalEfs": "/ruta/proyecto/archivos/guias/guia-1.pdf",
  "bucketS3": null,
  "keyS3": null,
  "nombreArchivo": "guia-1.pdf",
  "subidaS3": false
}
```

## Pasos de prueba en Postman

1. Crear guia:

```http
POST /api/guias
```

2. Consultar todas las guias:

```http
GET /api/guias
```

3. Consultar por transportista y fecha:

```http
GET /api/guias?transportista=Transportes del Norte&fecha=2026-06-07
```

4. Subir la guia a S3:

```http
POST /api/guias/1/subir-s3
```

5. Descargar desde S3 con permiso valido:

```http
GET /api/guias/1/descargar-s3?codigoPermiso=PERMISO-123
```

6. Actualizar la guia:

```http
PUT /api/guias/1
```

7. Eliminar la guia:

```http
DELETE /api/guias/1
```

## Ejecucion local

Dar permisos al wrapper si hace falta:

```bash
chmod +x mvnw
```

Ejecutar pruebas:

```bash
./mvnw clean test
```

Empaquetar:

```bash
./mvnw clean package
```

Levantar la aplicacion:

```bash
./mvnw spring-boot:run
```

## Docker y EC2

La aplicacion mantiene el despliegue con:

- imagen en Docker Hub
- doble tag `latest` y `${github.sha}`
- despliegue por SSH a EC2
- `docker compose pull`
- `docker compose up -d`

En EC2 se usa:

- carpeta de despliegue `/home/ec2-user/cloudnativeapp`
- variable `GUIAS_EFS_PATH=/app/efs/guias`
- montaje `/mnt/efs:/app/efs`

## Comandos de referencia para montar EFS en EC2

```bash
sudo mkdir -p /mnt/efs
sudo mount -t nfs4 -o nfsvers=xx,rsize=xx,wsize=xx,hard,timeo=xx,retrans=xx,noresvport fs-xxxxxxxx:/ /mnt/efs
sudo mkdir -p /mnt/efs/guias
```

## Pipeline CI/CD

El workflow `.github/workflows/deploy.yml` mantiene:

1. `actions/checkout@v4`
2. `actions/setup-java@v4`
3. `./mvnw clean test`
4. validacion de `secrets`
5. `docker/login-action@v3`
6. `docker build`
7. `docker push`
8. despliegue por SSH a EC2

Secrets usados:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `EC2_HOST`
- `EC2_SSH_KEY`
- `USER_SERVER`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`
- `AWS_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SESSION_TOKEN`
- `AWS_S3_BUCKET_NAME`

## Historial tecnico relevante

### Mejoras aplicadas en Semana 02

- Login seguro dentro de EC2 con `docker login --password-stdin`
- Publicacion de dos tags de imagen:
  - `latest`
  - `${github.sha}`
- Despliegue con `docker compose pull` y `docker compose up -d`
- Archivo `.env` generado en EC2 con variables de entorno
- Uso de carpeta de despliegue dedicada en `/home/ec2-user/cloudnativeapp`

### Ajustes aplicados al inicio de Semana 03

- Validacion temprana de `secrets` en GitHub Actions
- Manejo de errores S3 con mensajes mas claros para permisos, conectividad y objetos inexistentes
- Reutilizacion de la base AWS, Docker y CI/CD sobre el nuevo dominio de guias