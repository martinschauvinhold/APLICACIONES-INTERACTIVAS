import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import ProductCard from '../components/ProductCard.jsx'
import { products, categories } from '../data/products.js'

// Vista principal / catálogo. Usa useState para el filtro de categoría y el orden.
export default function Catalogo({ cart }) {
  const navigate = useNavigate()
  const [activeCat, setActiveCat] = useState('Todo')
  const [sort, setSort] = useState('relevantes')
  // Referencia a la grilla de productos para hacer scroll desde "Ver catálogo".
  const gridRef = useRef(null)

  // Selecciona una categoría y baja hasta la grilla de productos.
  function goToCategory(cat) {
    setActiveCat(cat)
    gridRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  // Filtra por categoría seleccionada.
  let visible = products.filter(
    (p) => activeCat === 'Todo' || p.category === activeCat,
  )

  // Ordena según el criterio elegido (crea una copia para no mutar el original).
  visible = [...visible].sort((a, b) => {
    if (sort === 'precio-asc') return a.price - b.price
    if (sort === 'precio-desc') return b.price - a.price
    if (sort === 'rating') return b.rating - a.rating
    return 0
  })

  // Agrega al carrito la variante por defecto (primera) del producto.
  function handleAdd(product) {
    cart.addItem(product, product.variants[0], 1)
  }

  const catTabs = ['Todo', ...categories.map((c) => c.key)]

  return (
    <>
      {/* Sub-navegación de categorías */}
      <div className="cat-nav">
        <div className="cat-nav-inner" style={{ padding: '12px 0' }}>
          {catTabs.map((c) => (
            <button
              key={c}
              className={activeCat === c ? 'active' : ''}
              onClick={() => setActiveCat(c)}
            >
              {c}
            </button>
          ))}
        </div>
      </div>

      {/* Hero */}
      <section className="hero">
        <div className="hero-left">
          <span className="eyebrow">NUEVOS INGRESOS · MAYO 2026</span>
          <h1>
            Tecnología seria,
            <br />
            para gente seria.
          </h1>
          <div className="hero-stats">
            <div>
              <div className="num">2.400+</div>
              <div className="cap">SKUS</div>
            </div>
            <div>
              <div className="num">3</div>
              <div className="cap">DEPÓSITOS</div>
            </div>
            <div>
              <div className="num">48 hs</div>
              <div className="cap">DESPACHO</div>
            </div>
          </div>
        </div>
        <div className="hero-right">
          <div className="big-n">N°01</div>
          <button
            className="btn btn-outline"
            style={{ alignSelf: 'flex-start' }}
            onClick={() => goToCategory('Todo')}
          >
            Ver catálogo →
          </button>
        </div>
      </section>

      {/* Tarjetas de categoría */}
      <section className="cat-cards">
        {categories.map((c, i) => (
          <div
            key={c.key}
            className={`cat-card ${activeCat === c.key ? 'dark' : ''}`}
            onClick={() => goToCategory(c.key)}
            style={{ cursor: 'pointer' }}
          >
            <span className="idx">0{i + 1}</span>
            <div className="row">
              <span>{c.key}</span>
              <c.Icon size={20} />
            </div>
          </div>
        ))}
      </section>

      {/* Grilla de productos */}
      <div className="section-head" ref={gridRef} style={{ scrollMarginTop: 80 }}>
        <h2>
          Todos los productos
          <span className="count">{visible.length} productos</span>
        </h2>
        <select
          className="sort"
          value={sort}
          onChange={(e) => setSort(e.target.value)}
        >
          <option value="relevantes">Más relevantes ↕</option>
          <option value="precio-asc">Precio: menor a mayor</option>
          <option value="precio-desc">Precio: mayor a menor</option>
          <option value="rating">Mejor puntuados</option>
        </select>
      </div>

      <section className="product-grid">
        {visible.map((p) => (
          <ProductCard key={p.id} product={p} onAdd={handleAdd} />
        ))}
      </section>
    </>
  )
}
