# 🛒 E-Commerce de Productos Tecnológicos — Documentación del Proyecto

## Descripción General

Plataforma de e-commerce orientada a la venta de productos tecnológicos en modalidad **minorista y mayorista**. Soporta múltiples roles de usuario (compradores, vendedores y administradores), gestión de inventario por depósito, variantes de producto, descuentos, cupones, seguimiento de envíos y soporte post-venta.

---

## 🗃️ Modelo de Base de Datos (DER)

El diagrama entidad-relación cubre las siguientes áreas funcionales:

### 👤 Usuarios y Sesiones
| Tabla | Descripción |
|---|---|
| `USERS` | Usuarios del sistema con rol: `buyer`, `seller` o `admin` |
| `ADDRESSES` | Direcciones de envío asociadas a cada usuario |
| `SESSIONS` | Sesiones activas con device info e IP |

### 📦 Catálogo de Productos
| Tabla | Descripción |
|---|---|
| `PRODUCTS` | Producto base con nombre, descripción, marca y estado |
| `CATEGORIES` | Árbol de categorías con slug |
| `PRODUCT_VARIANTS` | Variantes con SKU único, atributos en JSON y precio base |
| `PRODUCT_IMAGES` | Imágenes asociadas, con imagen principal y orden |
| `TAGS` | Etiquetas asociables a productos o categorías |
| `REVIEWS` | Reseñas de compradores con rating (1-5) |

### 🏭 Inventario y Precios
| Tabla | Descripción |
|---|---|
| `INVENTORY` | Stock por variante y depósito |
| `WAREHOUSES` | Depósitos con nombre, ubicación y contacto |
| `PRICE_TIERS` | Precios escalonados por cantidad (soporte mayorista) |

### 💰 Descuentos y Cupones
| Tabla | Descripción |
|---|---|
| `DISCOUNTS` | Reglas de descuento por producto, categoría o precio mínimo |
| `COUPONS` | Códigos de cupón vinculados a descuentos, con límite de uso |

### 🛒 Pedidos y Pagos
| Tabla | Descripción |
|---|---|
| `ORDERS` | Cabecera de pedido con dirección de envío y estado |
| `ORDER_ITEMS` | Líneas de pedido con precio al momento de compra y descuento aplicado |
| `PAYMENTS` | Registro de pagos con método, transaction ID y estado |

### 🚚 Logística y Envíos
| Tabla | Descripción |
|---|---|
| `DELIVERIES` | Envío asociado a un pedido con método y número de seguimiento |
| `SHIPMENT_TRACKING` | Checkpoints de seguimiento del envío |
| `RETURNS` | Solicitudes de devolución con motivo y estado |
| `REFUNDS` | Reembolsos asociados a devoluciones |

### 🎧 Soporte al Cliente
| Tabla | Descripción |
|---|---|
| `SUPPORT_TICKETS` | Tickets de soporte con estado: `open`, `pending`, `closed` |
| `MESSAGES` | Mensajes dentro de cada ticket |
| `NOTIFICATIONS` | Notificaciones del sistema al usuario |

---

## 🧩 Notas de Diseño

- **Modalidad mayorista:** implementada mediante `PRICE_TIERS`, que define precios por volumen por variante. El precio que aplica se determina según la cantidad del `ORDER_ITEM`.
- **Variantes de producto:** cada `PRODUCT` tiene una o más `PRODUCT_VARIANTS` con SKU único y atributos flexibles en JSON (ej: `{"color": "negro", "ram": "16GB"}`).
- **Descuentos:** la tabla `DISCOUNTS` es flexible: puede aplicar a un producto específico, a toda una categoría, o activarse por precio mínimo. Los cupones referencian a un descuento existente y tienen control de usos.
- **Historial de precios:** `unit_price_at_time` en `ORDER_ITEMS` asegura que el precio pagado quede registrado aunque el producto cambie de precio después.
- **Stock multi-depósito:** `INVENTORY` tiene `warehouse_id` nullable, permitiendo registrar stock sin depósito asignado o con múltiples ubicaciones.

---

## 👥 División de Tareas (4 personas)

La siguiente división está pensada para trabajo paralelo, minimizando dependencias entre módulos. Cada módulo tiene su propia responsabilidad clara.

---

### 🟦 Módulo 1 — Usuarios, Autenticación y Seguridad

**Tablas:** `USERS`, `ADDRESSES`, `SESSIONS`

**Tareas:**
- Registro e inicio de sesión (con hash de contraseña)
- Manejo de roles (`buyer`, `seller`, `admin`) y autorización
- CRUD de direcciones de envío
- Gestión de sesiones (expiración, device tracking)
- Middleware de autenticación (JWT o similar) para el resto de los módulos
- Endpoints: `/auth/register`, `/auth/login`, `/auth/logout`, `/users/me`, `/users/addresses`

**Dependencias que expone al resto del equipo:**
- `user_id` autenticado disponible en el contexto de cada request
- Guard/middleware de roles reutilizable

---

### 🟩 Módulo 2 — Catálogo, Inventario y Precios

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

### 🟧 Módulo 3 — Pedidos, Pagos y Descuentos

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

### 🟥 Módulo 4 — Logística, Devoluciones y Soporte

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

## 📋 Resumen Visual de la División

| Módulo | Persona | Área | Tablas |
|---|---|---|---|
| 1 | — | Usuarios & Auth | USERS, ADDRESSES, SESSIONS |
| 2 | — | Catálogo & Stock | PRODUCTS, VARIANTS, INVENTORY, PRICE_TIERS, REVIEWS... |
| 3 | — | Pedidos & Pagos | ORDERS, ORDER_ITEMS, PAYMENTS, DISCOUNTS, COUPONS |
| 4 | — | Logística & Soporte | DELIVERIES, TRACKING, RETURNS, REFUNDS, TICKETS... |

---

## 🛠️ Stack Sugerido

- **Backend:** Java + Spring Boot
- **ORM:** JPA / Hibernate
- **Base de datos:** PostgreSQL
- **Autenticación:** JWT
- **Frontend:** React + TypeScript

---

## 🚀 Orden de Implementación Recomendado

1. **Módulo 1** primero — todos los demás dependen de la autenticación
2. **Módulo 2** en paralelo una vez definidos los contratos de auth
3. **Módulo 3** una vez que existan productos y usuarios funcionales
4. **Módulo 4** en paralelo con el Módulo 3, puede empezar por tickets y notificaciones

