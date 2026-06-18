# Módulo 4 — Logística, Devoluciones y Soporte

Cubre las tablas: `DELIVERIES`, `SHIPMENT_TRACKING`, `RETURNS`, `REFUNDS`, `SUPPORT_TICKETS`, `MESSAGES`, `NOTIFICATIONS`.

---

## Cadena de dependencias

```
Order
  └─► Delivery (status debe ser ≠ PENDING para habilitar devolución)
        └─► ShipmentTracking (checkpoints informativos, sin restricción)
  └─► ProductReturn (exige Delivery despachado al crear)
        └─► Refund (exige ProductReturn.status == APPROVED al crear)

SupportTicket
  └─► Message (exige ticket.status ≠ CLOSED al enviar)

User
  └─► Notification (autónoma, no depende de otras entidades del módulo)
```

---

## Reglas de negocio (BusinessRuleException)

Hay exactamente **3 excepciones de negocio** en todo el módulo:

| Nro | Clase | Método | Mensaje | Condición |
|-----|-------|--------|---------|-----------|
| 1 | `ProductReturnServiceImpl` | `createReturn` | `"Cannot create a return: the order has no dispatched delivery"` | Ningún Delivery de la orden tiene status ≠ PENDING |
| 2 | `RefundServiceImpl` | `create` | `"Cannot create a refund: the return must be in APPROVED status"` | ProductReturn.status ≠ APPROVED |
| 3 | `MessageServiceImpl` | `send` | `"Cannot send a message to a closed ticket"` | SupportTicket.status == CLOSED |

---

## Subsistemas

### Delivery

**Entidad:**
- `order` (FK requerida), `shippingMethod`, `trackingNumber`, `status` (DeliveryStatus), `dispatchedAt`

**Estados:**
```
PENDING → DISPATCHED → DELIVERED
                     → RETURNED
```
Sin validación de transición — cualquier cambio es permitido.

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/deliveries` | seller, admin |
| GET | `/deliveries/{id}` | ⚠️ público |
| GET | `/deliveries/order/{orderId}` | ⚠️ público |
| POST | `/deliveries` | seller, admin |
| PUT | `/deliveries/{id}` | seller, admin |
| DELETE | `/deliveries/{id}` | admin |

**Lógica relevante:**
- Al eliminar un Delivery, se eliminan primero todos sus ShipmentTracking asociados (para no violar FK).
- `dispatchedAt` se asigna a `new Date()` siempre, incluso si status es PENDING. ⚠️ Semánticamente incorrecto.

---

### ShipmentTracking

**Entidad:**
- `delivery` (FK requerida), `checkpoint` (descripción del punto), `status` (TrackingStatus), `recordedAt`

**Estados:**
```
IN_TRANSIT, DELAYED, DELIVERED, RETURNED
```
Sin validación de transición.

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/tracking/delivery/{deliveryId}` | ⚠️ público |
| GET | `/tracking/{trackingId}` | ⚠️ público |
| POST | `/tracking` | seller, admin |
| PUT | `/tracking/{trackingId}/status` | seller, admin |

**Lógica relevante:**
- Validación: la Delivery referenciada debe existir.
- Cada checkpoint es inmutable una vez creado; solo su `status` puede actualizarse.

---

### ProductReturn

**Entidad:**
- `order` (FK requerida), `reason` (TEXT), `status` (ReturnStatus), `requestedAt`

**Estados:**
```
PENDING → APPROVED → COMPLETED
        → REJECTED
```
Sin validación de transición en `updateReturn` — un admin puede mover REJECTED → APPROVED.

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/returns` | admin |
| GET | `/returns/{returnId}` | buyer, admin |
| GET | `/returns/order/{orderId}` | buyer, admin |
| POST | `/returns` | buyer |
| PUT | `/returns/{returnId}` | admin |
| DELETE | `/returns/{returnId}` | admin |

**Lógica relevante:**
- **Regla #1:** Solo se puede crear una devolución si la orden tiene al menos un Delivery con status ≠ PENDING. Valida con `deliveryRepository.findByOrderId()`.
- Status default al crear: PENDING (si el request no lo envía).
- ⚠️ No hay validación de ownership: un buyer puede crear una devolución para una orden ajena con solo conocer su `orderId`.
- ⚠️ No hay límite de tiempo para solicitar devoluciones.

---

### Refund

**Entidad:**
- `productReturn` (FK requerida), `amount` (BigDecimal precision 10,2), `currency` (default "ARS"), `status` (RefundStatus), `processedAt`

**Estados:**
```
PENDING → PROCESSED
        → FAILED
```
Sin validación de transición en `updateStatus`.

**Acceso (todo requiere `admin`):**

| Método | Endpoint |
|--------|----------|
| GET | `/refunds/{refundId}` |
| GET | `/refunds/return/{returnId}` |
| POST | `/refunds` |
| PUT | `/refunds/{refundId}/status` |

**Lógica relevante:**
- **Regla #2:** Solo se puede crear un Refund si el ProductReturn asociado tiene status == APPROVED.
- Currency default: "ARS" si el request no la envía.
- Status inicial siempre: PENDING.
- ⚠️ `processedAt` se asigna al crear el Refund (status PENDING), no al procesarlo. Debería asignarse cuando pasa a PROCESSED.
- ⚠️ No hay integración con gateway de pagos. Marcar como PROCESSED solo cambia el campo en BD.
- ⚠️ El monto es ingresado manualmente por el admin. No se calcula automáticamente desde la Order.

---

### SupportTicket

**Entidad:**
- `user` (FK requerida), `subject`, `status` (TicketStatus), `createdAt`

**Estados:**
```
OPEN → PENDING → CLOSED
```
Sin validación de transición.

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/support/tickets` | admin |
| GET | `/support/tickets/{ticketId}` | ⚠️ público |
| POST | `/support/tickets` | buyer, seller |
| PUT | `/support/tickets/{ticketId}/status` | admin |
| GET | `/support/tickets/{ticketId}/messages` | ⚠️ público |
| POST | `/support/tickets/{ticketId}/messages` | ⚠️ público |

**Lógica relevante:**
- Validación: el User referenciado en el request debe existir.
- Status se envía en el request (no tiene default automático).
- ⚠️ No hay endpoint para listar los tickets propios de un usuario autenticado (solo admin puede listar todos).

---

### Message

**Entidad:**
- `ticket` (FK requerida), `sender` (User FK requerida), `content` (TEXT), `sentAt`

**Acceso:** integrado en `/support/tickets/{ticketId}/messages` (ver tabla de SupportTicket).

**Lógica relevante:**
- **Regla #3:** No se puede enviar mensaje a un ticket con status == CLOSED.
- Valida que el ticket exista y que el sender (User) exista antes de persistir.
- Una vez enviado, un mensaje es inmutable (no hay UPDATE ni DELETE de mensajes).

---

### Notification

**Entidad:**
- `user` (FK requerida), `type` (NotificationType), `message`, `isRead` (default false), `createdAt`

**Tipos disponibles:**
```
ORDER_DISPATCHED
RETURN_APPROVED     ← Definido pero nunca disparado automáticamente
REFUND_PROCESSED    ← Definido pero nunca disparado automáticamente
TICKET_CLOSED       ← Definido pero nunca disparado automáticamente
GENERIC
```

**Acceso:**

| Método | Endpoint | Rol |
|--------|----------|-----|
| GET | `/notifications` | admin |
| GET | `/notifications/unread` | ⚠️ público |
| PUT | `/notifications/{id}/read` | ⚠️ público |

**Lógica relevante:**
- `notify(userId, type, message)` crea y persiste la notificación con isRead=false.
- ⚠️ Las notificaciones `RETURN_APPROVED` y `REFUND_PROCESSED` existen en el enum pero **nunca se invocan** en ningún servicio. El flujo de cambio de estado no dispara notificaciones automáticas.
- ⚠️ `getUnread()` no filtra por usuario autenticado — devuelve todas las no leídas (o las de `isRead=false` en general).

---

## Problemas de seguridad identificados

| Problema | Endpoint(s) afectado(s) | Riesgo |
|----------|-------------------------|--------|
| Sin ownership en returns | `GET /returns/{id}`, `GET /returns/order/{orderId}` | Un buyer puede ver devoluciones de otros |
| Sin ownership al crear return | `POST /returns` | Un buyer puede devolver una orden ajena |
| Datos de entrega públicos | `GET /deliveries/{id}`, `GET /deliveries/order/{orderId}` | Cualquiera ve datos de envío sin autenticarse |
| Tickets y mensajes públicos | `GET /support/tickets/{id}`, `/messages` | Conversaciones privadas accesibles sin auth |
| Notificaciones públicas | `GET /notifications/unread`, `PUT /{id}/read` | Cualquiera puede marcar notificaciones de otros |

---

## Qué está completo vs. pendiente

### Implementado
- CRUD básico para todas las entidades
- Las 3 reglas de negocio con `BusinessRuleException`
- Validaciones de FK (orden, usuario, delivery existen)
- Eliminación en cascada manual de ShipmentTracking al borrar Delivery
- Tests unitarios para las 3 reglas de negocio

### Pendiente / Incompleto
- Notificaciones automáticas al cambiar estado (tipos definidos, nunca invocados)
- Validación de ownership (buyer solo opera sobre sus propias órdenes/devoluciones)
- Máquina de estados formal (transiciones no se validan)
- Integración con gateway de pagos en Refund
- Cálculo automático del monto del reembolso desde la orden
- `dispatchedAt` y `processedAt` se asignan al crear, no al despachar/procesar
- Límite de tiempo para solicitar devoluciones (ej: 30 días desde entrega)
- Endpoint para que un buyer liste sus propios tickets
