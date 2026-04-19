# E-Commerce de Productos Tecnológicos

Plataforma de e-commerce orientada a la venta de productos tecnológicos en modalidad **minorista y mayorista**. Soporta múltiples roles de usuario (compradores, vendedores y administradores), gestión de inventario por depósito, variantes de producto, descuentos, cupones, seguimiento de envíos y soporte post-venta.

## Stack

- **Backend:** Java + Spring Boot
- **ORM:** JPA / Hibernate
- **Base de datos:** PostgreSQL
- **Autenticación:** JWT
- **Frontend:** React + TypeScript

---

## Modelo de Base de Datos (DER)

### Usuarios y Sesiones
| Tabla | Descripción |
|---|---|
| `USERS` | Usuarios del sistema con rol: `buyer`, `seller` o `admin` |
| `ADDRESSES` | Direcciones de envío asociadas a cada usuario |
| `SESSIONS` | Sesiones activas con device info e IP |

### Catálogo de Productos
| Tabla | Descripción |
|---|---|
| `PRODUCTS` | Producto base con nombre, descripción, marca y estado |
| `CATEGORIES` | Árbol de categorías con slug |
| `PRODUCT_VARIANTS` | Variantes con SKU único, atributos en JSON y precio base |
| `PRODUCT_IMAGES` | Imágenes asociadas, con imagen principal y orden |
| `TAGS` | Etiquetas asociables a productos o categorías |
| `REVIEWS` | Reseñas de compradores con rating (1-5) |

### Inventario y Precios
| Tabla | Descripción |
|---|---|
| `INVENTORY` | Stock por variante y depósito |
| `WAREHOUSES` | Depósitos con nombre, ubicación y contacto |
| `PRICE_TIERS` | Precios escalonados por cantidad (soporte mayorista) |

### Descuentos y Cupones
| Tabla | Descripción |
|---|---|
| `DISCOUNTS` | Reglas de descuento por producto, categoría o precio mínimo |
| `COUPONS` | Códigos de cupón vinculados a descuentos, con límite de uso |

### Pedidos y Pagos
| Tabla | Descripción |
|---|---|
| `ORDERS` | Cabecera de pedido con dirección de envío y estado |
| `ORDER_ITEMS` | Líneas de pedido con precio al momento de compra y descuento aplicado |
| `PAYMENTS` | Registro de pagos con método, transaction ID y estado |

### Logística y Envíos
| Tabla | Descripción |
|---|---|
| `DELIVERIES` | Envío asociado a un pedido con método y número de seguimiento |
| `SHIPMENT_TRACKING` | Checkpoints de seguimiento del envío |
| `RETURNS` | Solicitudes de devolución con motivo y estado |
| `REFUNDS` | Reembolsos asociados a devoluciones |

### Soporte al Cliente
| Tabla | Descripción |
|---|---|
| `SUPPORT_TICKETS` | Tickets de soporte con estado: `open`, `pending`, `closed` |
| `MESSAGES` | Mensajes dentro de cada ticket |
| `NOTIFICATIONS` | Notificaciones del sistema al usuario |

---

## Notas de Diseño

- **Modalidad mayorista:** implementada mediante `PRICE_TIERS`, que define precios por volumen por variante. El precio que aplica se determina según la cantidad del `ORDER_ITEM`.
- **Variantes de producto:** cada `PRODUCT` tiene una o más `PRODUCT_VARIANTS` con SKU único y atributos flexibles en JSON (ej: `{"color": "negro", "ram": "16GB"}`).
- **Descuentos:** la tabla `DISCOUNTS` es flexible: puede aplicar a un producto específico, a toda una categoría, o activarse por precio mínimo. Los cupones referencian a un descuento existente y tienen control de usos.
- **Historial de precios:** `unit_price_at_time` en `ORDER_ITEMS` asegura que el precio pagado quede registrado aunque el producto cambie de precio después.
- **Stock multi-depósito:** `INVENTORY` tiene `warehouse_id` nullable, permitiendo registrar stock sin depósito asignado o con múltiples ubicaciones.

---

Ver [TASK_DIVISION.md](TASK_DIVISION.md) para la división de tareas por módulo.
