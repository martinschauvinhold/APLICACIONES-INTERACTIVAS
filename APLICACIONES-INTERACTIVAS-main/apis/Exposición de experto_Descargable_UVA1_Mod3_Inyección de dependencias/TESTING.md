# Plan de pruebas — E-Commerce API

> Completar la columna **Resultado** con ✅ OK / ❌ Falla / ⚠️ Comportamiento inesperado.
> Agregar notas donde haga falta.

---

## Nota sobre autenticación

`SecurityConfig` tiene `.anyRequest().authenticated()` — **casi todos** los endpoints requieren token JWT, con las siguientes excepciones explícitas vía `permitAll()`:

- `/auth/register`, `/auth/login`, `/error`
- `GET /categories`, `GET /categories/**`
- `GET /products`, `GET /products/**`
- `GET /variants`, `GET /variants/**`
- `GET /reviews`, `GET /reviews/**`

Comportamiento esperado:

- Endpoint protegido sin token → **401**
- Endpoint protegido con token pero rol incorrecto → **403**
- Endpoint protegido con token y rol correcto → **2xx**
- Endpoint público (catálogo) → **2xx** sin necesidad de token

> **Nota Postman:** la colección incluye una variable `{{token}}` que se setea automáticamente vía script post-request al ejecutar `Login` (o `Registrar usuario`). Después de loguearte una vez, el resto de los requests autenticados ya envían el header correcto.

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
| 10 | GET | `/categories` | Público | ✅ (sin token, intencional) | ✅ OK | 200 + lista — endpoint público |
| 10b | GET | `/categories` | Sin token | — | ✅ OK | 200 + lista (público, devuelve solo activas) |
| 11 | GET | `/categories/{id}` | Público | ✅ (sin token, intencional) | ✅ OK | 200 — endpoint público |
| 11b | GET | `/categories/{id}` | Sin token + ID inexistente | — | ✅ OK | 404 |
| 11c | GET | `/categories/{id}` | Sin token | — | ✅ OK | 200 (público) |
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
| 15 | GET | `/products` | Público | ✅ (sin token, intencional) | ✅ OK | 200 + lista — endpoint público |
| 15b | GET | `/products` | Sin token | — | ✅ OK | 200 + lista (público) |
| 16 | GET | `/products/{id}` | Público | ✅ (sin token, intencional) | ✅ OK | 200 — endpoint público |
| 16b | GET | `/products/{id}` | Sin token + ID inexistente | — | ✅ OK | 404 |
| 16c | GET | `/products/{id}` | Sin token | — | ✅ OK | 200 (público) |
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
| 20 | GET | `/variants` | Público | ✅ (sin token, intencional) | ✅ OK | 200 + lista — endpoint público |
| 20b | GET | `/variants` | Sin token | — | ✅ OK | 200 + lista (público) |
| 21 | GET | `/variants/{id}` | Público | ✅ (sin token, intencional) | ✅ OK | 200 con variante |
| 21b | GET | `/variants/{id}` | Sin token + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 21c | GET | `/variants/{id}` | Sin token | — | ✅ OK | 200 (público) |
| 22 | GET | `/variants/product/{productId}` | Público | ✅ (sin token, intencional) | ✅ OK | 200 + lista de variantes del producto |
| 22b | GET | `/variants/product/{productId}` | Sin token + productId inexistente | — | ✅ OK | 404 `Product con id X no encontrado` — corregido: ahora verifica existencia del producto antes de buscar variantes |
| 22c | GET | `/variants/product/{productId}` | Sin token | — | ✅ OK | 200 (público) |
| 23 | POST | `/variants` | seller | ❌ falta header | ✅ OK | 201 + variante creada |
| 23b | POST | `/variants` | admin | — | ✅ OK | 201 + variante creada |
| 23c | POST | `/variants` | Sin token | — | ✅ OK | 401 |
| 23d | POST | `/variants` | buyer | — | ✅ OK | 403 |
| 23e | POST | `/variants` | seller + productId inexistente | — | ✅ OK | 404 `Product con id 9999 no encontrado` — **bug corregido**: era 500, cambiado `RuntimeException` → `NotFoundException` en service |
| 24 | PUT | `/variants/{id}` | seller + ID existente | ❌ falta header | ✅ OK | 200 con datos actualizados |
| 24b | PUT | `/variants/{id}` | seller + ID inexistente | — | ✅ OK | 404 |
| 24c | PUT | `/variants/{id}` | Sin token | — | ✅ OK | 401 |
| 24d | PUT | `/variants/{id}` | buyer | — | ✅ OK | 403 |
| 25 | DELETE | `/variants/{id}` | seller + ID existente | ❌ falta header | ✅ OK | 204 sin body |
| 25b | DELETE | `/variants/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 25c | DELETE | `/variants/{id}` | Sin token | — | ✅ OK | 401 |
| 25d | DELETE | `/variants/{id}` | buyer | — | ✅ OK | 403 |

---

## Inventario

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 26 | GET | `/inventory` | seller o admin | ❌ falta header | ✅ OK | 200 + lista (seller y admin) |
| 26b | GET | `/inventory` | Sin token | — | ✅ OK | 401 |
| 26c | GET | `/inventory` | buyer | — | ✅ OK | 403 |
| 27 | GET | `/inventory/{id}` | seller + ID existente | ❌ falta header | ✅ OK | 200 con item |
| 27b | GET | `/inventory/{id}` | Sin token | — | ✅ OK | 401 |
| 27c | GET | `/inventory/{id}` | buyer | — | ✅ OK | 403 |
| 27d | GET | `/inventory/{id}` | seller + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 28 | GET | `/inventory/variant/{variantId}` | seller + variantId existente | ❌ falta header | ✅ OK | 200 + lista de inventarios |
| 28b | GET | `/inventory/variant/{variantId}` | Sin token | — | ✅ OK | 401 |
| 28c | GET | `/inventory/variant/{variantId}` | buyer | — | ✅ OK | 403 |
| 28d | GET | `/inventory/variant/{variantId}` | seller + variantId inexistente | — | ✅ OK | 404 `ProductVariant con id 9999 no encontrado` — **bug corregido**: era 200 + `[]`, ahora verifica existencia del variant antes de buscar |
| 29 | POST | `/inventory` | seller | ❌ falta header | ✅ OK | 201 + item creado |
| 29b | POST | `/inventory` | admin | — | ✅ OK | 201 + item creado |
| 29c | POST | `/inventory` | Sin token | — | ✅ OK | 401 |
| 29d | POST | `/inventory` | buyer | — | ✅ OK | 403 |
| 29e | POST | `/inventory` | seller + variantId inexistente | — | ✅ OK | 404 `ProductVariant con id 9999 no encontrado` |
| 29f | POST | `/inventory` | seller + warehouseId inexistente | — | ✅ OK | 404 `Warehouse con id 9999 no encontrado` |
| 30 | PUT | `/inventory/{id}` | seller + ID existente | ❌ falta header | ✅ OK | 200 con stock actualizado |
| 30b | PUT | `/inventory/{id}` | Sin token | — | ✅ OK | 401 |
| 30c | PUT | `/inventory/{id}` | buyer | — | ✅ OK | 403 |
| 30d | PUT | `/inventory/{id}` | seller + ID inexistente | — | ✅ OK | 404 |
| 31 | DELETE | `/inventory/{id}` | seller + ID existente | ❌ falta header | ✅ OK | 204 sin body |
| 31b | DELETE | `/inventory/{id}` | Sin token | — | ✅ OK | 401 |
| 31c | DELETE | `/inventory/{id}` | buyer | — | ✅ OK | 403 |
| 31d | DELETE | `/inventory/{id}` | admin + ID inexistente | — | ✅ OK | 404 |

---

## Depósitos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 32 | GET | `/warehouses` | seller o admin | ❌ falta header | ✅ OK | 200 + lista (seller y admin) |
| 32b | GET | `/warehouses` | Sin token | — | ✅ OK | 401 |
| 32c | GET | `/warehouses` | buyer | — | ✅ OK | 403 |
| 33 | GET | `/warehouses/{id}` | seller o admin | ❌ falta header | ✅ OK | 200 con warehouse |
| 33b | GET | `/warehouses/{id}` | seller + ID existente | — | ✅ OK | 200 |
| 33c | GET | `/warehouses/{id}` | admin + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 33d | GET | `/warehouses/{id}` | Sin token | — | ✅ OK | 401 |
| 33e | GET | `/warehouses/{id}` | buyer | — | ✅ OK | 403 |
| 34 | POST | `/warehouses` | admin | ❌ falta header | ✅ OK | 201 + warehouse creado |
| 34b | POST | `/warehouses` | seller | — | ✅ OK | 403 (solo admin puede crear) |
| 34c | POST | `/warehouses` | Sin token | — | ✅ OK | 401 |
| 34d | POST | `/warehouses` | buyer | — | ✅ OK | 403 |
| 35 | PUT | `/warehouses/{id}` | admin | ❌ falta header | ✅ OK | 200 con datos actualizados |
| 35b | PUT | `/warehouses/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 35c | PUT | `/warehouses/{id}` | seller | — | ✅ OK | 403 |
| 35d | PUT | `/warehouses/{id}` | Sin token | — | ✅ OK | 401 |
| 36 | DELETE | `/warehouses/{id}` | admin | ❌ falta header | ✅ OK | 204 sin body |
| 36b | DELETE | `/warehouses/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 36c | DELETE | `/warehouses/{id}` | seller | — | ✅ OK | 403 |
| 36d | DELETE | `/warehouses/{id}` | Sin token | — | ✅ OK | 401 |

---

## Direcciones

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 37 | GET | `/addresses` | admin | ❌ falta header | ✅ OK | 200 + lista (sin passwordHash ✅) |
| 37b | GET | `/addresses` | Sin token | — | ✅ OK | 401 |
| 37c | GET | `/addresses` | buyer | — | ✅ OK | 403 |
| 37d | GET | `/addresses` | seller | — | ✅ OK | 403 |
| 38 | GET | `/addresses/{id}` | admin | ❌ falta header | ✅ OK | 200 con dirección |
| 38b | GET | `/addresses/{id}` | admin + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 38c | GET | `/addresses/{id}` | Sin token | — | ✅ OK | 401 |
| 38d | GET | `/addresses/{id}` | buyer | — | ✅ OK | 403 |
| 39 | GET | `/addresses/user/{userId}` | admin + userId existente | ❌ falta header | ✅ OK | 200 + lista de direcciones |
| 39b | GET | `/addresses/user/{userId}` | admin + userId inexistente | — | ✅ OK | 404 `User con id 9999 no encontrado` — **bug corregido**: era 200 + `[]`, ahora verifica existencia del usuario |
| 39c | GET | `/addresses/user/{userId}` | Sin token | — | ✅ OK | 401 |
| 39d | GET | `/addresses/user/{userId}` | buyer | — | ✅ OK | 403 |
| 40 | POST | `/addresses` | buyer | ❌ falta header | ✅ OK | 201 + dirección creada |
| 40b | POST | `/addresses` | admin | — | ✅ OK | 201 + dirección creada |
| 40c | POST | `/addresses` | Sin token | — | ✅ OK | 401 |
| 40d | POST | `/addresses` | buyer + userId inexistente | — | ✅ OK | 404 `User con id 9999 no encontrado` |
| 40e | POST | `/addresses` | buyer + dirección duplicada | — | ✅ OK | 409 |
| 40f | POST | `/addresses` | buyer + body `{}` | — | ✅ OK | 400 `userId: must be greater than 0, street: must not be blank, city: must not be blank, zipCode: must not be blank` — **bug corregido**: era 404 (buscaba userId=0), agregado `@Validated` + validaciones en DTO |
| 41 | PUT | `/addresses/{id}` | buyer | ❌ falta header | ✅ OK | 200 con datos actualizados |
| 41b | PUT | `/addresses/{id}` | buyer + ID inexistente | — | ✅ OK | 404 |
| 41c | PUT | `/addresses/{id}` | Sin token | — | ✅ OK | 401 |
| 42 | DELETE | `/addresses/{id}` | buyer | ❌ falta header | ✅ OK | 204 sin body |
| 42b | DELETE | `/addresses/{id}` | buyer + ID inexistente | — | ✅ OK | 404 |
| 42c | DELETE | `/addresses/{id}` | Sin token | — | ✅ OK | 401 |

---

## Pedidos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 43 | GET | `/orders` | admin | ✅ | ✅ OK | 200 + lista de órdenes |
| 43b | GET | `/orders` | Sin token | — | ✅ OK | 401 |
| 43c | GET | `/orders` | buyer | — | ✅ OK | 403 |
| 44 | GET | `/orders/{id}` | buyer o admin | ✅ | ✅ OK | 200 con orden existente |
| 44b | GET | `/orders/{id}` | buyer | — | ✅ OK | 200 |
| 44c | GET | `/orders/9999` | admin + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 44d | GET | `/orders/{id}` | Sin token | — | ✅ OK | 401 |
| 44e | GET | `/orders/{id}` | seller | — | ✅ OK | 403 |
| 45 | GET | `/orders/user/{userId}` | admin | ✅ | ✅ OK | 200 + lista de órdenes del usuario |
| 45b | GET | `/orders/user/{userId}` | buyer | — | ✅ OK | 200 |
| 45c | GET | `/orders/user/9999` | admin + userId inexistente | — | ✅ OK | 404 `User con id 9999 no encontrado` — **bug corregido**: era 200 + `[]`, ahora verifica existencia del usuario |
| 45d | GET | `/orders/user/{userId}` | Sin token | — | ✅ OK | 401 |
| 45e | GET | `/orders/user/{userId}` | seller | — | ✅ OK | 403 |
| 46 | POST | `/orders` | buyer | ✅ | ✅ OK | 201 + orden creada en estado PENDING |
| 46b | POST | `/orders` | Sin token | — | ✅ OK | 401 |
| 46c | POST | `/orders` | seller | — | ✅ OK | 403 |
| 46d | POST | `/orders` | buyer + userId inexistente | — | ✅ OK | 404 `User con id 9999 no encontrado` |
| 46e | POST | `/orders` | buyer + addressId inexistente | — | ✅ OK | 404 `Address con id 9999 no encontrado` |
| 46f | POST | `/orders` | buyer + variantId inexistente | — | ✅ OK | 404 `ProductVariant con id 9999 no encontrado` |
| 46g | POST | `/orders` | buyer + items vacíos | — | ✅ OK | 422 `La orden debe contener al menos un item` |
| 46h | POST | `/orders` | buyer + stock insuficiente | — | ✅ OK | 422 `Stock insuficiente para variante X. Stock disponible: Y, solicitado: Z` |
| 47 | PUT | `/orders/{id}` | buyer | ✅ | ✅ OK | 200 con orden actualizada (updatedAt actualizado) |
| 47b | PUT | `/orders/9999` | admin + ID inexistente | — | ✅ OK | 404 |
| 47c | PUT | `/orders/{id}` | Sin token | — | ✅ OK | 401 |
| 47d | PUT | `/orders/{id}` | seller | — | ✅ OK | 403 |
| 48 | PUT | `/orders/{id}/cancel` | buyer | ✅ | ✅ OK | 200 + `status: CANCELLED` |
| 48b | PUT | `/orders/9999/cancel` | admin + ID inexistente | — | ✅ OK | 404 `Order con id 9999 no encontrado` |
| 48c | PUT | `/orders/{id}/cancel` | Sin token | — | ✅ OK | 401 |
| 48d | PUT | `/orders/{id}/cancel` | seller | — | ✅ OK | 403 |
| 48e | PUT | `/orders/{id}/cancel` | buyer + orden ya CANCELLED | — | ✅ OK | 422 `No se puede cancelar una orden con estado: CANCELLED` |
| 49 | DELETE | `/orders/{id}` | admin | ✅ | ✅ OK | 204 sin body — **bug corregido**: era 500 (FK constraint por items/pagos), `deleteOrder` ahora elimina entidades relacionadas en cascada |
| 49b | DELETE | `/orders/9999` | admin + ID inexistente | — | ✅ OK | 404 |
| 49c | DELETE | `/orders/{id}` | Sin token | — | ✅ OK | 401 |
| 49d | DELETE | `/orders/{id}` | buyer | — | ✅ OK | 403 |
| 50 | DELETE | `/orders/expired` | admin | ✅ | ✅ OK | 200 + cantidad de órdenes canceladas (0 si no hay expiradas) |
| 50b | DELETE | `/orders/expired` | Sin token | — | ✅ OK | 401 |
| 50c | DELETE | `/orders/expired` | buyer | — | ✅ OK | 403 |

---

## Items de pedido

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 51 | GET | `/order-items/{id}` | buyer | ✅ | ✅ OK | 200 con item |
| 51b | GET | `/order-items/9999` | admin + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 51c | GET | `/order-items/{id}` | Sin token | — | ✅ OK | 401 |
| 51d | GET | `/order-items/{id}` | seller | — | ✅ OK | 403 |
| 52 | GET | `/order-items/order/{orderId}` | buyer | ✅ | ✅ OK | 200 + lista de items |
| 52b | GET | `/order-items/order/9999` | admin + orderId inexistente | — | ✅ OK | 404 `Order con id 9999 no encontrado` — **bug corregido**: era 200 + `[]`, ahora verifica existencia de la orden |
| 52c | GET | `/order-items/order/{orderId}` | Sin token | — | ✅ OK | 401 |
| 52d | GET | `/order-items/order/{orderId}` | seller | — | ✅ OK | 403 |
| 53 | DELETE | `/order-items/{id}` | buyer (orden con 2+ items) | ✅ | ✅ OK | 204 sin body, recalcula total de la orden |
| 53b | DELETE | `/order-items/{id}` | buyer + último item de la orden | — | ✅ OK | 422 `No se puede borrar el ultimo item de una orden. Cancelar la orden en su lugar.` |
| 53c | DELETE | `/order-items/{id}` | admin + item de orden PAID | — | ✅ OK | 422 `Solo se pueden borrar items de ordenes en estado PENDING. Estado actual: PAID` |
| 53d | DELETE | `/order-items/9999` | buyer + ID inexistente | — | ✅ OK | 404 |
| 53e | DELETE | `/order-items/{id}` | Sin token | — | ✅ OK | 401 |
| 53f | DELETE | `/order-items/{id}` | seller | — | ✅ OK | 403 |

---

## Pagos

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 54 | GET | `/payments` | admin | ✅ | ✅ OK | 200 + lista de pagos |
| 54b | GET | `/payments` | Sin token | — | ✅ OK | 401 |
| 54c | GET | `/payments` | buyer | — | ✅ OK | 403 |
| 54d | GET | `/payments` | seller | — | ✅ OK | 403 |
| 55 | GET | `/payments/{id}` | buyer + ID existente | ✅ | ✅ OK | 200 con payment |
| 55b | GET | `/payments/{id}` | buyer + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 55c | GET | `/payments/{id}` | Sin token | — | ✅ OK | 401 |
| 55d | GET | `/payments/{id}` | seller | — | ✅ OK | 403 |
| 56 | GET | `/payments/order/{orderId}` | buyer + orderId existente | ✅ | ✅ OK | 200 + lista de pagos |
| 56b | GET | `/payments/order/{orderId}` | buyer + orderId inexistente | — | ⚠️ Comportamiento inesperado | 200 + `[]` — no verifica si la orden existe; debería ser 404 |
| 56c | GET | `/payments/order/{orderId}` | Sin token | — | ✅ OK | 401 |
| 56d | GET | `/payments/order/{orderId}` | seller | — | ✅ OK | 403 |
| 57 | POST | `/payments` | buyer + orden PENDING | ✅ | ✅ OK | 201 + `paymentStatus: COMPLETED`, orden pasa a PAID |
| 57b | POST | `/payments` | buyer + simulateFailure=true | — | ✅ OK | 201 + `paymentStatus: FAILED`, orden sigue PENDING (puede reintentar) |
| 57c | POST | `/payments` | Sin token | — | ✅ OK | 401 |
| 57d | POST | `/payments` | seller | — | ✅ OK | 403 |
| 57e | POST | `/payments` | buyer + orderId inexistente | — | ✅ OK | 404 `Order con id 9999 no encontrado` |
| 57f | POST | `/payments` | buyer + orden ya PAID | — | ✅ OK | 422 `Solo se pueden pagar ordenes en estado PENDING. Estado actual: PAID` |

---

## Reseñas

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 58 | GET | `/reviews` | Público | ✅ (sin token, intencional) | ✅ OK | 200 + lista — endpoint público |
| 58b | GET | `/reviews` | Sin token | — | ✅ OK | 200 + lista (público) |
| 59 | GET | `/reviews/{id}` | Público | ✅ (sin token, intencional) | ✅ OK | 200 con reseña |
| 59b | GET | `/reviews/{id}` | Sin token + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 59c | GET | `/reviews/{id}` | Sin token | — | ✅ OK | 200 (público) |
| 60 | GET | `/reviews/product/{productId}` | Público | ✅ (sin token, intencional) | ✅ OK | 200 + lista de reseñas |
| 60b | GET | `/reviews/product/{productId}` | Sin token + productId inexistente | — | ✅ OK | 404 `Product con id 9999 no encontrado` — **bug corregido**: era 200 + `[]`, ahora verifica existencia del producto antes de buscar reseñas |
| 60c | GET | `/reviews/product/{productId}` | Sin token | — | ✅ OK | 200 (público) |
| 61 | POST | `/reviews` | buyer + datos válidos | ❌ falta header | ✅ OK | 201 + reseña creada |
| 61b | POST | `/reviews` | Sin token | — | ✅ OK | 401 |
| 61c | POST | `/reviews` | seller | — | ✅ OK | 403 |
| 61d | POST | `/reviews` | admin | — | ✅ OK | 403 (solo buyer puede crear reseñas) |
| 61e | POST | `/reviews` | buyer + userId inexistente | — | ✅ OK | 404 `User con id 9999 no encontrado` |
| 61f | POST | `/reviews` | buyer + productId inexistente | — | ✅ OK | 404 `Product con id 9999 no encontrado` |
| 61g | POST | `/reviews` | buyer + body `{}` | — | ✅ OK | 400 — **bug corregido**: era 404 (buscaba userId=0), agregado `@Valid` + `@Positive` en `userId`/`productId`, `@Min(1) @Max(5)` en `rating` en `ReviewRequest` |
| 62 | PUT | `/reviews/{id}` | buyer + ID existente | ❌ falta header | ✅ OK | 200 con datos actualizados |
| 62b | PUT | `/reviews/{id}` | admin + ID existente | — | ✅ OK | 200 con datos actualizados |
| 62c | PUT | `/reviews/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 62d | PUT | `/reviews/{id}` | Sin token | — | ✅ OK | 401 |
| 62e | PUT | `/reviews/{id}` | seller | — | ✅ OK | 403 |
| 63 | DELETE | `/reviews/{id}` | admin + ID existente | ❌ falta header | ✅ OK | 204 sin body |
| 63b | DELETE | `/reviews/{id}` | admin + ID inexistente | — | ✅ OK | 404 |
| 63c | DELETE | `/reviews/{id}` | Sin token | — | ✅ OK | 401 |
| 63d | DELETE | `/reviews/{id}` | buyer | — | ✅ OK | 403 |
| 63e | DELETE | `/reviews/{id}` | seller | — | ✅ OK | 403 |

---

## Entregas

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 64 | GET | `/deliveries` | seller o admin | ✅ | ✅ OK | 200 + lista (seller y admin) |
| 64b | GET | `/deliveries` | Sin token | — | ✅ OK | 401 |
| 64c | GET | `/deliveries` | buyer | — | ✅ OK | 403 |
| 65 | GET | `/deliveries/{id}` | Token (¿debería ser público?) | ❌ falta header | ✅ OK | 200 — accesible con cualquier token (buyer, seller, admin) sin `@PreAuthorize` |
| 65b | GET | `/deliveries/{id}` | admin + ID inexistente | — | ✅ OK | 404 `Delivery con id 9999 no encontrado` |
| 65c | GET | `/deliveries/{id}` | Sin token | — | ✅ OK | 401 |
| 66 | GET | `/deliveries/order/{orderId}` | Token (¿debería ser público?) | ❌ falta header | ✅ OK | 200 + lista — accesible con cualquier token sin `@PreAuthorize` |
| 66b | GET | `/deliveries/order/{orderId}` | admin + orderId inexistente | — | ✅ OK | 404 `Order con id 9999 no encontrado` — **bug corregido**: era 200 + `[]`, ahora verifica existencia de la orden |
| 66c | GET | `/deliveries/order/{orderId}` | Sin token | — | ✅ OK | 401 |
| 67 | POST | `/deliveries` | seller o admin | ✅ | ✅ OK | 201 + delivery creado con `status: PENDING` por defecto |
| 67b | POST | `/deliveries` | admin | — | ✅ OK | 201 |
| 67c | POST | `/deliveries` | Sin token | — | ✅ OK | 401 |
| 67d | POST | `/deliveries` | buyer | — | ✅ OK | 403 |
| 67e | POST | `/deliveries` | seller + orderId inexistente | — | ✅ OK | 404 `Order con id 9999 no encontrado` |
| 67f | POST | `/deliveries` | seller + body `{}` | — | ✅ OK | 400 `orderId: must not be null, shippingMethod: must not be blank, trackingNumber: must not be blank` |
| 68 | PUT | `/deliveries/{id}` | seller o admin | ✅ | ✅ OK | 200 con datos actualizados |
| 68b | PUT | `/deliveries/{id}` | admin | — | ✅ OK | 200 |
| 68c | PUT | `/deliveries/9999` | admin + ID inexistente | — | ✅ OK | 404 |
| 68d | PUT | `/deliveries/{id}` | Sin token | — | ✅ OK | 401 |
| 68e | PUT | `/deliveries/{id}` | buyer | — | ✅ OK | 403 |
| 68f | PUT | `/deliveries/{id}` | seller + enum inválido en `status` | — | ✅ OK | 400 `Cuerpo de la solicitud inválido o con valor no reconocido` — **bug corregido**: era 500, agregado handler `HttpMessageNotReadableException` en `GlobalExceptionHandler` |
| 69 | DELETE | `/deliveries/{id}` | admin | ✅ | ✅ OK | 204 sin body |
| 69b | DELETE | `/deliveries/9999` | admin + ID inexistente | — | ✅ OK | 404 |
| 69c | DELETE | `/deliveries/{id}` | Sin token | — | ✅ OK | 401 |
| 69d | DELETE | `/deliveries/{id}` | seller | — | ✅ OK | 403 |
| 69e | DELETE | `/deliveries/{id}` | buyer | — | ✅ OK | 403 |

---

## Seguimiento de envío

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 70 | GET | `/tracking/{id}` | Token (¿debería ser público?) | ❌ falta header | ✅ OK | 200 — accesible con cualquier token (buyer, seller, admin) sin `@PreAuthorize` |
| 70b | GET | `/tracking/{id}` | Sin token | — | ✅ OK | 401 |
| 70c | GET | `/tracking/9999` | buyer + ID inexistente | — | ✅ OK | 404 `ShipmentTracking con id 9999 no encontrado` |
| 71 | GET | `/tracking/delivery/{deliveryId}` | Token (¿debería ser público?) | ❌ falta header | ✅ OK | 200 + lista — accesible con cualquier token sin `@PreAuthorize` |
| 71b | GET | `/tracking/delivery/{deliveryId}` | Sin token | — | ✅ OK | 401 |
| 71c | GET | `/tracking/delivery/9999` | buyer + deliveryId inexistente | — | ✅ OK | 404 `Delivery con id 9999 no encontrado` |
| 72 | POST | `/tracking` | seller | ✅ | ✅ OK | 201 + checkpoint creado con `status: IN_TRANSIT` |
| 72b | POST | `/tracking` | admin | — | ✅ OK | 201 |
| 72c | POST | `/tracking` | Sin token | — | ✅ OK | 401 |
| 72d | POST | `/tracking` | buyer | — | ✅ OK | 403 |
| 72e | POST | `/tracking` | seller + deliveryId inexistente | — | ✅ OK | 404 `Delivery con id 9999 no encontrado` |
| 72f | POST | `/tracking` | seller + body `{}` | — | ✅ OK | 400 `status: must not be null, deliveryId: must not be null, checkpoint: must not be blank` |
| 72g | POST | `/tracking` | seller + status enum inválido | — | ✅ OK | 400 `Cuerpo de la solicitud inválido o con valor no reconocido` |
| 73 | PUT | `/tracking/{id}/status` | seller | ✅ | ✅ OK | 200 con `status` actualizado |
| 73b | PUT | `/tracking/{id}/status` | admin | — | ✅ OK | 200 con `status` actualizado |
| 73c | PUT | `/tracking/{id}/status` | Sin token | — | ✅ OK | 401 |
| 73d | PUT | `/tracking/{id}/status` | buyer | — | ✅ OK | 403 |
| 73e | PUT | `/tracking/9999/status` | seller + ID inexistente | — | ✅ OK | 404 `ShipmentTracking con id 9999 no encontrado` |
| 73f | PUT | `/tracking/{id}/status` | seller + enum inválido | — | ✅ OK | 400 `Cuerpo de la solicitud inválido o con valor no reconocido` |
| 73g | PUT | `/tracking/{id}/status` | seller + body `{}` | — | ✅ OK | 400 `status: must not be null` |

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
| 93 | GET | `/coupons` | Token (¿debería ser público?) | ❌ falta header | ✅ OK | 200 + lista — cualquier rol autenticado (no hay `@PreAuthorize`) |
| 93b | GET | `/coupons` | Sin token | — | ✅ OK | 401 |
| 94 | GET | `/coupons/{id}` | Token (¿debería ser público?) | ❌ falta header | ✅ OK | 200 — cualquier rol autenticado |
| 94b | GET | `/coupons/{id}` | Sin token | — | ✅ OK | 401 |
| 94c | GET | `/coupons/{id}` | Token + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 95 | GET | `/coupons/validate/{code}` | buyer o admin | ❌ falta header | ✅ OK | 200 + cupón válido |
| 95b | GET | `/coupons/validate/{code}` | buyer/admin + code inexistente | — | ✅ OK | 404 `Coupon con id NOEXISTE no encontrado` |
| 95c | GET | `/coupons/validate/{code}` | Sin token | — | ✅ OK | 401 |
| 95d | GET | `/coupons/validate/{code}` | buyer/admin + cupón expirado | — | ✅ OK | 422 `El cupon 'X' esta expirado` |
| 95e | GET | `/coupons/validate/{code}` | buyer/admin + límite de usos agotado | — | ✅ OK | 422 `El cupon 'X' alcanzo el limite de usos (N)` |
| 96 | POST | `/coupons` | admin | ❌ falta header | ✅ OK | 201 + cupón creado |
| 96b | POST | `/coupons` | Sin token | — | ✅ OK | 401 |
| 96c | POST | `/coupons` | buyer / seller | — | — | esperar 403 — `@PreAuthorize("hasRole('admin')")` agregado |
| 96d | POST | `/coupons` | admin + discountId inexistente | — | ✅ OK | 404 `Discount con id 9999 no encontrado` |
| 96e | POST | `/coupons` | admin + body `{}` | — | ✅ OK | 400 `discountId: must be greater than 0, code: must not be blank` — **bug corregido**: era 404 (buscaba discountId=0), agregado `@Valid` en controller y `@Positive`/`@NotBlank` en DTO |
| 96f | POST | `/coupons` | admin + código duplicado | — | ✅ OK | 409 — **bug corregido**: era 500 (constraint DB), agregado duplicate check en `createCoupon` |
| 97 | DELETE | `/coupons/{id}` | admin | ❌ falta header | ✅ OK | 204 sin body |
| 97b | DELETE | `/coupons/{id}` | Sin token | — | ✅ OK | 401 |
| 97c | DELETE | `/coupons/{id}` | buyer / seller | — | — | esperar 403 — `@PreAuthorize("hasRole('admin')")` agregado |
| 97d | DELETE | `/coupons/{id}` | admin + ID inexistente | — | ✅ OK | 404 `Coupon con id 9999 no encontrado` |

---

## Sesiones

| # | Método | Ruta | Auth requerida | Postman OK | Resultado | Notas |
|---|--------|------|----------------|------------|-----------|-------|
| 98 | GET | `/sessions/user/{userId}` | Token | ✅ | ✅ OK | 200 + lista de sesiones del usuario |
| 98b | GET | `/sessions/user/{userId}` | Sin token | — | ✅ OK | 401 |
| 98c | GET | `/sessions/user/{userId}` | Token + userId inexistente | — | ✅ OK | 404 `User con id 9999 no encontrado` — **bug corregido**: era 200 + `[]`, ahora verifica existencia del usuario antes de buscar sesiones |
| 99 | GET | `/sessions/{id}` | Token | ✅ | ✅ OK | 200 con sesión |
| 99b | GET | `/sessions/{id}` | Sin token | — | ✅ OK | 401 |
| 99c | GET | `/sessions/{id}` | Token + ID inexistente | — | ✅ OK | 404 |
| 100 | DELETE | `/sessions/{id}` | Token | ✅ | ✅ OK | 204 sin body |
| 100b | DELETE | `/sessions/{id}` | Sin token | — | ✅ OK | 401 |
| 100c | DELETE | `/sessions/{id}` | Token + ID inexistente | — | ✅ OK | 404 |

---

## Decisiones pendientes

Estos endpoints estaban marcados con `@PreAuthorize` que permite acceso sin rol específico o sin `@PreAuthorize` directamente, pero `SecurityConfig` los filtraba igual porque usaba `.anyRequest().authenticated()`. Resuelto: los endpoints de catálogo se marcaron como públicos, el resto sigue requiriendo token (datos sensibles: direcciones de envío, números de orden, códigos de descuento, mensajes de tickets).

| Endpoint | ¿Público? | Implementación |
|----------|-----------|----------------|
| `GET /categories` y `GET /categories/{id}` | ✅ Sí | `permitAll()` en `SecurityConfig` |
| `GET /products` y `GET /products/{id}` | ✅ Sí | `permitAll()` en `SecurityConfig` |
| `GET /variants`, `GET /variants/{id}`, `GET /variants/product/{id}` | ✅ Sí | `permitAll()` en `SecurityConfig` |
| `GET /reviews`, `GET /reviews/{id}`, `GET /reviews/product/{id}` | ✅ Sí | `permitAll()` en `SecurityConfig` |
| `GET /coupons` | ❌ No | Sigue requiriendo token (códigos de descuento son sensibles) |
| `GET /deliveries/{id}` | ❌ No | Expone direcciones de envío |
| `GET /deliveries/order/{orderId}` | ❌ No | Expone número de orden + envío |
| `GET /tracking/{id}` | ❌ No | Expone ubicación del envío |
| `GET /tracking/delivery/{deliveryId}` | ❌ No | Expone ubicación del envío |
| `GET /support/tickets/{id}` | ❌ No | Expone mensajes privados de soporte |

Tests agregados en `SecurityAccessTest`: 10 casos verifican que los GETs públicos no devuelven 401/403 sin token, 4 casos verifican que las mutaciones (POST) sobre los mismos recursos siguen requiriendo token, y 4 casos verifican que los endpoints sensibles que NO se hicieron públicos siguen devolviendo 401 sin token.

---

## Decisiones de negocio

| # | Pregunta | Decisión |
|---|----------|----------|
| 1 | ¿Puede una persona tener cuenta de comprador y vendedor al mismo tiempo con el mismo email? | **Decisión pendiente de implementar** — El modelo actual asigna un único rol por usuario (`role` campo simple + `email unique`), por lo que hoy no es posible. La solución preferida es multi-rol: `role` pasa a `@ElementCollection` o `@ManyToMany`, el JWT incluye todos los roles, y los `@PreAuthorize` se adaptan. Es un cambio de modelo no trivial; se pospone para una iteración futura. Alternativa de corto plazo: endpoint `PATCH /users/{id}/role` (solo admin) para promover un buyer a seller, pero pierde el historial de roles. |

---

## Preguntas abiertas de la sesión

| # | Pregunta | Contexto | Decisión |
|---|----------|----------|----------|
| 1 | ~~¿Los endpoints de lectura (categories, products, variants, reviews, coupons, tracking) deben ser públicos o requerir token?~~ | Actualmente todos requieren auth por `.anyRequest().authenticated()` en `SecurityConfig`. Si se decide que son públicos, hay que agregarlos al `permitAll()` y actualizar la colección de Postman. | ✅ **RESUELTO** — Solo los endpoints de catálogo (`categories`, `products`, `variants`, `reviews` y sus variantes anidadas) se hicieron públicos. El resto (coupons, deliveries, tracking, support tickets) sigue con token porque expone datos sensibles. Implementado con `requestMatchers(HttpMethod.GET, ...).permitAll()` en `SecurityConfig`. `CategoriesController.getCategories` ajustado para tolerar `Authentication == null` (sin token devuelve solo activas; admin con token sigue viendo todas). Tests en `SecurityAccessTest` cubren los 3 escenarios: GETs públicos sin 401, mutaciones siguen requiriendo token, y endpoints sensibles no afectados. |
| 2 | ~~¿La colección de Postman se actualiza agregando `Authorization: Bearer {{token}}` a los requests que lo necesitan, o se cambia `SecurityConfig` para hacer algunos endpoints verdaderamente públicos?~~ | ~30 requests en la colección no tienen el header. Ambos caminos son válidos, depende de la decisión anterior. | ✅ **RESUELTO** — Se hicieron ambas cosas: 1) Variable de colección `token` agregada (vacía por defecto). 2) Script post-request en `Login` y `Registrar usuario` que captura `pm.response.json().token` y lo guarda con `pm.collectionVariables.set('token', ...)`. 3) Header `Authorization: Bearer {{token}}` agregado a los 54 requests que lo necesitaban. Los 12 que quedan sin header son intencionales: `/auth/register`, `/auth/login` (no necesitan token) y los 10 GETs públicos del punto 1. **Flujo de uso:** ejecutar `Login` una vez, el resto de los requests usan el token automáticamente. |
| 3 | ~~¿El logout debe invalidar solo la sesión actual o todas las sesiones del usuario?~~ | El código actual (`sessionRepository::deleteByUser`) borra todas las sesiones al hacer logout desde cualquier dispositivo. Si el usuario está logueado desde el celular y la PC, al cerrar sesión desde uno se cierra en ambos. | ✅ **DECISIÓN DE NEGOCIO** — Se mantiene el comportamiento actual: el logout cierra **todas** las sesiones del usuario, no solo la actual. Razón: para el alcance del proyecto, prioriza simplicidad y el caso "pánico" (si te robaron un dispositivo, un solo logout te saca de todos lados). **A futuro** se quiere iterar hacia logout por sesión + endpoint aparte `POST /auth/logout-all` para el caso de pánico. Implica identificar la sesión activa por `jti` del JWT o por un ID en la tabla `sessions`. |
| 4 | ~~¿Los límites de rate limiting son adecuados?~~ | Actualmente: 10 req/min para `/auth/*`, 100 req/min para el resto (por IP). ¿Son valores razonables para el proyecto? | ✅ **RESUELTO** — Se mantienen los valores actuales (10 req/min para `/auth/*`, 100 req/min para el resto). En las pruebas de la colección de Postman y los flujos manuales no se observaron falsos positivos ni fricciones. En un escenario productivo con tráfico real se reajustaría con métricas (ver dashboards), pero para el alcance del proyecto los valores son adecuados. |
| 5 | ~~¿Cómo se crea el primer admin en un ambiente nuevo?~~ | No hay endpoint público para esto. Se definió que hay que hacer un INSERT directo en la DB con un hash BCrypt. ¿Se documenta el hash de una contraseña de ejemplo para simplificar el setup inicial? | ✅ **RESUELTO** — Se agregó el archivo `seed-users.sql` con INSERTs idempotentes (`IF NOT EXISTS`) para un admin (`admin@mail.com` / `Test1234!`) y un seller (`seller_test@test.com` / `Test1234!`) de prueba. Se agregó el target `make seed-db` que ejecuta ese archivo dentro del contenedor de SQL Server e imprime las credenciales en la terminal. **Flujo de primera vez:** 1) `make start-all` (levanta DB + arranca app, Hibernate crea las tablas), 2) en otra terminal, `make seed-db` (carga los usuarios de prueba). Las credenciales están documentadas en una sección dedicada del README con un warning de "solo para desarrollo". No se encadenó `seed-db` directamente a `start-all` porque `start-app` es bloqueante y las tablas no existen hasta que la app arranca por primera vez. |
| 6 | ~~**BUG**: `/auth/register` devuelve un token pero no crea sesión en la tabla `sessions`.~~ | ✅ **RESUELTO** — `register` ahora crea una `Session` y la persiste igual que `login`. El token del register funciona directamente en endpoints autenticados sin necesidad de hacer login por separado. | |
| 7 | ~~**COMPORTAMIENTO INCONSISTENTE**: `GET /users/{id}` y `GET /products/{id}` devuelven 204 para ID inexistente.~~ | ✅ **RESUELTO** — cambiado `noContent()` → `notFound()` en `UsersController.java` y `ProductsController.java`. | |
| 8 | ~~**BUG**: `POST /products` sin `@Valid` ni validaciones → 500 con name null.~~ | ✅ **RESUELTO** — agregado `@Valid` en POST y PUT de `ProductsController`, `@NotBlank` en `name` y `@Positive` en `categoryId` en `ProductRequest`. | |
| 9 | ~~**BUG**: `GET /categories/{id}` devuelve 204 para ID inexistente.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `CategoriesController`. | |
| 10 | ~~**BUG**: `POST /categories` duplicado → 500.~~ | ✅ **RESUELTO** — reemplazado `CategoryDuplicateException` por `DuplicateException` (409) en `CategoryServiceImpl`. | |
| 11 | ~~**BUG**: `POST /categories` body vacío → 201 con description null.~~ | ✅ **RESUELTO** — `@NotBlank` en `CategoryRequest.description` + `@Valid` en controller. | |
| 12 | ~~**BUG**: `PUT /categories/{id}` no valida duplicados.~~ | ✅ **RESUELTO** — `updateCategory` ahora verifica con `findByDescription` antes de guardar. | |
| 13 | ~~**BUG**: `DELETE /categories/{id}` con productos → 500.~~ | ✅ **RESUELTO** — `deleteCategory` verifica `productRepository.existsByCategory_Id` y lanza `BusinessRuleException` (422). Se agregó además `PATCH /categories/{id}/deactivate` para soft delete. | |
| 14 | ~~**COMPORTAMIENTO**: Categorías nuevas con `active: false`.~~ | ✅ **RESUELTO** — `@Builder.Default` agregado en `Category.java`. Data preexistente corregida con `UPDATE categories SET is_active = 1`. | |
| 15 | ~~**BUG**: `GET /variants/{id}` con ID inexistente devolvía 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `ProductVariantsController`. | |
| 16 | ~~**BUG**: `POST /variants` con `productId` inexistente devolvía 500.~~ | ✅ **RESUELTO** — `RuntimeException` → `NotFoundException` en `ProductVariantServiceImpl.createVariant` y `updateVariant`. | |
| 17 | ~~**COMPORTAMIENTO**: `GET /variants/product/{productId}` con productId inexistente devolvía 200 + lista vacía.~~ | ✅ **RESUELTO** — `getVariantsByProduct` ahora verifica `productRepository.existsById` y lanza `NotFoundException` (404) si el producto no existe. |
| 18 | ~~**BUG**: `GET /inventory/{id}` con ID inexistente devolvía 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `InventoryController`. |
| 19 | ~~**COMPORTAMIENTO**: `GET /inventory/variant/{variantId}` con variantId inexistente devolvía 200 + lista vacía.~~ | ✅ **RESUELTO** — `getInventoryByVariant` ahora verifica `productVariantRepository.existsById` y lanza `NotFoundException` (404) si la variante no existe. | |
| 20 | ~~**BUG**: `GET /payments/{id}` con ID inexistente devuelve 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `PaymentsController.java`. |
| 21 | ~~**COMPORTAMIENTO**: `GET /payments/order/{orderId}` con orderId inexistente devuelve 200 + `[]` en vez de 404.~~ | ✅ **RESUELTO** — `getPaymentsByOrder` ahora verifica `orderRepository.existsById` y lanza `NotFoundException` (404) si la orden no existe. |
| 22 | ~~**ERROR EN SQL**: `admin_test` tenía email `admin_test@test.com` en el SQL pero `admin@mail.com` en la DB. Hash era correcto, el email estaba mal.~~ | ✅ **RESUELTO** — corregido el email en ambos `create_database(apis).sql`. Credenciales válidas: `admin@mail.com` / `Test1234!`. |
| 23 | ~~**BUG**: `GET /warehouses/{id}` con ID inexistente devuelve 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `WarehousesController.java`. |
| 24 | ~~**BUG**: `GET /addresses/{id}` con ID inexistente devuelve 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `AddressesController.java`. |
| 25 | ~~**COMPORTAMIENTO**: `GET /addresses/user/{userId}` con userId inexistente devuelve 200 + `[]`.~~ | ✅ **RESUELTO** — `getAddressesByUser` ahora verifica `userRepository.existsById` y lanza `NotFoundException` (404) si el usuario no existe. |
| 26 | ~~**BUG**: `POST /addresses` con body vacío devuelve 404 (buscaba userId=0) en vez de 400.~~ | ✅ **RESUELTO** — agregado `@Validated` en controller y `@Positive` en `userId`, `@NotBlank` en `street`, `city`, `zipCode` en `AddressRequest`. |
| 27 | ~~**BUG**: `GET /orders/{id}` con ID inexistente devuelve 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `OrdersController.java`. |
| 28 | ~~**COMPORTAMIENTO**: `GET /orders/user/{userId}` con userId inexistente devuelve 200 + `[]`.~~ | ✅ **RESUELTO** — `getOrdersByUser` ahora verifica `userRepository.existsById` y lanza `NotFoundException` (404) si el usuario no existe. |
| 29 | ~~**BUG**: `DELETE /orders/{id}` con orden que tiene items/pagos devuelve 500 (FK constraint).~~ | ✅ **RESUELTO** — `deleteOrder` ahora elimina en cascada: order items, payments, deliveries y returns antes de borrar la orden. Agregado `@Transactional`. |
| 30 | ~~**BUG**: `GET /order-items/{id}` con ID inexistente devuelve 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `OrderItemsController.java`. |
| 31 | ~~**COMPORTAMIENTO**: `GET /order-items/order/{orderId}` con orderId inexistente devuelve 200 + `[]`.~~ | ✅ **RESUELTO** — `getItemsByOrder` ahora verifica `orderRepository.existsById` y lanza `NotFoundException` (404) si la orden no existe. |
| 32 | ~~**BUG**: `GET /reviews/{id}` con ID inexistente devuelve 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `ReviewsController.java`. |
| 33 | ~~**COMPORTAMIENTO**: `GET /reviews/product/{productId}` con productId inexistente devuelve 200 + `[]`.~~ | ✅ **RESUELTO** — `getReviewsByProduct` ahora verifica `productRepository.existsById` y lanza `NotFoundException` (404) si el producto no existe. |
| 34 | ~~**BUG**: `POST /reviews` con body vacío devuelve 404 (buscaba userId=0) en vez de 400.~~ | ✅ **RESUELTO** — agregado `@Valid` en POST y PUT de `ReviewsController`, `@Positive` en `userId`/`productId`, `@Min(1) @Max(5)` en `rating` en `ReviewRequest`. |
| 35 | ~~**BUG**: `PUT /deliveries/{id}` con valor de enum inválido en `status` devuelve 500 en vez de 400.~~ | ✅ **RESUELTO** — agregado `@ExceptionHandler(HttpMessageNotReadableException.class)` en `GlobalExceptionHandler` que devuelve 400 con mensaje claro. Aplica globalmente a todos los endpoints que reciban enums. |
| 36 | ~~**COMPORTAMIENTO**: `GET /deliveries/order/{orderId}` con orderId inexistente devuelve 200 + `[]` en vez de 404.~~ | ✅ **RESUELTO** — `getDeliveriesByOrder` ahora verifica `orderRepository.existsById` y lanza `NotFoundException` (404) si la orden no existe. Test `getDeliveriesByOrder_deberiaLanzarNotFoundException_cuandoOrderNoExiste` agregado en `DeliveryServiceTest`. |
| 37 | ~~**BUG**: `DELETE /deliveries/{id}` con checkpoints de seguimiento asociados devuelve 500 (FK constraint en `SHIPMENT_TRACKING.delivery_id`).~~ | ✅ **RESUELTO** — `deleteDelivery` en `DeliveryServiceImpl` ahora elimina primero los registros de `ShipmentTracking` con `shipmentTrackingRepository.deleteAll(findByDeliveryId(...))` antes de borrar la delivery. `ShipmentTrackingRepository` inyectado vía `@RequiredArgsConstructor`. Test `deleteDelivery_deberiaEliminar_cuandoIdExiste` actualizado para mockear el repositorio. |
| 38 | ~~**BUG**: `DELETE /orders/{id}` con entregas que tienen checkpoints de seguimiento devuelve 500 (FK constraint en `SHIPMENT_TRACKING.delivery_id`).~~ | ✅ **RESUELTO** — `deleteOrder` en `OrderServiceImpl` ahora, antes de eliminar las deliveries, elimina sus tracking checkpoints iterando sobre las deliveries de la orden. `ShipmentTrackingRepository` inyectado vía `@Autowired`. |
| 39 | ~~**COMPORTAMIENTO**: `GET /sessions/user/{userId}` con userId inexistente devuelve 200 + `[]` en vez de 404.~~ | ✅ **RESUELTO** — `getSessionsByUser` en `SessionServiceImpl` ahora verifica `userRepository.existsById` y lanza `NotFoundException` (404) si el usuario no existe. Test `getSessionsByUser_deberiaLanzarNotFoundException_cuandoUserNoExiste` agregado en `SessionServiceTest`. |
| 40 | ~~**BUG**: `GET /coupons/{id}` con ID inexistente devuelve 204 en vez de 404.~~ | ✅ **RESUELTO** — `noContent()` → `notFound()` en `CouponsController.java`. |
| 41 | ~~**BUG**: `POST /coupons` con body vacío devuelve 404 (buscaba discountId=0) en vez de 400.~~ | ✅ **RESUELTO** — agregado `@Valid` en `createCoupon` en `CouponsController`, `@Positive` en `discountId` y `@NotBlank` en `code` en `CouponRequest`. |
| 42 | ~~**BUG**: `POST /coupons` con código duplicado devuelve 500 (DB unique constraint) en vez de 409.~~ | ✅ **RESUELTO** — `createCoupon` en `CouponServiceImpl` ahora verifica `couponRepository.findByCode` y lanza `DuplicateException` (409) si el código ya existe. Test `createCoupon_deberiaLanzarDuplicateException_cuandoCodigoYaExiste` agregado en `CouponServiceTest`. |
| 43 | ~~**DISEÑO**: `CouponsController` no tenía `@PreAuthorize` en POST y DELETE.~~ | ✅ **RESUELTO** — `POST /coupons` y `DELETE /coupons/{id}` ahora requieren `hasRole('admin')`. |
| 44 | ~~**DECISIÓN PENDIENTE**: `GET /coupons/validate/{code}` — ¿qué roles pueden validar cupones?~~ | ✅ **RESUELTO** — `@PreAuthorize("hasAnyRole('buyer','admin')")`: buyers lo usan en el flujo de compra, admins pueden validar para soporte/testing. |
| 45 | **PENDIENTE (mejora futura)**: parametrizar las credenciales del seed con variables de entorno. | Hoy `seed-users.sql` tiene los emails y hashes BCrypt hardcodeados (`admin@mail.com` / `seller_test@test.com`, ambos con password `Test1234!`). Mientras el proyecto sea académico esto es cómodo, pero cuando deje de serlo conviene migrar a variables tipo `INITIAL_ADMIN_EMAIL` / `INITIAL_ADMIN_PASSWORD` (en `.env.example`) y que `seed-users.sql` las consuma vía `sqlcmd -v` (`$(INITIAL_ADMIN_EMAIL)` dentro del SQL). Eso permite sobrescribir credenciales sin tocar archivos versionados. Implica: 1) agregar las variables a `.env.example`, 2) parametrizar el SQL, 3) ajustar el target `seed-db` del Makefile para pasar las `-v`, 4) actualizar el README. |
