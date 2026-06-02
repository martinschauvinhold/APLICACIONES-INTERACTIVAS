import { useNavigate } from 'react-router-dom'
import Stars from './Stars.jsx'
import { formatARS } from '../data/format.js'

// Tarjeta de producto del catálogo.
// Recibe el producto por props y avisa al padre cuando se agrega al carrito.
export default function ProductCard({ product, onAdd }) {
  const navigate = useNavigate()

  return (
    <article className="product-card">
      <div
        className="product-thumb"
        onClick={() => navigate(`/detalle/${product.id}`)}
        style={{ cursor: 'pointer' }}
      >
        {product.badge && (
          <span className={`badge ${product.badge === 'OFERTA' ? 'oferta' : ''}`}>
            {product.badge}
          </span>
        )}
        {product.name}
      </div>

      <div className="product-body">
        <div className="brand">{product.brand}</div>
        <div
          className="name"
          onClick={() => navigate(`/detalle/${product.id}`)}
          style={{ cursor: 'pointer' }}
        >
          {product.name}
        </div>
        <div className="rating">
          <Stars rating={product.rating} /> {product.rating}
        </div>

        <div className="product-foot">
          <span className="price">{formatARS(product.price)}</span>
          <button className="add-btn" onClick={() => onAdd(product)}>
            + Add
          </button>
        </div>
      </div>
    </article>
  )
}
