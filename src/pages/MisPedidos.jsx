import { useState } from 'react'
import OrderCard from '../components/OrderCard.jsx'
import { orders } from '../data/orders.js'
import { formatARS } from '../data/format.js'

// Pestañas de filtro y a qué estados corresponden.
const TABS = {
  Todos: () => true,
  'En curso': (o) => o.status === 'En preparación' || o.status === 'Pago pendiente',
  Entregados: (o) => o.status === 'Entregado',
  Cancelados: (o) => o.status === 'Cancelado',
}

// Vista "Mis pedidos". useState para la pestaña activa.
export default function MisPedidos() {
  const [tab, setTab] = useState('Todos')

  const visible = orders.filter(TABS[tab])

  // Estadísticas calculadas sobre todos los pedidos.
  const totalSpent = orders.reduce((acc, o) => acc + o.total, 0)
  const delivered = orders.filter((o) => o.status === 'Entregado').length
  const inProgress = orders.filter((o) => o.status === 'En preparación').length

  return (
    <>
      <h1 className="page-title">Mis pedidos</h1>

      <div className="tabs">
        {Object.keys(TABS).map((t) => (
          <button
            key={t}
            className={tab === t ? 'active' : ''}
            onClick={() => setTab(t)}
          >
            {t}
          </button>
        ))}
      </div>

      {visible.length === 0 ? (
        <p style={{ color: 'var(--text-soft)', padding: '20px 0' }}>
          No hay pedidos en esta categoría.
        </p>
      ) : (
        visible.map((o) => <OrderCard key={o.id} order={o} />)
      )}

      {/* Estadísticas */}
      <div className="orders-stats">
        <div className="stat">
          <div className="num">{orders.length}</div>
          <div className="cap">PEDIDOS TOTALES</div>
        </div>
        <div className="stat">
          <div className="num">{formatARS(totalSpent)}</div>
          <div className="cap">GASTADO EN TOTAL</div>
        </div>
        <div className="stat">
          <div className="num">{delivered}</div>
          <div className="cap">ENTREGADOS</div>
        </div>
        <div className="stat">
          <div className="num">{inProgress}</div>
          <div className="cap">EN CURSO</div>
        </div>
      </div>
    </>
  )
}
