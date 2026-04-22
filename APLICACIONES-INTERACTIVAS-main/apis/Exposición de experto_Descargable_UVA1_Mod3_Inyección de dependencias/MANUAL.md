# Manual funcional — E-Commerce API

Documento orientado a **usar** la API: qué endpoints existen, qué errores pueden devolver, cómo funcionan los flujos de negocio (orders, cupones, pagos) y cómo probar todo en pocos pasos.

> Para **levantar** el proyecto, ver el [README.md](README.md). Este manual asume que la app ya corre en `http://localhost:8080` y que ya corriste `make seed-db` (existen los usuarios `admin@mail.com` y `seller_test@test.com`).

---

## Tabla de contenidos

1. [Introducción](#1-introducción)
2. [Roles del sistema](#2-roles-del-sistema)
3. [Autenticación](#3-autenticación)
4. [Códigos de respuesta](#4-códigos-de-respuesta)
5. [Mapa de endpoints](#5-mapa-de-endpoints)
6. [Decisiones de negocio](#6-decisiones-de-negocio)
7. [Cómo probar fácilmente](#7-cómo-probar-fácilmente)
8. [Limitaciones y trabajo futuro](#8-limitaciones-y-trabajo-futuro)

---

## 1. Introducción

API REST de e-commerce construida con **Spring Boot 3** y **SQL Server** (H2 en tests). Implementa autenticación JWT con sesiones persistidas en DB, control de acceso por roles, rate limiting por IP y los módulos típicos de un comercio: catálogo, pedidos, pagos, entregas, devoluciones y soporte.

Stack relevante: Spring Web + Spring Security + Spring Data JPA + Lombok. La capa de pagos es **simulada** (mock procesador, sin integración con gateway real).

---

## 2. Roles del sistema

| Rol | Quién lo crea | Qué puede hacer (resumen) |
|-----|---------------|---------------------------|
| `buyer` | Cualquiera vía `POST /auth/register` (rol fijo, no elegible) | Comprar (crear órdenes), pagar, dejar reseñas, devolver pedidos, abrir tickets de soporte. |
| `seller` | Un admin vía `POST /users` con `"role": "seller"` | Publicar productos y variantes, gestionar inventario y depósitos, registrar entregas y checkpoints de tracking. |
| `admin` | Sembrado con `make seed-db` (`admin@mail.com`); puede crear más vía `POST /users` | Control total: además gestiona descuentos, cupones, refunds, cancela órdenes ajenas. |

Los roles se almacenan en `USERS.role` como enum (`@Enumerated(EnumType.STRING)`). Un usuario tiene **un solo rol** (la decisión de multi-rol está pospuesta — ver [Limitaciones](#8-limitaciones-y-trabajo-futuro)).

---

## 3. Autenticación

### Obtener token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@mail.com","password":"Test1234!"}'
```

**Respuesta 200 OK:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

El token es un JWT firmado con `JWT_SECRET` (env var). Dura **24 horas** por defecto (`jwt.expiration-ms`).

### Usar token

Incluir el header `Authorization: Bearer <token>` en cada request a un endpoint protegido:

```bash
curl http://localhost:8080/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Sesiones y logout

Cada login crea una fila en la tabla `SESSIONS`. El filtro `JwtAuthFilter` valida en cada request que la sesión siga activa. Esto permite invalidar tokens del lado del servidor antes de su expiración natural.

`POST /auth/logout` cierra **todas las sesiones** del usuario, no solo la actual. Es decisión de negocio (priorizar el caso "pánico"); ver [DECISIONES_PENDIENTES.md #3](DECISIONES_PENDIENTES.md) para el contexto y la iteración futura prevista.

### Registro (solo buyers)

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan_perez",
    "email": "juan@mail.com",
    "password": "Password123",
    "firstName": "Juan",
    "lastName": "Pérez",
    "phone": "1144556677"
  }'
```

El password debe tener al menos 8 caracteres. El rol se fuerza a `buyer`. Para crear `seller` o `admin` hace falta un admin logueado y `POST /users`.

---

## 4. Códigos de respuesta

El `GlobalExceptionHandler` mapea las excepciones de dominio a códigos HTTP consistentes en toda la API.

| Código | Cuándo aparece | Disparador típico |
|--------|----------------|-------------------|
| **200 OK** | GET/PUT exitosos. | — |
| **201 Created** | POST que crea un recurso. | `POST /orders`, `POST /products`, etc. |
| **204 No Content** | DELETE exitosos. | `DELETE /orders/{id}` |
| **400 Bad Request** | Body inválido (validaciones `@Valid` en DTOs). | `code` vacío en `CouponRequest`, password < 8 chars. |
| **401 Unauthorized** | **Sin autenticar**: no hay header `Authorization`, el token es inválido, expirado, o la sesión fue cerrada. | Cualquier endpoint protegido sin token. Vía `authenticationEntryPoint` en `SecurityConfig`. |
| **403 Forbidden** | **Autenticado pero sin permiso**: token válido pero el rol no alcanza para el endpoint. | Buyer intentando `POST /products` (requiere seller/admin). Vía `accessDeniedHandler` + `GlobalExceptionHandler.handleForbidden()`. |
| **404 Not Found** | Recurso inexistente. | `GET /products/9999`, `couponCode` inexistente. Lanza `NotFoundException`. |
| **409 Conflict** | Duplicado. | `POST /coupons` con un `code` que ya existe (`DuplicateException`). |
| **422 Unprocessable Entity** | Regla de negocio violada. El body es válido pero no se puede ejecutar. | Cupón expirado, cupón sin usos restantes, stock insuficiente, cancelar una orden ya cancelada. Lanza `BusinessRuleException`. |
| **429 Too Many Requests** | Rate limit superado. | 11+ requests/min a `/auth/*` o 101+ requests/min al resto, por IP. Vía `RateLimitFilter`. |
| **500 Internal Server Error** | Error inesperado. Si lo ves en pruebas, es un bug. | — |

### 401 vs 403 — la diferencia importante

- **401**: el sistema no sabe quién sos. Solución: loguearte o renovar el token.
- **403**: el sistema sabe quién sos pero no te corresponde hacer eso. Solución: usar una cuenta con el rol adecuado.

---

## 5. Mapa de endpoints

Acceso:
- 🌐 **Público** — `permitAll()`, no requiere token.
- 🔒 **Autenticado** — requiere token válido (cualquier rol).
- 👤 **buyer / seller / admin** — requiere ese rol específico (`@PreAuthorize`).

> Las decisiones de acceso por endpoint están consolidadas — no hay endpoints de escritura sin protección. (Las protecciones que faltaban se cerraron en TESTING.md #46/#47/#48.)

### Auth

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/auth/register` | 🌐 | Registrar buyer. |
| POST | `/auth/login` | 🌐 | Devuelve JWT. |
| POST | `/auth/logout` | 🔒 | Cierra **todas** las sesiones del usuario. |

### Users

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/users` | 👤 admin | Lista todos. |
| GET | `/users/me` | 🔒 | Perfil del usuario autenticado. |
| GET | `/users/{id}` | 👤 admin | Por ID. |
| POST | `/users` | 👤 admin | Crear usuario (puede ser seller o admin). |
| PUT | `/users/{id}` | 👤 admin | Actualizar. |
| DELETE | `/users/{id}` | 👤 admin | Eliminar. |

### Catálogo (público para lectura)

Los `GET` de **categories**, **products**, **variants** y **reviews** son públicos por decisión: facilita la vista de catálogo sin login (patrón estándar Mercado Libre / Amazon). Ver [DECISIONES_PENDIENTES.md #1](DECISIONES_PENDIENTES.md).

#### Categories

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/categories` | 🌐 | Admin ve todas; resto ve solo activas. |
| GET | `/categories/{id}` | 🌐 | Por ID. |
| POST | `/categories` | 👤 admin | Crear. |
| PUT | `/categories/{id}` | 👤 admin | Actualizar. |
| PATCH | `/categories/{id}/deactivate` | 👤 admin | Soft delete. |
| DELETE | `/categories/{id}` | 👤 admin | Eliminar. |

#### Products

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/products` | 🌐 | Listar. |
| GET | `/products/{id}` | 🌐 | Por ID. |
| POST | `/products` | 👤 seller, admin | Crear. |
| PUT | `/products/{id}` | 👤 seller, admin | Actualizar. |
| DELETE | `/products/{id}` | 👤 admin | Eliminar. |

#### Variants

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/variants` | 🌐 | Listar. |
| GET | `/variants/{id}` | 🌐 | Por ID. |
| GET | `/variants/product/{productId}` | 🌐 | Por producto. |
| POST | `/variants` | 👤 seller, admin | Crear. |
| PUT | `/variants/{id}` | 👤 seller, admin | Actualizar. |
| DELETE | `/variants/{id}` | 👤 seller, admin | Eliminar. |

#### Reviews

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/reviews` | 🌐 | Listar. |
| GET | `/reviews/{id}` | 🌐 | Por ID. |
| GET | `/reviews/product/{productId}` | 🌐 | Por producto. |
| POST | `/reviews` | 👤 buyer | Crear. |
| PUT | `/reviews/{id}` | 👤 buyer, admin | Actualizar. |
| DELETE | `/reviews/{id}` | 👤 admin | Eliminar. |

### Coupons & Discounts

#### Coupons

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/coupons` | 🔒 | Listar. |
| GET | `/coupons/{id}` | 🔒 | Por ID. |
| GET | `/coupons/validate/{code}` | 👤 buyer, admin | Valida un código (activo + no expirado + con usos). |
| POST | `/coupons` | 👤 admin | Crear. |
| DELETE | `/coupons/{id}` | 👤 admin | Eliminar. |

#### Discounts

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/discounts` | 🔒 | Listar. |
| GET | `/discounts/{id}` | 🔒 | Por ID. |
| GET | `/discounts/product/{productId}` | 🔒 | Activos para un producto. |
| POST | `/discounts` | 👤 admin | Crear. |
| PUT | `/discounts/{id}` | 👤 admin | Actualizar. |
| DELETE | `/discounts/{id}` | 👤 admin | Eliminar. |

> Sin relación `Product → seller` en el modelo, no se puede restringir a "el seller del producto". Por simplicidad y consistencia con cupones, los descuentos los crea/edita admin.

### Orders, Order Items & Payments

#### Orders

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/orders` | 👤 admin | Listar todas. |
| GET | `/orders/{id}` | 👤 buyer, admin | Por ID. |
| GET | `/orders/user/{userId}` | 👤 buyer, admin | Órdenes de un usuario. |
| POST | `/orders` | 👤 buyer | Crear pedido (acepta `couponCode` opcional). |
| PUT | `/orders/{id}` | 👤 buyer, admin | Actualizar. |
| PUT | `/orders/{id}/cancel` | 👤 buyer, admin | Cancelar (si estaba PAID, restaura stock). |
| DELETE | `/orders/{id}` | 👤 admin | Eliminar (cascade a items y pagos). |
| DELETE | `/orders/expired` | 👤 admin | Cancela todas las PENDING con > 48h de antigüedad. |

#### Order Items

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/order-items/order/{orderId}` | 👤 buyer, admin | Items de una orden. |
| GET | `/order-items/{itemId}` | 👤 buyer, admin | Por ID. |
| DELETE | `/order-items/{itemId}` | 👤 buyer, admin | Eliminar item. |

#### Payments

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/payments` | 👤 admin | Listar todos. |
| GET | `/payments/{id}` | 👤 buyer, admin | Por ID. |
| GET | `/payments/order/{orderId}` | 👤 buyer, admin | Pagos de una orden. |
| POST | `/payments` | 👤 buyer | Procesar pago. Acepta query param `?simulateFailure=true` para forzar FAILED en testing. |

### Inventario y depósitos

#### Inventory (toda la clase requiere seller o admin)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/inventory` | 👤 seller, admin | Listar. |
| GET | `/inventory/{id}` | 👤 seller, admin | Por ID. |
| GET | `/inventory/variant/{variantId}` | 👤 seller, admin | Por variante. |
| POST | `/inventory` | 👤 seller, admin | Registrar. |
| PUT | `/inventory/{id}` | 👤 seller, admin | Actualizar. |
| DELETE | `/inventory/{id}` | 👤 seller, admin | Eliminar. |

#### Warehouses

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/warehouses` | 👤 seller, admin | Listar. |
| GET | `/warehouses/{id}` | 👤 seller, admin | Por ID. |
| POST | `/warehouses` | 👤 admin | Crear. |
| PUT | `/warehouses/{id}` | 👤 admin | Actualizar. |
| DELETE | `/warehouses/{id}` | 👤 admin | Eliminar. |

#### Addresses

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/addresses` | 👤 admin | Listar. |
| GET | `/addresses/{id}` | 👤 admin | Por ID. |
| GET | `/addresses/user/{userId}` | 👤 admin | Direcciones de un usuario. |
| POST | `/addresses` | 🔒 dueño / admin | Crear (el `userId` del body debe ser el del usuario autenticado, o admin). |
| PUT | `/addresses/{id}` | 🔒 dueño / admin | Actualizar (solo el dueño de la address o admin). |
| DELETE | `/addresses/{id}` | 🔒 dueño / admin | Eliminar (solo el dueño o admin). |

> En POST/PUT/DELETE, si el usuario autenticado no es admin y no es el dueño, devuelve **403 Forbidden**.

### Logística

#### Deliveries

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/deliveries` | 🔒 | Listar todas. |
| GET | `/deliveries/{id}` | 🔒 | Por ID. |
| GET | `/deliveries/order/{orderId}` | 🔒 | Por orden. |
| POST | `/deliveries` | 🔒 | Crear. |
| PUT | `/deliveries/{id}` | 🔒 | Actualizar. |
| DELETE | `/deliveries/{id}` | 🔒 | Eliminar. |

#### Tracking

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/tracking/{id}` | 🔒 | Checkpoint por ID. |
| GET | `/tracking/delivery/{deliveryId}` | 🔒 | Checkpoints de una entrega. |
| POST | `/tracking` | 🔒 | Agregar checkpoint. |
| PUT | `/tracking/{id}/status` | 🔒 | Actualizar estado. |

### Devoluciones y reembolsos

#### Returns

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/returns` | 👤 admin | Listar. |
| GET | `/returns/{id}` | 👤 buyer, admin | Por ID. |
| GET | `/returns/order/{orderId}` | 👤 buyer, admin | Por orden. |
| POST | `/returns` | 👤 buyer | Crear. |
| PUT | `/returns/{id}` | 👤 admin | Actualizar. |
| DELETE | `/returns/{id}` | 👤 admin | Eliminar. |

#### Refunds (toda la clase requiere admin)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/refunds/{id}` | 👤 admin | Por ID. |
| GET | `/refunds/return/{returnId}` | 👤 admin | Por devolución. |
| POST | `/refunds` | 👤 admin | Crear. |
| PUT | `/refunds/{id}/status` | 👤 admin | Cambiar estado. |

### Soporte y notificaciones

#### Support tickets

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/support/tickets` | 👤 admin | Listar todos. |
| GET | `/support/tickets/{id}` | 🔒 | Por ID. |
| POST | `/support/tickets` | 👤 buyer, seller | Crear ticket. |
| PUT | `/support/tickets/{id}/status` | 👤 admin | Cambiar estado. |
| GET | `/support/tickets/{id}/messages` | 🔒 | Listar mensajes. |
| POST | `/support/tickets/{id}/messages` | 🔒 | Enviar mensaje. |

#### Notifications

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/notifications` | 👤 admin | Listar todas. |
| GET | `/notifications/unread` | 🔒 | No leídas del usuario. |
| PUT | `/notifications/{id}/read` | 🔒 | Marcar como leída. |

### Sessions

No expuesto vía API. La gestión de sesiones es interna: la crea `POST /auth/login`, la valida `JwtAuthFilter` en cada request, y la cierra `POST /auth/logout` (todas las sesiones del usuario). El antiguo `SessionsController` se removió porque exponía sesiones de cualquier usuario sin scoping.

---

## 6. Decisiones de negocio

### 6.1 Orders

**Estados y transiciones:**

```
        ┌──── pago COMPLETED ────┐
        ▼                        │
    PENDING ─────cancelar──────► CANCELLED  (terminal)
        │                        ▲
        ▼                        │
       PAID ────cancelar─────────┘  (restaura stock)
```

- Un `buyer` crea la orden con `POST /orders` indicando `userId`, `shippingAddressId`, lista de `items` y opcionalmente un `couponCode`.
- Validaciones al crear: usuario y dirección existen, hay al menos 1 item, **stock disponible es suficiente** (pero **no se reserva**), variantes existen, cupón válido (si se envía).
- El precio unitario aplica `PriceTier` (precios por volumen): se busca el tier con mayor `minQuantity` que aplique a la cantidad solicitada; si no hay tier, se usa `variant.basePrice`.
- **Expiración automática:** `DELETE /orders/expired` (admin) cancela todas las órdenes en `PENDING` con más de 48 horas. No restaura stock (no se había descontado) ni revierte el cupón.
- **Cancelación:** `PUT /orders/{id}/cancel` lo puede hacer el buyer dueño o un admin. Si la orden estaba `PAID`, se **restaura el stock**.

### 6.2 Cupones y descuentos

**Decisión clave 1 — el cupón se consume al crear la orden, no al pagar.**
Al hacer `POST /orders` con `couponCode`, el sistema valida el cupón e incrementa `times_used` inmediatamente. Si después la orden se cancela o expira, **el contador no se revierte**: el cupón queda "gastado" sin haber generado venta. Decisión consciente para que el límite de uso refleje intentos, no compras concretas.

**Decisión clave 2 — los descuentos se apilan en cascada con tope.**
Para cada ítem se calcula:

1. `unitPrice` = mejor `PriceTier` aplicable, o `basePrice`.
2. `productDiscount` = mejor descuento activo entre los de producto y los de categoría (no se suman entre sí, gana el mayor). Tipos: `PERCENTAGE` o `FIXED`.
3. `couponDiscount` = descuento del cupón **solo si su `Discount` aplica al producto** (mismo producto si es de tipo `PRODUCT`, misma categoría si es `CATEGORY`). Tope: `unitPrice - productDiscount` (no puede dejar el precio en negativo).
4. `effectivePrice = unitPrice - productDiscount - couponDiscount`.
5. `subtotal = effectivePrice × quantity`.

**Decisión clave 3 — un cupón se puede usar varias veces por el mismo usuario.**
No hay "primer uso", "uno por usuario" ni vínculo `coupon ↔ user`. El único límite es global (`usage_limit`).

**Validaciones al aplicar cupón** (lanzan `BusinessRuleException` o `NotFoundException`):
- Existe un cupón con ese `code`.
- `is_active = true`.
- `expires_at > ahora`.
- `times_used < usage_limit`.

### 6.3 Pagos

**Pagos simulados.** No hay integración con gateway real. `SimulatedPaymentProcessor` acepta tres métodos hardcoded: `CREDIT_CARD`, `DEBIT_CARD`, `CASH`. Cualquier otro string en `paymentMethod` → resultado `FAILED`. El `transactionId` es un UUID generado por el mock.

**Flujo completo de `POST /payments`:**

1. Validar que la orden existe y está en `PENDING`.
2. Validar stock con **lock pessimista** (`SELECT FOR UPDATE` en `Inventory.findByVariantIdForUpdate`) — esto sí evita race conditions entre dos pagos concurrentes.
3. Llamar al procesador. Si `simulateFailure=true`, devuelve `FAILED` sin tocar nada más.
4. Si `COMPLETED`:
   - Descontar stock siguiendo "primero disponible" (consume de un `Inventory` por vez hasta cubrir la cantidad).
   - Marcar orden como `PAID`.
   - Persistir `Payment` con `status=COMPLETED`.
5. Si `FAILED`: persistir `Payment` con `status=FAILED`, **la orden sigue en `PENDING`** y el usuario puede reintentar con otro intento de pago.

**Estados de payment** (`PaymentStatus`): `COMPLETED`, `FAILED`, `REFUNDED` (este último presente en el enum pero no usado todavía — el flujo de refund vive en `Refund`/`ProductReturn`).

### 6.4 Limitación conocida de stock al crear orden

Dos órdenes `PENDING` creadas casi simultáneamente pueden ambas validar el stock disponible para el mismo ítem (no hay lock al crear). La protección real está al pagar. En la práctica: una de las dos órdenes va a fallar al pagar con `BusinessRuleException` por stock insuficiente. Es una decisión consciente para no bloquear inventario sin pago.

---

## 7. Cómo probar fácilmente

### 7.1 Setup en 3 pasos

1. `make start-all` — levanta Docker, inicializa la DB y arranca la app.
2. En **otra terminal**, una vez que veas `Started DemoApplication`: `make seed-db` — carga `admin@mail.com` y `seller_test@test.com` (ambos con password `Test1234!`).
3. Importar `postman_collection.json` en Postman.

### 7.2 Postman: token automático

La colección incluye un script post-request en `/auth/login` y `/auth/register` que **guarda el token automáticamente en la variable de colección `{{token}}`**. Los demás 50+ requests ya tienen `Authorization: Bearer {{token}}` configurado. Flujo: login una vez, después todo el resto funciona sin tocar headers.

### 7.3 Flujo end-to-end de compra (curl)

Asume: app corriendo en `localhost:8080`, `make seed-db` corrido, y existe al menos 1 producto + variante + inventario (creados por seller/admin).

```bash
# 1. Login como buyer (registrate primero o usa uno existente)
TOKEN_BUYER=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@mail.com","password":"Password123"}' \
  | jq -r .token)

# 2. Login como admin (para crear el cupón)
TOKEN_ADMIN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@mail.com","password":"Test1234!"}' \
  | jq -r .token)

# 3. (Admin) Crear cupón asociado a un descuento existente
curl -X POST http://localhost:8080/coupons \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "discountId": 1,
    "code": "WELCOME10",
    "usageLimit": 100,
    "expiresAt": "2026-12-31T23:59:59"
  }'

# 4. (Buyer) Crear orden con cupón
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN_BUYER" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "shippingAddressId": 1,
    "couponCode": "WELCOME10",
    "items": [
      { "variantId": 1, "quantity": 2 }
    ]
  }'
# → 201 Created. Anotá el "id" de la orden, supongamos 5.

# 5. (Buyer) Pagar
curl -X POST http://localhost:8080/payments \
  -H "Authorization: Bearer $TOKEN_BUYER" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 5,
    "paymentMethod": "CREDIT_CARD"
  }'
# → 201 Created con status COMPLETED.

# 6. Verificar que la orden quedó PAID
curl http://localhost:8080/orders/5 \
  -H "Authorization: Bearer $TOKEN_BUYER"
# → status: "PAID", stock descontado.
```

### 7.4 Casos de error útiles para demostrar

| Caso | Cómo provocarlo | Resultado esperado |
|------|-----------------|--------------------|
| **401 sin token** | `curl http://localhost:8080/users/me` (sin header). | 401. |
| **403 rol insuficiente** | Buyer haciendo `POST /products`. | 403. |
| **422 cupón expirado** | Crear cupón con `expiresAt` en el pasado y usarlo en una orden. | 422 con mensaje de cupón expirado. |
| **422 stock insuficiente** | Crear orden con `quantity` mayor al stock total de la variante. | 422. |
| **409 cupón duplicado** | `POST /coupons` con un `code` que ya existe. | 409. |
| **Pago FAILED controlado** | `POST /payments?simulateFailure=true`. La orden sigue en PENDING. | 201 con `status: "FAILED"`, sin descuento de stock. |
| **429 rate limit** | 11 logins seguidos en menos de un minuto desde la misma IP. | 429 a partir del request 11. |

---

## 8. Limitaciones y trabajo futuro

Resumen de las decisiones explícitamente pospuestas o pendientes. Detalles en [TESTING.md](TESTING.md) y [DECISIONES_PENDIENTES.md](DECISIONES_PENDIENTES.md).

| Tema | Estado actual | Iteración futura |
|------|---------------|------------------|
| Logout | Cierra todas las sesiones del usuario. | Cerrar solo la sesión actual + endpoint `/auth/logout-all` para el caso pánico. |
| Multi-rol | Un usuario tiene un solo rol. | `@ElementCollection<Role>` para permitir buyer + seller con el mismo email. |
| Cupones | `times_used` no se revierte si la orden se cancela. | Decidir si revertir al cancelar PENDING (no al cancelar PAID). |
| Stock al crear orden | Validado pero no reservado. La protección real está al pagar (lock pessimista). | Reserva temporal con TTL si se vuelve un problema con concurrencia real. |
| Pagos | Simulados (mock processor). | Integración con MercadoPago o similar. |
| Endpoints sin `@PreAuthorize` | ✅ Resuelto — discounts (admin), addresses (dueño/admin), sessions (controller removido). Ver TESTING.md #46/#47/#48. | — |
| Credenciales de seed | Hardcoded en `seed-users.sql` y versionadas (proyecto académico). | Parametrizar con env vars `INITIAL_ADMIN_EMAIL` / `INITIAL_ADMIN_PASSWORD` — pendiente #45. |
| Rate limit | 10/min auth, 100/min general (por IP). | Validado en pruebas, dejar igual. Ajustar con métricas reales si se despliega. |
