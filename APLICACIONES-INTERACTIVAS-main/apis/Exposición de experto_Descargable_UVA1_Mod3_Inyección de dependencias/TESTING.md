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
| 4 | GET | `/users` | admin | ✅ | | |
| 5 | GET | `/users/{id}` | admin | ✅ | | |
| 6 | GET | `/users/me` | Token | ✅ | | Verificar que `passwordHash` no aparezca en la respuesta |
| 7 | POST | `/users` | admin | ✅ | | Probar con `role: "seller"` y `role: "admin"` |
| 8 | PUT | `/users/{id}` | admin | ✅ | | |
| 9 | DELETE | `/users/{id}` | admin | ✅ | | |

---

## Categorías

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 10 | GET | `/categories` | Token (¿debería ser público?) | ❌ falta header | | |
| 11 | GET | `/categories/{id}` | Token (¿debería ser público?) | ❌ falta header | | |
| 12 | POST | `/categories` | admin | ✅ | | |
| 13 | PUT | `/categories/{id}` | admin | ✅ | | |
| 14 | DELETE | `/categories/{id}` | admin | ✅ | | |

---

## Productos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 15 | GET | `/products` | Token (¿debería ser público?) | ❌ falta header | | |
| 16 | GET | `/products/{id}` | Token (¿debería ser público?) | ❌ falta header | | |
| 17 | POST | `/products` | seller o admin | ❌ falta header | | |
| 18 | PUT | `/products/{id}` | seller o admin | ❌ falta header | | |
| 19 | DELETE | `/products/{id}` | admin | ❌ falta header | | |

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
