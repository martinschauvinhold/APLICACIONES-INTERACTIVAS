# Plan de pruebas — E-Commerce API

> Completar la columna **Resultado** con ✅ OK / ❌ Falla / ⚠️ Comportamiento inesperado.
> Agregar notas donde haga falta.

---

## Nota sobre autenticación

`SecurityConfig` tiene `.anyRequest().authenticated()` — **todos** los endpoints (excepto `/auth/register`, `/auth/login` y `/error`) requieren token JWT.

- Sin token → **401**
- Con token pero rol incorrecto → **403**
- Con token y rol correcto → **2xx**

Por eso `GET /categories` devuelve 401 (falta token en Postman) y `GET /users` devuelve 403 (token de buyer, endpoint es solo admin). **Son errores distintos.**

**Decisión pendiente:** ¿queremos que endpoints de lectura como `/categories`, `/products`, `/reviews`, `/variants` sean verdaderamente públicos (sin token)? Si sí, hay que actualizar `SecurityConfig`.

---

## Convención de columnas

| Columna | Significado |
|---------|-------------|
| Auth requerida | Qué necesita el endpoint |
| Postman OK | El request en la colección tiene el header correcto |
| Resultado | Resultado de la prueba manual |
| Notas | Observaciones |

---

## Autenticación

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 1 | POST | `/auth/register` | Ninguna | ✅ | ✅ OK | Datos válidos → 200 + token |
| 1b | POST | `/auth/register` | Ninguna | ✅ | ✅ OK | Email duplicado → 409 con mensaje claro |
| 1c | POST | `/auth/register` | Ninguna | ✅ | ✅ OK | Username duplicado → 409 con mensaje claro |
| 1d | POST | `/auth/register` | Ninguna | ✅ | ✅ OK | Email inválido → 400 `email: must be a well-formed email address` |
| 1e | POST | `/auth/register` | Ninguna | ✅ | ✅ OK | Password corto → 400 `password: size must be between 8 and 2147483647` |
| 1f | POST | `/auth/register` | Ninguna | ✅ | ✅ OK | Username vacío → 400 `username: must not be blank` |
| 2 | POST | `/auth/login` | Ninguna | ✅ | ✅ OK | Credenciales correctas → 200 + token |
| 2b | POST | `/auth/login` | Ninguna | ✅ | ✅ OK | Password incorrecto → 401 `Credenciales inválidas` |
| 2c | POST | `/auth/login` | Ninguna | ✅ | ✅ OK | Email inexistente → 401 `Credenciales inválidas` (no revela si el email existe) |
| 3 | POST | `/auth/logout` | Token | ✅ | ✅ OK | Con token válido → 204 sin body |
| 3b | POST | `/auth/logout` | Token | ✅ | ✅ OK | Sin token → 401 |
| 3c | GET | `/users/me` | Token | ✅ | ✅ OK | Token usado post-logout → 401 (sesión invalidada correctamente) |

---

## Usuarios

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 4 | GET | `/users` | admin | ✅ | ✅ OK | 200 + lista sin passwordHash |
| 4b | GET | `/users` | Sin token | ✅ | ✅ OK | 401 |
| 4c | GET | `/users` | buyer | ✅ | ✅ OK | 403 |
| 5 | GET | `/users/{id}` | admin | ✅ | ✅ OK | 200 con usuario existente |
| 5b | GET | `/users/{id}` | admin | ✅ | ✅ OK | ID inexistente → 404 |
| 5c | GET | `/users/{id}` | Sin token | ✅ | ✅ OK | 401 |
| 6 | GET | `/users/me` | buyer | ✅ | ✅ OK | 200, sin passwordHash ✅ |
| 6b | GET | `/users/me` | admin | ✅ | ✅ OK | 200, sin passwordHash ✅ |
| 6c | GET | `/users/me` | Sin token | ✅ | ✅ OK | 401 |
| 7 | POST | `/users` | admin + role seller | ✅ | ✅ OK | 201, sin passwordHash |
| 7b | POST | `/users` | admin + role admin | ✅ | ✅ OK | 201 |
| 7c | POST | `/users` | Sin token | ✅ | ✅ OK | 401 |
| 7d | POST | `/users` | buyer | ✅ | ✅ OK | 403 |
| 7e | POST | `/users` | admin + email inválido | ✅ | ✅ OK | 400 `email: must be a well-formed email address` |
| 8 | PUT | `/users/{id}` | admin | ✅ | ✅ OK | 200 con datos actualizados |
| 8b | PUT | `/users/{id}` | admin + ID inexistente | ✅ | ✅ OK | 404 |
| 8c | PUT | `/users/{id}` | Sin token | ✅ | ✅ OK | 401 |
| 8d | PUT | `/users/{id}` | buyer | ✅ | ✅ OK | 403 |
| 9 | DELETE | `/users/{id}` | admin | ✅ | ✅ OK | 204 sin body |
| 9b | DELETE | `/users/{id}` | admin + ID inexistente | ✅ | ✅ OK | 404 |
| 9c | DELETE | `/users/{id}` | Sin token | ✅ | ✅ OK | 401 |
| 9d | DELETE | `/users/{id}` | buyer | ✅ | ✅ OK | 403 |

---

## Categorías

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 10 | GET | `/categories` | Token | ❌ falta header | ✅ OK | 200 + lista |
| 10b | GET | `/categories` | Sin token | — | ✅ OK | 401 |
| 11 | GET | `/categories/{id}` | Token + ID existente | ❌ falta header | ✅ OK | 200 |
| 11b | GET | `/categories/{id}` | Token + ID inexistente | — | ✅ OK | 404 |
| 11c | GET | `/categories/{id}` | Sin token | — | ✅ OK | 401 |
| 12 | POST | `/categories` | admin | ✅ | ✅ OK | 201 |
| 12b | POST | `/categories` | admin + descripción duplicada | — | ✅ OK | 409 `Category con description 'X' ya existe` |
| 12c | POST | `/categories` | Sin token | — | ✅ OK | 401 |
| 12d | POST | `/categories` | buyer | — | ✅ OK | 403 |
| 12e | POST | `/categories` | seller | — | ✅ OK | 403 |
| 12f | POST | `/categories` | admin + body `{}` (description null) | — | ✅ OK | 400 `description: must not be blank` |
| 13 | PUT | `/categories/{id}` | admin | ✅ | ✅ OK | 200 |
| 13b | PUT | `/categories/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 13c | PUT | `/categories/{id}` | Sin token | — | ✅ OK | 401 |
| 13d | PUT | `/categories/{id}` | buyer | — | ✅ OK | 403 |
| 13e | PUT | `/categories/{id}` | admin + descripción de otra cat. | — | ✅ OK | 409 — duplicate check agregado en update |
| 14 | DELETE | `/categories/{id}` | admin + sin productos | ✅ | ✅ OK | 204 |
| 14b | DELETE | `/categories/{id}` | admin + con productos asociados | — | ✅ OK | 422 `No se puede eliminar la categoría porque tiene productos asociados` |
| 14c | DELETE | `/categories/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 14d | DELETE | `/categories/{id}` | Sin token | — | ✅ OK | 401 |
| 14e | DELETE | `/categories/{id}` | buyer | — | ✅ OK | 403 |
| 14f | PATCH | `/categories/{id}/deactivate` | admin | — | ✅ OK | 200 + `active: false` — soft delete para cats con productos |
| 14g | PATCH | `/categories/{id}/deactivate` | admin + ID inexistente | — | ✅ OK | 404 |
| 14h | PATCH | `/categories/{id}/deactivate` | Sin token | — | ✅ OK | 401 |
| 14i | PATCH | `/categories/{id}/deactivate` | buyer | — | ✅ OK | 403 |
| 14j | PATCH | `/categories/{id}/deactivate` | seller | — | ✅ OK | 403 |
| 10c | GET | `/categories` | admin | — | ✅ OK | ve todas (activas + inactivas) |
| 10d | GET | `/categories` | buyer / seller | — | ✅ OK | solo activas — 3 categorías visibles tras UPDATE manual de data preexistente |

---

## Productos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 15 | GET | `/products` | Token | ❌ falta header | ✅ OK | 200 + lista |
| 15b | GET | `/products` | Sin token | — | ✅ OK | 401 |
| 16 | GET | `/products/{id}` | Token + ID existente | ❌ falta header | ✅ OK | 200 |
| 16b | GET | `/products/{id}` | Token + ID inexistente | — | ✅ OK | 404 |
| 16c | GET | `/products/{id}` | Sin token | — | ✅ OK | 401 |
| 17 | POST | `/products` | admin | ❌ falta header | ✅ OK | 201 |
| 17b | POST | `/products` | seller | ❌ falta header | ✅ OK | 201 |
| 17c | POST | `/products` | Sin token | — | ✅ OK | 401 |
| 17d | POST | `/products` | buyer | — | ✅ OK | 403 |
| 17e | POST | `/products` | admin + categoryId inexistente | — | ✅ OK | 404 `Category con id 9999 no encontrado` |
| 17f | POST | `/products` | admin + body vacío `{}` | — | ✅ OK | 400 `categoryId: must be greater than 0, name: must not be blank` |
| 17g | POST | `/products` | admin + categoryId válido + name null | — | ✅ OK | 400 `name: must not be blank` |
| 18 | PUT | `/products/{id}` | admin | ❌ falta header | ✅ OK | 200 con datos actualizados |
| 18b | PUT | `/products/{id}` | seller | ❌ falta header | ✅ OK | 200 |
| 18c | PUT | `/products/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 18d | PUT | `/products/{id}` | admin + categoryId inexistente | — | ✅ OK | 404 |
| 18e | PUT | `/products/{id}` | Sin token | — | ✅ OK | 401 |
| 18f | PUT | `/products/{id}` | buyer | — | ✅ OK | 403 |
| 19 | DELETE | `/products/{id}` | admin | ❌ falta header | ✅ OK | 204 |
| 19b | DELETE | `/products/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 19c | DELETE | `/products/{id}` | seller (solo admin puede) | — | ✅ OK | 403 |
| 19d | DELETE | `/products/{id}` | Sin token | — | ✅ OK | 401 |
| 19e | DELETE | `/products/{id}` | buyer | — | ✅ OK | 403 |

---

## Variantes de producto

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 20 | GET | `/variants` | Token (¿debería ser público?) | ❌ falta header | | |
| 21 | GET | `/variants/{id}` | Token (¿debería ser público?) | ❌ falta header | | |
| 22 | GET | `/variants/product/{productId}` | Token (¿debería ser público?) | ❌ falta header | | |
| 23 | POST | `/variants` | seller o admin | ❌ falta header | | |
| 24 | PUT | `/variants/{id}` | seller o admin | ❌ falta header | | |
| 25 | DELETE | `/variants/{id}` | seller o admin | ❌ falta header | | |

---

## Inventario

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 26 | GET | `/inventory` | seller o admin | ❌ falta header | | |
| 27 | GET | `/inventory/{id}` | seller o admin | ❌ falta header | | |
| 28 | GET | `/inventory/variant/{variantId}` | seller o admin | ❌ falta header | | |
| 29 | POST | `/inventory` | seller o admin | ❌ falta header | | |
| 30 | PUT | `/inventory/{id}` | seller o admin | ❌ falta header | | |
| 31 | DELETE | `/inventory/{id}` | seller o admin | ❌ falta header | | |

---

## Depósitos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 32 | GET | `/warehouses` | seller o admin | ❌ falta header | | |
| 33 | GET | `/warehouses/{id}` | seller o admin | ❌ falta header | | |
| 34 | POST | `/warehouses` | admin | ❌ falta header | | |
| 35 | PUT | `/warehouses/{id}` | admin | ❌ falta header | | |
| 36 | DELETE | `/warehouses/{id}` | admin | ❌ falta header | | |

---

## Direcciones

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 37 | GET | `/addresses` | admin | ❌ falta header | | |
| 38 | GET | `/addresses/{id}` | admin | ❌ falta header | | |
| 39 | GET | `/addresses/user/{userId}` | admin | ❌ falta header | | |
| 40 | POST | `/addresses` | Token | ❌ falta header | | |
| 41 | PUT | `/addresses/{id}` | Token | ❌ falta header | | |
| 42 | DELETE | `/addresses/{id}` | Token | ❌ falta header | | |

---

## Pedidos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 43 | GET | `/orders` | admin | ✅ | | |
| 44 | GET | `/orders/{id}` | buyer o admin | ✅ | | |
| 45 | GET | `/orders/user/{userId}` | buyer o admin | ✅ | | |
| 46 | POST | `/orders` | buyer | ✅ | | |
| 47 | PUT | `/orders/{id}` | buyer o admin | ✅ | | |
| 48 | PUT | `/orders/{id}/cancel` | buyer o admin | ✅ | | |
| 49 | DELETE | `/orders/{id}` | admin | ✅ | | |
| 50 | DELETE | `/orders/expired` | admin | ✅ | | |

---

## Items de pedido

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 51 | GET | `/order-items/{id}` | buyer o admin | ✅ | | |
| 52 | GET | `/order-items/order/{orderId}` | buyer o admin | ✅ | | |
| 53 | DELETE | `/order-items/{id}` | buyer o admin | ✅ | | |

---

## Pagos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 54 | GET | `/payments` | admin | ✅ | | |
| 55 | GET | `/payments/{id}` | buyer o admin | ✅ | | |
| 56 | GET | `/payments/order/{orderId}` | buyer o admin | ✅ | | |
| 57 | POST | `/payments` | buyer | ✅ | | Probar con `simulateFailure=true` también |

---

## Reseñas

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 58 | GET | `/reviews` | Token (¿debería ser público?) | ❌ falta header | | |
| 59 | GET | `/reviews/{id}` | Token (¿debería ser público?) | ❌ falta header | | |
| 60 | GET | `/reviews/product/{productId}` | Token (¿debería ser público?) | ❌ falta header | | |
| 61 | POST | `/reviews` | buyer | ❌ falta header | | |
| 62 | PUT | `/reviews/{id}` | buyer o admin | ❌ falta header | | |
| 63 | DELETE | `/reviews/{id}` | admin | ❌ falta header | | |

---

## Entregas

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 64 | GET | `/deliveries` | seller o admin | ✅ | | |
| 65 | GET | `/deliveries/{id}` | Token (¿debería ser público?) | ❌ falta header | | |
| 66 | GET | `/deliveries/order/{orderId}` | Token (¿debería ser público?) | ❌ falta header | | |
| 67 | POST | `/deliveries` | seller o admin | ✅ | | |
| 68 | PUT | `/deliveries/{id}` | seller o admin | ✅ | | |
| 69 | DELETE | `/deliveries/{id}` | admin | ✅ | | |

---

## Seguimiento de envío

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 70 | GET | `/tracking/{id}` | Token (¿debería ser público?) | ❌ falta header | | |
| 71 | GET | `/tracking/delivery/{deliveryId}` | Token (¿debería ser público?) | ❌ falta header | | |
| 72 | POST | `/tracking` | seller o admin | ✅ | | |
| 73 | PUT | `/tracking/{id}/status` | seller o admin | ✅ | | |

---

## Devoluciones

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 74 | GET | `/returns` | admin | ✅ | | |
| 75 | GET | `/returns/{id}` | buyer o admin | ✅ | | |
| 76 | GET | `/returns/order/{orderId}` | buyer o admin | ✅ | | |
| 77 | POST | `/returns` | buyer | ✅ | | |
| 78 | PUT | `/returns/{id}` | admin | ✅ | | |
| 79 | DELETE | `/returns/{id}` | admin | ✅ | | |

---

## Reembolsos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 80 | GET | `/refunds/{id}` | admin | ❌ falta header | | |
| 81 | GET | `/refunds/return/{returnId}` | admin | ❌ falta header | | |
| 82 | POST | `/refunds` | admin | ❌ falta header | | |
| 83 | PUT | `/refunds/{id}/status` | admin | ❌ falta header | | |

---

## Soporte — Tickets y Mensajes

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 84 | GET | `/support/tickets` | admin | ✅ | | |
| 85 | GET | `/support/tickets/{id}` | Token (¿debería ser público?) | ❌ falta header | | |
| 86 | POST | `/support/tickets` | buyer o seller | ✅ | | |
| 87 | PUT | `/support/tickets/{id}/status` | admin | ✅ | | |
| 88 | GET | `/support/tickets/{id}/messages` | Token (¿debería ser público?) | ❌ falta header | | |
| 89 | POST | `/support/tickets/{id}/messages` | Token | ✅ | | |

---

## Notificaciones

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 90 | GET | `/notifications` | admin | ✅ | | |
| 91 | GET | `/notifications/unread` | Token | ✅ | | |
| 92 | PUT | `/notifications/{id}/read` | Token | ✅ | | |

---

## Cupones

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 93 | GET | `/coupons` | Token (¿debería ser público?) | ❌ falta header | | |
| 94 | GET | `/coupons/{id}` | Token (¿debería ser público?) | ❌ falta header | | |
| 95 | GET | `/coupons/validate/{code}` | Token (¿debería ser público?) | ❌ falta header | | |
| 96 | POST | `/coupons` | Token | ❌ falta header | | |
| 97 | DELETE | `/coupons/{id}` | Token | ❌ falta header | | |

---

## Sesiones

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 98 | GET | `/sessions/user/{userId}` | Token | ✅ | | |
| 99 | GET | `/sessions/{id}` | Token | ✅ | | |
| 100 | DELETE | `/sessions/{id}` | Token | ✅ | | |

---

## Decisiones pendientes

Estos endpoints están marcados con `@PreAuthorize` que permite acceso sin rol específico o sin `@PreAuthorize` directamente, pero `SecurityConfig` los filtra igual porque usa `.anyRequest().authenticated()`. Definir si van a ser verdaderamente públicos o siempre requieren token:

| Endpoint | ¿Público? |
|----------|-----------|
| `GET /categories` | |
| `GET /products` | |
| `GET /variants` | |
| `GET /reviews` | |
| `GET /coupons` | |
| `GET /deliveries/{id}` | |
| `GET /tracking/{id}` | |
| `GET /support/tickets/{id}` | |

Si se decide que son públicos, hay que agregar las rutas al `permitAll()` en `SecurityConfig`.

---

## Preguntas abiertas de la sesión

| # | Pregunta | Contexto | Decisión |
|---|----------|----------|----------|
| 1 | ¿Los endpoints de lectura (categories, products, variants, reviews, coupons, tracking) deben ser públicos o requerir token? | Actualmente todos requieren auth por `.anyRequest().authenticated()` en `SecurityConfig`. Si se decide que son públicos, hay que agregarlos al `permitAll()` y actualizar la colección de Postman. | |
| 2 | ¿La colección de Postman se actualiza agregando `Authorization: Bearer {{token}}` a los requests que lo necesitan, o se cambia `SecurityConfig` para hacer algunos endpoints verdaderamente públicos? | ~30 requests en la colección no tienen el header. Ambos caminos son válidos, depende de la decisión anterior. | |
| 3 | ¿El logout debe invalidar solo la sesión actual o todas las sesiones del usuario? | El código actual (`sessionRepository::deleteByUser`) borra todas las sesiones al hacer logout desde cualquier dispositivo. Si el usuario está logueado desde el celular y la PC, al cerrar sesión desde uno se cierra en ambos. | |
| 4 | ¿Los límites de rate limiting son adecuados? | Actualmente: 10 req/min para `/auth/*`, 100 req/min para el resto (por IP). ¿Son valores razonables para el proyecto? | |
| 5 | ¿Cómo se crea el primer admin en un ambiente nuevo? | No hay endpoint público para esto. Se definió que hay que hacer un INSERT directo en la DB con un hash BCrypt. ¿Se documenta el hash de una contraseña de ejemplo para simplificar el setup inicial? | |
| 6 | **BUG**: `/auth/register` devuelve un token pero no crea sesión en la tabla `sessions`. | El `JwtAuthFilter` valida `sessionRepository.existsByUserEmail(email)` además del JWT. El token del register no funciona en ningún endpoint autenticado — el usuario tiene que hacer login después. | |
| 7 | ~~**COMPORTAMIENTO INCONSISTENTE**: `GET /users/{id}` y `GET /products/{id}` devuelven 204 para ID inexistente.~~ | ✅ **RESUELTO** — cambiado `noContent()` → `notFound()` en `UsersController.java` y `ProductsController.java`. | |
| 8 | ~~**BUG**: `POST /products` sin `@Valid` ni validaciones → 500 con name null.~~ | ✅ **RESUELTO** — agregado `@Valid` en POST y PUT de `ProductsController`, `@NotBlank` en `name` y `@Positive` en `categoryId` en `ProductRequest`. | |
| 9 | ~~**BUG**: `GET /categories/{id}` devuelve 204 para ID inexistente.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `CategoriesController`. | |
| 10 | ~~**BUG**: `POST /categories` duplicado → 500.~~ | ✅ **RESUELTO** — reemplazado `CategoryDuplicateException` por `DuplicateException` (409) en `CategoryServiceImpl`. | |
| 11 | ~~**BUG**: `POST /categories` body vacío → 201 con description null.~~ | ✅ **RESUELTO** — `@NotBlank` en `CategoryRequest.description` + `@Valid` en controller. | |
| 12 | ~~**BUG**: `PUT /categories/{id}` no valida duplicados.~~ | ✅ **RESUELTO** — `updateCategory` ahora verifica con `findByDescription` antes de guardar. | |
| 13 | ~~**BUG**: `DELETE /categories/{id}` con productos → 500.~~ | ✅ **RESUELTO** — `deleteCategory` verifica `productRepository.existsByCategory_Id` y lanza `BusinessRuleException` (422). Se agregó además `PATCH /categories/{id}/deactivate` para soft delete. | |
| 14 | ~~**COMPORTAMIENTO**: Categorías nuevas con `active: false`.~~ | ✅ **RESUELTO** — `@Builder.Default` agregado en `Category.java`. Data preexistente corregida con `UPDATE categories SET is_active = 1`. | |
