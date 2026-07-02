## Qué QUEDA pendiente (para otra persona)

- **`inventorySlice` sin normalizar** — guarda la forma cruda del back. Falta el DTO
  de `Inventory` para normalizarlo bien; al hacerlo hay que actualizar
  `Vendedor.saveStock` (hoy lee `row.id` y `row.warehouse.id` crudos).
