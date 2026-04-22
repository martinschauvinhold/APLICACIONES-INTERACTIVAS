# División de Tareas por Módulo

Pensada para trabajo paralelo, minimizando dependencias entre módulos.

---

## Módulo 1 — Usuarios, Autenticación y Seguridad

**Tablas:** `USERS`, `ADDRESSES`, `SESSIONS`

**Tareas:**
- Registro e inicio de sesión (con hash de contraseña)
- Manejo de roles (`buyer`, `seller`, `admin`) y autorización
- CRUD de direcciones de envío
- Gestión de sesiones (expiración, device tracking)
- Middleware de autenticación (JWT) para el resto de los módulos
- Endpoints: `/auth/register`, `/auth/login`, `/auth/logout`, `/users/me`, `/users/addresses`

**Dependencias que expone al resto del equipo:**
- `user_id` autenticado disponible en el contexto de cada request
- Guard/middleware de roles reutilizable

---

## Módulo 2 — Catálogo, Inventario y Precios

**Tablas:** `PRODUCTS`, `CATEGORIES`, `PRODUCT_VARIANTS`, `PRODUCT_IMAGES`, `TAGS`, `INVENTORY`, `WAREHOUSES`, `PRICE_TIERS`, `REVIEWS`

**Tareas:**
- ABM de productos, variantes e imágenes
- Gestión de categorías con jerarquía
- Control de stock por variante y depósito
- Lógica de `PRICE_TIERS` (precio mayorista según cantidad)
- Endpoints de búsqueda y filtrado de productos
- Endpoint de reseñas
- Endpoints: `/products`, `/products/:id`, `/categories`, `/inventory`, `/warehouses`

**Dependencias:**
- Necesita `user_id` con rol `seller` o `admin` para operaciones de escritura (del Módulo 1)

---

## Módulo 3 — Pedidos, Pagos y Descuentos

**Tablas:** `ORDERS`, `ORDER_ITEMS`, `PAYMENTS`, `DISCOUNTS`, `COUPONS`

**Tareas:**
- Creación y gestión de pedidos
- Lógica de aplicación de descuentos y cupones al checkout
- Registro de pagos y manejo de estados (`pending`, `paid`, `failed`)
- Validación de cupones (límite de usos, fecha de expiración, activación)
- Cálculo de subtotales con descuento aplicado
- Endpoints: `/orders`, `/orders/:id`, `/checkout`, `/coupons/validate`, `/payments`

**Dependencias:**
- Necesita `user_id` autenticado (Módulo 1)
- Necesita consultar variantes y precio según cantidad (Módulo 2)

---

## Módulo 4 — Logística, Devoluciones y Soporte

**Tablas:** `DELIVERIES`, `SHIPMENT_TRACKING`, `RETURNS`, `REFUNDS`, `SUPPORT_TICKETS`, `MESSAGES`, `NOTIFICATIONS`

**Tareas:**
- Registro y seguimiento de envíos con checkpoints
- Gestión de devoluciones y reembolsos
- Sistema de tickets de soporte con mensajería interna
- Sistema de notificaciones al usuario (nuevo mensaje, cambio de estado, etc.)
- Endpoints: `/deliveries/:orderId`, `/tracking/:deliveryId`, `/returns`, `/refunds`, `/support/tickets`, `/notifications`

**Dependencias:**
- Necesita `order_id` existente (Módulo 3)
- Necesita `user_id` autenticado (Módulo 1)

---

## Resumen

| Módulo | Área | Tablas |
|---|---|---|
| 1 | Usuarios & Auth | USERS, ADDRESSES, SESSIONS |
| 2 | Catálogo & Stock | PRODUCTS, VARIANTS, INVENTORY, PRICE_TIERS, REVIEWS... |
| 3 | Pedidos & Pagos | ORDERS, ORDER_ITEMS, PAYMENTS, DISCOUNTS, COUPONS |
| 4 | Logística & Soporte | DELIVERIES, TRACKING, RETURNS, REFUNDS, TICKETS... |

## Orden de Implementación Recomendado

1. **Módulo 1** primero — todos los demás dependen de la autenticación
2. **Módulo 2** en paralelo una vez definidos los contratos de auth
3. **Módulo 3** una vez que existan productos y usuarios funcionales
4. **Módulo 4** en paralelo con el Módulo 3, puede empezar por tickets y notificaciones
