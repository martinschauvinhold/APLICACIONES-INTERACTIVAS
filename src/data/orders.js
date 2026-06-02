// Pedidos de ejemplo para la vista "Mis pedidos".
// status: 'En preparación' | 'Entregado' | 'Pago pendiente' | 'Cancelado'

export const orders = [
  {
    id: 'VT-20260519-4827',
    status: 'En preparación',
    date: '19 may 2026',
    total: 8841600,
    tracking: 'VT-TRK-9921',
    items: 'MacBook Air M3 · Galaxy S24 Ultra ×5',
  },
  {
    id: 'VT-20260512-3901',
    status: 'Entregado',
    date: '12 may 2026',
    total: 489000,
    tracking: 'VT-TRK-8810',
    items: 'Sony WH-1000XM5',
  },
  {
    id: 'VT-20260430-3540',
    status: 'Entregado',
    date: '30 abr 2026',
    total: 210000,
    tracking: 'VT-TRK-7732',
    items: 'Logitech MX Keys S Combo',
  },
  {
    id: 'VT-20260415-3102',
    status: 'Pago pendiente',
    date: '15 abr 2026',
    total: 1750000,
    tracking: '—',
    items: 'Samsung Galaxy S24 Ultra',
  },
]
