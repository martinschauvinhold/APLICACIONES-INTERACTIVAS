# Módulo 2 — Catálogo, Precios e Inventario

Cubre las tablas: `PRODUCTS`, `CATEGORIES`, `PRODUCT_VARIANTS`, `PRODUCT_IMAGES`, `TAGS`, `PRICE_TIERS`, `INVENTORY`, `WAREHOUSES`, `REVIEWS`.

---

## Cadena de dependencias

```
Category
  └─► Product (FK requerida; no puede eliminarse si tiene productos)
        └─► ProductVariant (SKU único; tiene basePrice propio)
              └─► PriceTier (precio por volumen; ≥ 1 tier con minQuantity=1)
              └─► Inventory (stock por depósito; FK a Warehouse)
        └─► ProductImage (url + isPrimary + sortOrder)
        └─► Tag (nullable en product y category — diseño inconsistente)
        └─► Review (user + rating 1-5 + comment)
```

Al procesar un pedido (Módulo 3), `InventoryRepository.findByVariantIdForUpdate()` aplica **PESSIMISTIC_WRITE lock** para evitar race conditions al reservar stock simultáneamente.

---

## Reglas de negocio

| Nro | Clase | Método | Excepción | Condición |
|-----|-------|--------|-----------|-----------|
| 1 | `CategoryServiceImpl` | `createCategory` | `DuplicateException` | Ya existe categoría con la misma `description` |
| 2 | `CategoryServiceImpl` | `updateCategory` | `DuplicateException` | Otra categoría ya usa la nueva `description` |
| 3 | `CategoryServiceImpl` | `deleteCategory` | `BusinessRuleException` | La categoría tiene productos asociados (`ProductRepository.existsByCategory_Id`) |

No hay reglas de negocio en los otros servicios del módulo — el resto son validaciones de existencia (`NotFoundException`).

---

## Subsistemas

### Product

**Entidad:**
- `category` (FK requerida), `name`, `description`, `brand`, `isActive` (default true), `updatedAt`

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/products` | público |
| GET | `/products/{productId}` | público |
| POST | `/products` | seller, admin |
| PUT | `/products/{productId}` | seller, admin |
| DELETE | `/products/{productId}` | admin |

**Lógica relevante:**
- Crea con `isActive=true` y `updatedAt=new Date()`. No hay soft-delete: DELETE elimina la fila.
- No se valida si la categoría está activa al crear/actualizar un producto — se puede asignar un producto a una categoría desactivada.
- No hay endpoint de filtrado por categoría, brand, o precio.

---

### Category

**Entidad:**
- `description` (mapeado a columna `name` en BD — naming confuso), `slug`, `isActive` (default true)

**Estados:**
```
activa (isActive=true) ──PATCH /deactivate──► inactiva (isActive=false)
```
Sin vuelta atrás: no hay endpoint para reactivar.

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/categories` | público (filtra activas si no es admin) |
| GET | `/categories/{categoryId}` | público |
| POST | `/categories` | admin |
| PUT | `/categories/{categoryId}` | admin |
| PATCH | `/categories/{categoryId}/deactivate` | admin |
| DELETE | `/categories/{categoryId}` | admin |

**Lógica especial en `GET /categories`:**
```java
boolean isAdmin = auth != null && auth.getAuthorities().stream()
    .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
return isAdmin ? categoryService.getCategories()
               : categoryService.getActiveCategories();
```
Un admin ve todas (activas e inactivas). Los demás solo ven las activas.

**Lógica relevante:**
- `deleteCategory` valida que no existan productos con esa categoría antes de borrar.
- Al desactivar una categoría, sus productos no se desactivan automáticamente — quedan activos pero en categoría inactiva.
- No hay reactivación de categoría.

---

### ProductVariant

**Entidad:**
- `product` (FK requerida), `sku` (unique), `attributes` (JSON), `basePrice` (BigDecimal), `updatedAt`

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/variants` | público |
| GET | `/variants/{variantId}` | público |
| GET | `/variants/product/{productId}` | público |
| POST | `/variants` | seller, admin |
| PUT | `/variants/{variantId}` | seller, admin |
| DELETE | `/variants/{variantId}` | seller, admin |

**Lógica relevante:**
- `sku` único se garantiza por constraint de BD (no por validación en servicio).
- `basePrice` acepta valores negativos — no hay `@Positive` en `ProductVariantRequest`.
- Eliminar una variante no elimina en cascada sus `PriceTier` ni `Inventory`.

---

### PriceTier

**Entidad:**
- `variant` (FK requerida), `minQuantity`, `unitPrice` (BigDecimal), `currency` (default "ARS")

**Lógica de selección de precio al procesar un pedido (Módulo 3):**
```
Dado un pedido de cantidad Q de la variante V:
1. Obtener todos los PriceTiers de V
2. Filtrar los que tienen minQuantity <= Q
3. Seleccionar el de mayor minQuantity (el más específico)
4. Usar su unitPrice
```

**Estado actual del módulo:**
- ✅ Entidad y repositorio implementados
- ❌ **No existe service ni controller para PriceTier**

**Sobre si habilitar endpoints públicos:**

No hay frontend en el proyecto (pura API). Sin embargo, si se implementa un front de catálogo:
- `GET /price-tiers/variant/{variantId}` **debería ser público** — el comprador necesita ver los precios por volumen antes de hacer un pedido, al igual que ya puede ver `/variants` (que incluye `basePrice`).
- No hay información sensible en un PriceTier (solo umbrales de cantidad y precio).
- La misma lógica que permitió exponer `/variants` públicamente aplica aquí.

**Recomendación:** Al implementar el controller, agregar en `SecurityConfig`:
```java
.requestMatchers(HttpMethod.GET, "/price-tiers", "/price-tiers/**").permitAll()
```

---

### Inventory

**Entidad:**
- `variant` (FK requerida), `warehouse` (FK requerida), `stockQuantity` (int, default 0), `lastUpdated`

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/inventory` | seller, admin |
| GET | `/inventory/{inventoryId}` | seller, admin |
| GET | `/inventory/variant/{variantId}` | seller, admin |
| POST | `/inventory` | seller, admin |
| PUT | `/inventory/{inventoryId}` | seller, admin |
| DELETE | `/inventory/{inventoryId}` | seller, admin |

**Lógica relevante:**
- `updateInventory` solo actualiza `stockQuantity` — no cambia variante ni warehouse.
- `findByVariantIdForUpdate()` usa `@Lock(LockModeType.PESSIMISTIC_WRITE)` — invocado por `OrderServiceImpl` al procesar pagos para evitar overselling.
- No hay validación de `stockQuantity >= 0` al actualizar — se puede poner stock negativo.
- Inventario completamente restringido a roles internos; ningún dato de stock es público.

---

### Warehouse

**Entidad:**
- `name`, `location`, `contactPhone`

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/warehouses` | seller, admin |
| GET | `/warehouses/{warehouseId}` | seller, admin |
| POST | `/warehouses` | admin |
| PUT | `/warehouses/{warehouseId}` | admin |
| DELETE | `/warehouses/{warehouseId}` | admin |

**Lógica relevante:**
- No valida si el warehouse tiene inventario antes de eliminarlo — puede dejar filas `INVENTORY` con FK rota si la BD no tiene cascade o FK constraint.
- No hay soft-delete.

---

### Review

**Entidad:**
- `user` (FK requerida), `product` (FK requerida), `rating` (int), `comment` (TEXT), `createdAt`

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/reviews` | público |
| GET | `/reviews/{reviewId}` | público |
| GET | `/reviews/product/{productId}` | público |
| POST | `/reviews` | buyer |
| PUT | `/reviews/{reviewId}` | buyer, admin |
| DELETE | `/reviews/{reviewId}` | admin |

**Lógica relevante:**
- `rating` validado 1-5 en DTO (`@Min(1) @Max(5)`), pero no en la entidad — si se bypasea la validación llega a la BD.
- No hay validación de ownership: un buyer puede editar una reseña de otro pasando un `reviewId` ajeno.
- No hay límite de una reseña por producto por usuario.
- `createdAt` no se actualiza al editar — queda la fecha original de creación.

---

### ProductImage y Tag

**Estado actual:**
- Ambas tienen entidad y repositorio, pero **no tienen service ni controller**.
- `ProductImage`: imagen con `url`, `isPrimary`, `sortOrder`. Vinculada a Product.
- `Tag`: vinculada a Product **o** Category (ambas FK son nullable — diseño inconsistente; un tag podría no pertenecer a ninguno).

---

## Seguridad — SecurityConfig

**Archivo:** `src/main/java/com/uade/tpo/demo/config/SecurityConfig.java`

**Configuración actual:**
```java
.requestMatchers("/auth/register", "/auth/login", "/error").permitAll()
.requestMatchers(HttpMethod.GET, "/categories", "/categories/**").permitAll()
.requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
.requestMatchers(HttpMethod.GET, "/variants", "/variants/**").permitAll()
.requestMatchers(HttpMethod.GET, "/reviews", "/reviews/**").permitAll()
.anyRequest().authenticated()
```

- **JWT stateless** — sin sesiones.
- **CSRF deshabilitado** — correcto para API REST.
- **Method Security** habilitada (`@EnableMethodSecurity`) — los controllers usan `@PreAuthorize` para el control de roles.

**Mecanismo dual:**
La seguridad opera en dos capas:
1. `SecurityConfig` define qué requiere autenticación (nivel de request).
2. `@PreAuthorize` en controllers define qué roles pueden acceder (nivel de método).

---

## Problemas de seguridad identificados

| Problema | Endpoint(s) afectado(s) | Riesgo |
|----------|-------------------------|--------|
| Sin ownership en Reviews | `PUT /reviews/{id}` | Un buyer puede editar la reseña de otro |
| Sin límite de reseñas | `POST /reviews` | Un buyer puede dejar múltiples reseñas por producto |
| userId en ReviewRequest | `POST /reviews` | El buyer declara su propio userId en el body — no se extrae del token |
| Sin validación de basePrice | `POST /variants` | Se pueden crear variantes con precio negativo |
| Sin validación de stockQuantity | `PUT /inventory/{id}` | Se puede setear stock negativo |
| Cascada de eliminación ausente | `DELETE /variants/{id}` | Deja PriceTier e Inventory huérfanos |
| Tag con FKs ambas nullable | `Tag.java` | Un tag puede existir sin pertenecer a nada |
| Categoría activa no validada | `POST/PUT /products` | Producto puede crearse en categoría desactivada |

---

## Qué está completo vs. pendiente

### Implementado
- CRUD completo para Products, Categories, ProductVariants, Inventory, Warehouses, Reviews
- Reglas de negocio: duplicado de categoría y bloqueo de eliminación con productos
- Validaciones de FK y existencia en servicios
- `PESSIMISTIC_WRITE lock` en InventoryRepository para procesamiento de pagos
- Endpoints públicos de lectura para catálogo (products, categories, variants, reviews)
- Lógica de filtrado de categorías activas/inactivas según rol del llamante
- `@PreAuthorize` con roles diferenciados (buyer, seller, admin)
- GlobalExceptionHandler centralizado

### Pendiente / Incompleto
- PriceTier: entidad y repo existen, **faltan service y controller**
- ProductImage: entidad y repo existen, **faltan service y controller**
- Tag: entidad y repo existen, **faltan service y controller**
- Validación de ownership en Reviews (buyer solo edita sus propias reseñas)
- Una reseña por producto por usuario
- Extracción de userId desde JWT en lugar de aceptarlo en el body
- Validación de `basePrice > 0` en ProductVariantRequest
- Validación de `stockQuantity >= 0` en InventoryRequest
- Cascada de eliminación al borrar variante
- Validación de categoría activa al crear/actualizar producto
- Soft-delete para productos (marcar `isActive=false` en lugar de borrar)
- Endpoint para reactivar categorías
- Filtrado y búsqueda de productos (por categoría, precio, rating)
- Rating promedio calculado por producto
- Límite de tiempo para editar reseñas
