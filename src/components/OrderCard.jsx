import { formatARS } from '../data/format.js'

// Mapea el estado del pedido a la clase del badge de color.
const statusClass = {
  'En preparación': 'prep',
  Entregado: 'entregado',
  'Pago pendiente': 'pendiente',
  Cancelado: 'cancelado',
}

// Tarjeta de un pedido en "Mis pedidos". El pedido llega por props.
export default function OrderCard({ order }) {
  return (
    <article className="order-card">
      <div className="top">
        <span className="oid">#{order.id}</span>
        <span className={`status-badge ${statusClass[order.status]}`}>
          {order.status}
        </span>
        <div className="meta">
          <span className="date">{order.date}</span>
          <span className="total">{formatARS(order.total)}</span>
          <span style={{ color: 'var(--text-soft)' }}>›</span>
        </div>
      </div>

      <div className="bottom">
        <div className="imgs">
          <div className="img" />
          <div className="img" />
        </div>
        <div className="desc">
          <div className="items">{order.items}</div>
          <div className="track">Tracking: {order.tracking}</div>
        </div>
        <a className="detail-link link-green" href="#">
          Ver detalle →
        </a>
      </div>
    </article>
  )
}
