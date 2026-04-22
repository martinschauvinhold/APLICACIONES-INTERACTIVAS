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
| 20 | GET | `/variants` | Token (¿debería ser público?) | ❌ falta header | ✅ OK | 200 + lista (vacía si no hay variantes) |
| 20b | GET | `/variants` | Sin token | — | ✅ OK | 401 |
| 21 | GET | `/variants/{id}` | Token + ID existente | ❌ falta header | ✅ OK | 200 con variante |
| 21b | GET | `/variants/{id}` | Token + ID inexistente | — | ✅ OK | 404 — **bug corregido**: era 204, cambiado `noContent()` → `notFound()` en controller |
| 21c | GET | `/variants/{id}` | Sin token | — | ✅ OK | 401 |
| 22 | GET | `/variants/product/{productId}` | Token + productId existente | ❌ falta header | ✅ OK | 200 + lista de variantes del producto |
| 22b | GET | `/variants/product/{productId}` | Token + productId inexistente | — | ✅ OK | 404 `Product con id X no encontrado` — corregido: ahora verifica existencia del producto antes de buscar variantes |
| 22c | GET | `/variants/product/{productId}` | Sin token | — | ✅ OK | 401 |
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
