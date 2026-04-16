# E-Commerce API

API REST construida con Spring Boot 3 y SQL Server.

---

## Inicio rápido (Make)

Si tenés `make` instalado, es la forma más simple de levantar todo:

```bash
make start-all    # primera vez: levanta Docker, inicializa la DB y corre la app
make start-app    # si Docker ya está corriendo
make stop-db      # baja el contenedor
make wipe-db      # baja el contenedor y BORRA los datos
make run-tests    # corre los tests (no necesita Docker)
```

Corré `make` sin argumentos para ver todos los comandos disponibles.

---

## Requisitos

- Java 17+
- Maven (o usar `./mvnw`)
- SQL Server Express (local) **o** Docker

---

## Opciones de base de datos

### Opcion A — SQL Server Express (instalacion local, Windows)

No requiere ninguna configuracion extra. La app usa el perfil por defecto.

```bash
./mvnw spring-boot:run
```

Credenciales por defecto definidas en `application.properties`:
- Usuario: `appuser`
- Password: `App12345!`
- Instancia: `localhost\SQLEXPRESS`
- Base de datos: `E-commerce`

---

### Opcion B — Docker (Mac/Linux/Windows con Docker Desktop)

**1. Levantar el contenedor de SQL Server:**

```bash
docker compose up -d
```

Espera ~30 segundos a que SQL Server termine de inicializar.

**2. Crear la base de datos y el usuario** (solo la primera vez):

```bash
docker exec -it ecommerce-sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong@Passw0rd' -No \
  -Q "CREATE DATABASE [E-commerce]; \
      CREATE LOGIN appuser WITH PASSWORD='App12345!'; \
      USE [E-commerce]; \
      CREATE USER appuser FOR LOGIN appuser; \
      ALTER ROLE db_owner ADD MEMBER appuser;"
```

**3. Correr la app con el perfil docker:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

O con variable de entorno:

```bash
SPRING_PROFILES_ACTIVE=docker ./mvnw spring-boot:run
```

**Detener el contenedor:**

```bash
docker compose down
```

**Borrar datos (volumen):**

```bash
docker compose down -v
```

---

## Variables de entorno

Se pueden sobreescribir las credenciales sin tocar el codigo:

| Variable | Descripcion | Default (docker) |
|----------|-------------|-----------------|
| `DB_URL`  | JDBC URL completa | `localhost:1433` TCP |
| `DB_USER` | Usuario de la DB | `sa` |
| `DB_PASS` | Password de la DB | `YourStrong@Passw0rd` |

Copia `.env.example` como `.env` y ajusta los valores (el `.env` no se sube al repo).

---

## Conectarse desde un cliente SQL (IntelliJ / DBeaver / SSMS)

| Campo | Valor |
|-------|-------|
| Host | `localhost` |
| Puerto | `1433` |
| Usuario | `sa` |
| Password | `YourStrong@Passw0rd` |
| Base de datos | `E-commerce` |
| Driver | SQL Server (mssql-jdbc) |

En IntelliJ: `Database` > `+` > `Data Source` > `Microsoft SQL Server` > completar los campos anteriores.

---

## Tests

Los tests corren con H2 (base de datos en memoria) y no requieren ninguna DB externa. Funcionan en cualquier maquina sin configuracion adicional.

```bash
./mvnw test
```

El archivo `src/test/resources/application.properties` configura H2 automaticamente para el scope de test. No afecta el perfil de desarrollo ni produccion.

---

## Colección Postman

Importar `postman_collection.json` (raíz del proyecto) en Postman para tener todos los endpoints listos con ejemplos de request body.

1. Postman → **Import** → seleccionar `postman_collection.json`
2. La variable `{{baseUrl}}` ya está configurada como `http://localhost:8080`. Se puede cambiar desde **Environments** o directamente en la colección sin tocar los requests.

---

## Endpoints disponibles

La app corre en `http://localhost:8080`.

### Autenticación

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/auth/register` | Registrar nuevo usuario (rol: buyer) | No |
| `POST` | `/auth/login` | Iniciar sesión, devuelve JWT | No |
| `POST` | `/auth/logout` | Cerrar sesión (invalida la sesión en DB) | Bearer token |
| `GET` | `/users/me` | Obtener perfil del usuario autenticado | Bearer token |

El token JWT se incluye en el header `Authorization: Bearer <token>`. Expira en 24 horas (configurable con `JWT_SECRET` y `jwt.expiration-ms`).

### Módulos base

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/users` | Crear usuario |
| `POST` | `/categories` | Crear categoría |
| `POST` | `/warehouses` | Crear depósito |
| `POST` | `/addresses` | Crear dirección |
| `POST` | `/products` | Crear producto |
| `POST` | `/variants` | Crear variante |
| `POST` | `/inventory` | Registrar inventario |
| `POST` | `/orders` | Crear pedido |
| `POST` | `/payments` | Registrar pago |
| `POST` | `/reviews` | Crear reseña |

### Entregas

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/deliveries` | Listar todas las entregas |
| `GET` | `/deliveries/{id}` | Obtener entrega por ID |
| `GET` | `/deliveries/order/{orderId}` | Listar entregas de un pedido |
| `POST` | `/deliveries` | Crear entrega |
| `PUT` | `/deliveries/{id}` | Actualizar entrega |
| `DELETE` | `/deliveries/{id}` | Eliminar entrega |

### Seguimiento de envío

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/tracking/{id}` | Obtener checkpoint por ID |
| `GET` | `/tracking/delivery/{deliveryId}` | Listar checkpoints de una entrega |
| `POST` | `/tracking` | Agregar checkpoint |
| `PUT` | `/tracking/{id}/status` | Actualizar estado del checkpoint |

### Devoluciones

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/returns` | Listar todas las devoluciones |
| `GET` | `/returns/{id}` | Obtener devolución por ID |
| `GET` | `/returns/order/{orderId}` | Listar devoluciones de un pedido |
| `POST` | `/returns` | Crear devolución |
| `PUT` | `/returns/{id}` | Actualizar devolución |
| `DELETE` | `/returns/{id}` | Eliminar devolución |

### Reembolsos

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/refunds/{id}` | Obtener reembolso por ID |
| `GET` | `/refunds/return/{returnId}` | Listar reembolsos de una devolución |
| `POST` | `/refunds` | Crear reembolso |
| `PUT` | `/refunds/{id}/status` | Actualizar estado del reembolso |

### Soporte — Tickets y Mensajes

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/support/tickets` | Listar todos los tickets |
| `GET` | `/support/tickets/{id}` | Obtener ticket por ID |
| `POST` | `/support/tickets` | Crear ticket |
| `PUT` | `/support/tickets/{id}/status` | Actualizar estado del ticket |
| `GET` | `/support/tickets/{id}/messages` | Listar mensajes de un ticket |
| `POST` | `/support/tickets/{id}/messages` | Enviar mensaje a un ticket |

### Notificaciones

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/notifications` | Listar todas las notificaciones |
| `GET` | `/notifications/unread` | Listar notificaciones no leídas |
| `PUT` | `/notifications/{id}/read` | Marcar notificación como leída |
