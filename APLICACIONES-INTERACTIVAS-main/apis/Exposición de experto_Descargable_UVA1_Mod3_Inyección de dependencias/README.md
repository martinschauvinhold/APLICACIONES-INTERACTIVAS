# E-Commerce API

API REST construida con Spring Boot 3 y SQL Server.

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

## Endpoints disponibles

La app corre en `http://localhost:8080`. Ver `test-endpoints.http` para ejemplos de todos los endpoints o importar `insomnia-collection.json` en Insomnia.

| Recurso | Ruta |
|---------|------|
| Usuarios | `POST /users` |
| Categorias | `POST /categories` |
| Depositos | `POST /warehouses` |
| Direcciones | `POST /addresses` |
| Productos | `POST /products` |
| Variantes | `POST /variants` |
| Inventario | `POST /inventory` |
| Pedidos | `POST /orders` |
| Pagos | `POST /payments` |
| Entregas | `POST /deliveries` |
| Devoluciones | `POST /returns` |
| Resenas | `POST /reviews` |
