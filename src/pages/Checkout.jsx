import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { CreditCard, Landmark, Wallet, Receipt, Lock } from 'lucide-react'
import { formatARS } from '../data/format.js'

const PAYMENT_METHODS = [
  { id: 'tarjeta', Icon: CreditCard, name: 'Tarjeta de crédito / débito', sub: 'Visa, Mastercard, Amex' },
  { id: 'transferencia', Icon: Landmark, name: 'Transferencia bancaria', sub: 'CBU / CVU — acreditación inmediata' },
  { id: 'mp', Icon: Wallet, name: 'Mercado Pago', sub: 'Con o sin cuenta MP' },
  { id: 'contraentrega', Icon: Receipt, name: 'Pago contra entrega', sub: 'Solo CABA y GBA' },
]

// Vista de checkout (cabecera propia, sin navbar de tienda).
// useState para el método de pago y los datos de la tarjeta.
export default function Checkout({ cart }) {
  const navigate = useNavigate()
  const [method, setMethod] = useState('tarjeta')
  const [card, setCard] = useState({ number: '', holder: '', expiry: '', cvv: '' })

  // Descuento de la maqueta: cupón TECH10 (10%).
  const discount = Math.round(cart.subtotal * 0.1)
  const total = cart.subtotal - discount

  function updateCard(field, value) {
    setCard((prev) => ({ ...prev, [field]: value }))
  }

  return (
    <>
      {/* Cabecera propia del checkout */}
      <div className="ticker">
        <span className="live">LIVE</span>
        <span className="ticker-msg">
          <Lock size={14} /> Conexión segura SSL · Tus datos están protegidos
        </span>
      </div>
      <header className="checkout-header">
        <div className="checkout-header-inner">
          <Link to="/" className="brand">
            <span className="diamond" />
            Vector / tech · Checkout
          </Link>
          <span className="secure secure-row">
            <Lock size={14} /> Compra segura
          </span>
        </div>
      </header>

      <main className="container">
        {/* Pasos */}
        <div className="steps">
          <div className="step done">
            <span className="dot">✓</span> Dirección
          </div>
          <div className="step active">
            <span className="dot">02</span> Pago
          </div>
          <div className="step">
            <span className="dot">03</span> Revisión
          </div>
        </div>

        <div className="checkout-layout">
          {/* Método de pago */}
          <section className="pay-card">
            <h2>Método de pago</h2>
            <p className="desc">Elegí cómo querés pagar tu pedido.</p>

            {PAYMENT_METHODS.map((m) => (
              <div
                key={m.id}
                className={`pay-option ${method === m.id ? 'active' : ''}`}
                onClick={() => setMethod(m.id)}
              >
                <span className="radio" />
                <span className="icon">
                  <m.Icon size={20} />
                </span>
                <div>
                  <div className="o-name">{m.name}</div>
                  <div className="o-sub">{m.sub}</div>
                </div>
              </div>
            ))}

            {/* Datos de tarjeta (solo si el método es tarjeta) */}
            {method === 'tarjeta' && (
              <div className="card-fields">
                <div className="field">
                  <label>NÚMERO DE TARJETA</label>
                  <input
                    placeholder="•••• •••• •••• ••••"
                    value={card.number}
                    onChange={(e) => updateCard('number', e.target.value)}
                  />
                </div>
                <div className="field-row" style={{ gridTemplateColumns: '2fr 1fr 1fr' }}>
                  <div className="field">
                    <label>TITULAR</label>
                    <input
                      placeholder="Nombre como figura en la tarjeta"
                      value={card.holder}
                      onChange={(e) => updateCard('holder', e.target.value)}
                    />
                  </div>
                  <div className="field">
                    <label>VENCIMIENTO</label>
                    <input
                      placeholder="MM / AA"
                      value={card.expiry}
                      onChange={(e) => updateCard('expiry', e.target.value)}
                    />
                  </div>
                  <div className="field">
                    <label>CVV</label>
                    <input
                      placeholder="•••"
                      value={card.cvv}
                      onChange={(e) => updateCard('cvv', e.target.value)}
                    />
                  </div>
                </div>
              </div>
            )}

            <div className="checkout-actions">
              <button className="btn btn-outline" onClick={() => navigate('/carrito')}>
                ← Volver
              </button>
              <button className="btn btn-dark" onClick={() => navigate('/confirmacion')}>
                Confirmar y pagar →
              </button>
            </div>
          </section>

          {/* Mini resumen del pedido */}
          <aside className="order-mini">
            <h3>Tu pedido</h3>
            {cart.items.map((item) => (
              <div className="mini-item" key={`${item.id}-${item.variant}`}>
                <div className="img" />
                <div>
                  <div className="name">{item.name}</div>
                  <div className="sub">
                    {item.variant} · ×{item.qty}
                  </div>
                </div>
                <span className="price">{formatARS(item.price * item.qty)}</span>
              </div>
            ))}

            <div className="mini-divider" />
            <div className="summary-row">
              <span className="muted">Subtotal</span>
              <span>{formatARS(cart.subtotal)}</span>
            </div>
            <div className="summary-row">
              <span className="muted">Descuentos</span>
              <span className="green">− {formatARS(discount)}</span>
            </div>
            <div className="summary-row">
              <span className="muted">Envío</span>
              <span className="green">Gratis</span>
            </div>
            <div className="summary-total">
              <span className="lbl">Total</span>
              <span className="val">{formatARS(total)}</span>
            </div>

            <div className="ship-addr">
              <div className="head">
                <span>DIRECCIÓN DE ENTREGA</span>
                <a href="#" className="link-green">
                  Editar
                </a>
              </div>
              Av. Corrientes 1234, Piso 3
              <br />
              Ciudad Autónoma de Buenos Aires
            </div>
          </aside>
        </div>
      </main>
    </>
  )
}
