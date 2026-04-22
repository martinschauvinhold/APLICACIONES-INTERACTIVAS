# E-Commerce API

API REST construida con Spring Boot 3 y SQL Server.

---

## Inicio rápido (Make)

Si tenés `make` instalado, es la forma más simple de levantar todo:

```bash
# Primera vez (dos pasos, en terminales separadas):
make start-all    # levanta Docker, inicializa la DB y corre la app
make seed-db      # en otra terminal, una vez que veas "Started DemoApplication"

# Uso normal:
make start-app    # corre la app si Docker ya está corriendo
make stop-db      # baja el contenedor
make wipe-db      # baja el contenedor y BORRA los datos
make run-tests    # corre los tests (no necesita Docker)
```

Corré `make` sin argumentos para ver todos los comandos disponibles.

> **Por qué dos pasos:** Hibernate crea las tablas al arrancar la app por primera vez (`ddl-auto=update`). El `seed-db` necesita que la tabla `USERS` exista, así que se corre después de que la app levantó por primera vez. En arranques posteriores no hace falta volver a seedear (los INSERTs son idempotentes vía `IF NOT EXISTS`).

---

## Credenciales de desarrollo

Después de correr `make seed-db`, la base queda con dos usuarios listos para usar:

| Rol | Email | Password |
|-----|-------|----------|
| `admin` | `admin@mail.com` | `Test1234!` |
| `seller` | `seller_test@test.com` | `Test1234!` |

> ⚠️ **Solo para desarrollo.** Estas credenciales están versionadas en el repositorio (`seed-users.sql`) para facilitar el setup en un proyecto académico. **No usarlas en un ambiente productivo.** Si en algún momento el proyecto se despliega, el primer paso es eliminar estos usuarios y crear admins reales con contraseñas seguras.

Para registrar un usuario `buyer` desde la API: `POST /auth/register` (no requiere autenticación).

---

## Documentación funcional

Para entender qué hace cada endpoint, qué errores devuelve y cómo funcionan los flujos de negocio (orders + cupones + pagos), ver **[MANUAL.md](MANUAL.md)**.

Cubre:
- Mapa completo de endpoints con nivel de acceso (público / autenticado / por rol)
- Significado de los códigos 401, 403, 422, 429 y cuándo aparece cada uno
- Decisiones de negocio clave: cómo se aplican cupones, cuándo se descuenta el stock, qué pasa al cancelar
- Flujo end-to-end de compra con ejemplos curl (login → cupón → orden → pago)
- Cómo forzar errores para probar (incluido `?simulateFailure=true` en pagos)

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

## Roles y creación de usuarios

Hay tres roles: `buyer`, `seller`, `admin`.

| Rol | Quién lo crea |
|-----|--------------|
| `buyer` | Cualquiera via `POST /auth/register` (rol fijo, no elegible) |
| `seller` | Un admin via `POST /users` con `"role": "seller"` |
| `admin` | Un admin via `POST /users` con `"role": "admin"` |

### Crear el primer admin

Ya está cubierto por `make seed-db` (ver sección [Credenciales de desarrollo](#credenciales-de-desarrollo)). Una vez que estás logueado con `admin@mail.com`, podés crear más admins/sellers desde la API:

```http
POST /users
Authorization: Bearer <token_admin>

{
  "username": "vendedor1",
  "email": "vendedor1@mail.com",
  "password": "password123",
  "role": "seller"
}
```

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
