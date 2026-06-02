import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import QuantityStepper from '../components/QuantityStepper.jsx'
import { formatARS } from '../data/format.js'

// Cupones válidos de la maqueta.
const COUPONS = { TECH10: 0.1 }

// Vista del carrito. Las cantidades se editan sobre el estado del carrito (props)
// y el cupón se maneja con useState local.
export default function Carrito({ cart }) {
  const navigate = useNavigate()
  const [couponInput, setCouponInput] = useState('')
  const [coupon, setCoupon] = useState('TECH10') // viene aplicado en la maqueta

  const discountRate = coupon ? COUPONS[coupon] || 0 : 0
  const discount = Math.round(cart.subtotal * discountRate)
  const total = cart.subtotal - discount

  function applyCoupon() {
    const code = couponInput.trim().toUpperCase()
    if (COUPONS[code]) {
      setCoupon(code)
      setCouponInput('')
    }
  }

  if (cart.items.length === 0) {
    return (
      <div style={{ padding: '60px 0', textAlign: 'center' }}>
        <h2>Tu carrito está vacío</h2>
        <button className="btn btn-dark" onClick={() => navigate('/')}>
          ← Seguir comprando
        </button>
      </div>
    )
  }

  return (
    <>
      <h1 className="page-title">
        Carrito de compras
        <span className="count">{cart.items.length} productos</span>
      </h1>

      <div className="cart-layout">
        {/* Tabla de items */}
        <div>
          <div className="cart-table">
            <div className="cart-th">
              <span>PRODUCTO</span>
              <span>VARIANTE</span>
              <span>CANTIDAD</span>
              <span>PRECIO</span>
              <span>SUBTOTAL</span>
            </div>

            {cart.items.map((item, i) => (
              <div className="cart-row" key={`${item.id}-${item.variant}`}>
                <div className="cart-prod">
                  <div className="img" />
                  <div>
                    <div className="brand">{item.brand}</div>
                    <div className="name">{item.name}</div>
                    <div className="var">{item.variant}</div>
                    {item.wholesale && (
                      <div className="whole">★ Precio mayorista aplicado</div>
                    )}
                  </div>
                </div>
                <span>{item.variant.split('·')[0].trim()}</span>
                <QuantityStepper
                  value={item.qty}
                  onChange={(q) => cart.updateQty(i, q)}
                />
                <span>{formatARS(item.price)}</span>
                <div className="cart-sub">
                  <span>{formatARS(item.price * item.qty)}</span>
                  <button className="cart-x" onClick={() => cart.removeItem(i)}>
                    ✕
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Cupón de descuento */}
          <div className="coupon-box">
            <div className="cap">CÓDIGO DE DESCUENTO</div>
            {coupon ? (
              <div className="coupon-applied">
                <span className="coupon-chip">
                  ✓ {coupon} — {discountRate * 100}% de descuento
                </span>
                <button
                  className="cart-x"
                  onClick={() => setCoupon(null)}
                  style={{ fontSize: 14 }}
                >
                  Quitar ✕
                </button>
              </div>
            ) : (
              <div className="coupon-input">
                <input
                  placeholder="Ingresá tu cupón (ej: TECH10)"
                  value={couponInput}
                  onChange={(e) => setCouponInput(e.target.value)}
                />
                <button className="btn btn-outline" onClick={applyCoupon}>
                  Aplicar
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Resumen del pedido */}
        <aside className="summary">
          <h3>Resumen del pedido</h3>
          <div className="summary-row">
            <span className="muted">Subtotal ({cart.items.length} productos)</span>
            <span>{formatARS(cart.subtotal)}</span>
          </div>
          {cart.wholesaleSaving > 0 && (
            <div className="summary-row">
              <span className="muted">Ahorro mayorista</span>
              <span className="green">− {formatARS(cart.wholesaleSaving)}</span>
            </div>
          )}
          {discount > 0 && (
            <div className="summary-row">
              <span className="muted">Descuento {coupon} ({discountRate * 100}%)</span>
              <span className="green">− {formatARS(discount)}</span>
            </div>
          )}
          <div className="summary-row">
            <span className="muted">Envío estimado</span>
            <span className="green">Gratis</span>
          </div>

          <div className="summary-total">
            <span className="lbl">Total</span>
            <span className="val">{formatARS(total)}</span>
          </div>

          <button
            className="btn btn-dark btn-block"
            onClick={() => navigate('/checkout')}
          >
            Iniciar compra →
          </button>
          <button
            className="btn btn-outline btn-block"
            style={{ marginTop: 10 }}
            onClick={() => navigate('/')}
          >
            ← Seguir comprando
          </button>

          <ul className="assure-list">
            <li>Pago seguro SSL</li>
            <li>Envío a todo el país</li>
            <li>Devolución en 30 días</li>
          </ul>
        </aside>
      </div>
    </>
  )
}
