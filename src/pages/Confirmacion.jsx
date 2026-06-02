import { Link, useNavigate } from 'react-router-dom'
import { CircleCheck, Check } from 'lucide-react'
import { formatARS } from '../data/format.js'

// Línea de tiempo del envío.
const timeline = [
  { name: 'Pago confirmado', time: '19/05/2026 14:32 hs', state: 'done' },
  { name: 'Pedido en preparación', time: '19/05/2026 14:45 hs', state: 'current' },
  { name: 'Despachado', time: 'Estimado: hoy antes de las 18 hs', state: '' },
  { name: 'En camino', time: 'Estimado: 20/05/2026', state: '' },
  { name: 'Entregado', time: 'Estimado: 21/05/2026', state: '' },
]

// Vista de confirmación de compra.
export default function Confirmacion() {
  const navigate = useNavigate()

  return (
    <>
      <div className="ticker">
        <span className="live">LIVE</span>
        <span className="ticker-msg">
          <CircleCheck size={14} /> Pedido #VT-20260519-4827 confirmado · Preparando tu
          envío
        </span>
      </div>

      <div className="confirm-wrap">
        <div className="confirm-hero">
          <div>
            <div className="check">
              <Check size={24} />
            </div>
            <h1>¡Pedido confirmado!</h1>
            <p>Te enviamos un email de confirmación a juan@email.com</p>
            <div className="ref">
              Pedido <strong>#VT-20260519-4827</strong> · Tracking: VT-TRK-9921
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <button className="btn btn-dark" onClick={() => navigate('/mis-pedidos')}>
              Ver mis pedidos →
            </button>
            <button className="btn btn-outline" onClick={() => navigate('/')}>
              Seguir comprando
            </button>
          </div>
        </div>

        <div className="confirm-grid">
          {/* Detalle del pedido */}
          <div className="spec-card">
            <h3 style={{ marginBottom: 14 }}>Detalle del pedido</h3>
            <div className="mini-item">
              <div className="img" />
              <div>
                <div className="name">MacBook Air M3</div>
                <div className="sub">8GB / 256GB Midnight ×1</div>
              </div>
              <span className="price">{formatARS(2299000)}</span>
            </div>
            <div className="mini-item">
              <div className="img" />
              <div>
                <div className="name">Galaxy S24 Ultra</div>
                <div className="sub">256GB Phantom Black ×5</div>
              </div>
              <span className="price">{formatARS(7875000)}</span>
            </div>

            <div className="mini-divider" />
            <div className="summary-row">
              <span className="muted">Subtotal</span>
              <span>{formatARS(10174000)}</span>
            </div>
            <div className="summary-row">
              <span className="muted">Ahorro mayorista</span>
              <span className="green">− {formatARS(875000)}</span>
            </div>
            <div className="summary-row">
              <span className="muted">Descuento TECH10</span>
              <span className="green">− {formatARS(1017400)}</span>
            </div>
            <div className="summary-row">
              <span className="muted">Envío</span>
              <span className="green">Gratis</span>
            </div>
            <div className="summary-total">
              <span className="lbl">Total pagado</span>
              <span className="val">{formatARS(9156600)}</span>
            </div>

            <div className="ship-addr">
              <div className="head">
                <span>DIRECCIÓN DE ENTREGA</span>
              </div>
              Av. Corrientes 1234, Piso 3 · Ciudad Autónoma de Buenos Aires · CP 1043
            </div>
          </div>

          {/* Seguimiento del envío */}
          <div className="spec-card">
            <div className="head">
              <h3>Seguimiento del envío</h3>
              <span className="sku">VT-TRK-9921</span>
            </div>
            <ul className="timeline">
              {timeline.map((t) => (
                <li key={t.name} className={t.state}>
                  <div>
                    <div className="t-name">{t.name}</div>
                    <div className="t-time">{t.time}</div>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <p style={{ marginTop: 24, textAlign: 'center' }}>
          <Link to="/" className="link-green">
            ← Volver al inicio
          </Link>
        </p>
      </div>
    </>
  )
}
