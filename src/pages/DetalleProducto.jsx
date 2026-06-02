import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import Stars from '../components/Stars.jsx'
import QuantityStepper from '../components/QuantityStepper.jsx'
import { getProductById } from '../data/products.js'
import { formatARS } from '../data/format.js'

// Vista de detalle. Lee el :id de la URL con useParams (ruta dinámica).
export default function DetalleProducto({ cart }) {
  const { id } = useParams()
  const navigate = useNavigate()
  const product = getProductById(id)

  // Estados locales: variante seleccionada, cantidad y miniatura activa.
  const [variantIdx, setVariantIdx] = useState(0)
  const [qty, setQty] = useState(1)
  const [activeThumb, setActiveThumb] = useState(0)

  // Si el id no existe en el catálogo.
  if (!product) {
    return (
      <div style={{ padding: '60px 0', textAlign: 'center' }}>
        <h2>Producto no encontrado</h2>
        <Link to="/" className="link-green">
          ← Volver al catálogo
        </Link>
      </div>
    )
  }

  const variant = product.variants[variantIdx]

  // Agrega la variante seleccionada al carrito y va al carrito.
  function handleAdd() {
    cart.addItem(product, variant, qty)
    navigate('/carrito')
  }

  return (
    <>
      <nav className="breadcrumb">
        <Link to="/">Catálogo</Link> / {product.category} / {product.name}
      </nav>

      <div className="detail">
        {/* Galería */}
        <div>
          <div className="gallery-main">
            {product.name}
            <br />— {product.color} —
          </div>
          <div className="gallery-thumbs">
            {product.gallery.map((g, i) => (
              <div
                key={g}
                className={`thumb ${activeThumb === i ? 'active' : ''}`}
                onClick={() => setActiveThumb(i)}
                style={{ cursor: 'pointer' }}
              >
                {g}
              </div>
            ))}
          </div>
        </div>

        {/* Información */}
        <div className="detail-info">
          <div className="brand">{product.brand}</div>
          <h1>{product.name}</h1>
          <div className="detail-meta">
            <Stars rating={product.rating} /> {product.rating} ({product.reviews}{' '}
            reseñas) · SKU: {product.sku}
          </div>

          <div className="tag-row">
            {product.tags.map((t) => (
              <span key={t} className="tag">
                {t}
              </span>
            ))}
          </div>

          {/* Variantes (estado controlado con useState) */}
          <div className="variants-title">VARIANTES</div>
          {product.variants.map((v, i) => {
            const out = v.stock === 0
            return (
              <div
                key={v.label}
                className={`variant ${variantIdx === i ? 'active' : ''} ${
                  out ? 'out' : ''
                }`}
                onClick={() => !out && setVariantIdx(i)}
              >
                <span className="v-name">{v.label}</span>
                <span className="v-price">{formatARS(v.price)}</span>
                <span className={`v-stock ${out ? 'out' : ''}`}>
                  {out ? 'Agotado' : `${v.stock} en stock`}
                </span>
              </div>
            )
          })}

          {/* Precio + mayorista */}
          <div className="price-box">
            <div>
              <div className="cap">PRECIO UNITARIO</div>
              <div className="unit">{formatARS(variant.price)}</div>
            </div>
            <div className="wholesale">
              <div className="cap">PRECIOS MAYORISTAS</div>
              {product.wholesale.map((w) => (
                <div key={w.range} className={`row ${w.best ? 'best' : ''}`}>
                  {w.range} {formatARS(w.price)}
                </div>
              ))}
            </div>
          </div>

          {/* Compra */}
          <div className="buy-row">
            <QuantityStepper value={qty} onChange={setQty} />
            <button
              className="btn btn-dark"
              onClick={handleAdd}
              disabled={variant.stock === 0}
            >
              Agregar al carrito →
            </button>
          </div>

          <div className="assurances">
            <span>Envío gratis +$50k</span>
            <span>Garantía 12 meses</span>
            <span>Devolución 30 días</span>
          </div>

          {/* Ficha técnica */}
          <div className="spec-card">
            <div className="head">
              <h3>Ficha técnica</h3>
              <span className="sku">{product.sku}</span>
            </div>
            {Object.entries(product.specs).map(([k, v]) => (
              <div key={k} className="spec-row">
                <span className="k">{k}</span>
                <span>{v}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  )
}
