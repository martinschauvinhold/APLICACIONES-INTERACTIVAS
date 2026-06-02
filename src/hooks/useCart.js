import { useState } from 'react'

// Hook personalizado que maneja el estado del carrito con useState.
// Se crea una vez en App y se pasa por props a las vistas que lo necesitan
// (Navbar, Catálogo, Detalle, Carrito, Checkout...).
//
// Cada item del carrito: { id, brand, name, variant, price, retailPrice, qty }
//   - price:       precio unitario aplicado (mayorista si qty >= 5)
//   - retailPrice: precio minorista de lista (para calcular el ahorro)

const initialItems = [
  {
    id: 1,
    brand: 'APPLE',
    name: 'MacBook Air M3',
    variant: '8GB / 256GB · Midnight',
    price: 2299000,
    retailPrice: 2299000,
    qty: 1,
  },
  {
    id: 2,
    brand: 'SAMSUNG',
    name: 'Galaxy S24 Ultra',
    variant: '256GB · Phantom Black',
    price: 1575000,
    retailPrice: 1750000,
    qty: 5,
    wholesale: true,
  },
]

export function useCart() {
  const [items, setItems] = useState(initialItems)

  // Agrega un producto; si ya existe la misma variante, suma cantidades.
  function addItem(product, variant, qty = 1) {
    setItems((prev) => {
      const key = `${product.id}-${variant.label}`
      const existing = prev.find((it) => `${it.id}-${it.variant}` === key)
      if (existing) {
        return prev.map((it) =>
          `${it.id}-${it.variant}` === key ? { ...it, qty: it.qty + qty } : it,
        )
      }
      return [
        ...prev,
        {
          id: product.id,
          brand: product.brand,
          name: product.name,
          variant: variant.label,
          price: variant.price,
          retailPrice: variant.price,
          qty,
        },
      ]
    })
  }

  // Cambia la cantidad de una línea (mínimo 1).
  function updateQty(index, qty) {
    setItems((prev) =>
      prev.map((it, i) => (i === index ? { ...it, qty: Math.max(1, qty) } : it)),
    )
  }

  // Elimina una línea del carrito.
  function removeItem(index) {
    setItems((prev) => prev.filter((_, i) => i !== index))
  }

  // Cantidad total de unidades (para el badge del Navbar).
  const count = items.reduce((acc, it) => acc + it.qty, 0)

  // Subtotal con el precio unitario aplicado.
  const subtotal = items.reduce((acc, it) => acc + it.price * it.qty, 0)

  // Ahorro por precios mayoristas (diferencia contra el precio de lista).
  const wholesaleSaving = items.reduce(
    (acc, it) => acc + (it.retailPrice - it.price) * it.qty,
    0,
  )

  return {
    items,
    addItem,
    updateQty,
    removeItem,
    count,
    subtotal,
    wholesaleSaving,
  }
}
