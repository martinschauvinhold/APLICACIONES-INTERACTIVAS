import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import ProductImage from '../components/ProductImage';
import Stars from '../components/Stars';
import Tag from '../components/Tag';
import Btn from '../components/Btn';
import SpecSheet from '../components/SpecSheet';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { selectProductById } from '../store/selectors';
import { add } from '../store/slices/cartSlice';
import { toggleCartDrawer } from '../store/slices/uiSlice';

/**
 * Product — detalle del producto.
 *
 * Hooks:
 *   - useParams: extrae el id del producto de la URL
 *   - useNavigate: para volver al catálogo
 *   - useSelector: trae el producto del store
 *   - useState: estado local de variante seleccionada y cantidad
 */
function Product() {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const product = useSelector(selectProductById(id));

  // Si el producto no existe, devolver Empty
  if (!product) {
    return (
      <main className="screen">
        <Empty
          title="Producto no encontrado"
          hint="Es probable que el id no exista en el catálogo."
          action={<Btn variant="primary" onClick={() => navigate('/')}>Volver al catálogo</Btn>}
        />
      </main>
    );
  }

  const [variantId, setVariantId] = useState(product.variants[0].variant_id);
  const [qty, setQty] = useState(1);

  const variant = product.variants.find((v) => v.variant_id === variantId) || product.variants[0];

  // Aplica precios por cantidad (descuento mayorista)
  const tier = [...variant.tiers].reverse().find((t) => qty >= t.min_quantity) || variant.tiers[0];
  const unit = tier.unit_price;
  const savings = variant.base_price - unit;

  const handleAdd = () => {
    dispatch(add({ variantId: variant.variant_id, quantity: qty }));
    dispatch(toggleCartDrawer());
  };

  return (
    <main className="screen">
      <nav className="crumbs">
        <Link to="/">← catálogo</Link>
        <span>/</span>
        <span>{product.brand}</span>
        <span>/</span>
        <span className="crumbs-current">{product.name}</span>
      </nav>

      <div className="product-grid">
        <div className="product-gallery">
          <div className="gallery-main">
            <ProductImage src={product.image_url} alt={product.name} ratio="4/3" />
          </div>
          <div className="gallery-thumbs">
            {[0, 1, 2, 3].map((i) => (
              <div key={i} className="gallery-thumb">
                <ProductImage src={product.image_url} alt={`Vista ${i + 1}`} ratio="1/1" />
              </div>
            ))}
          </div>
        </div>

        <div className="product-info">
          <div className="product-brand">{product.brand}</div>
          <h1 className="product-name">{product.name}</h1>
          <div className="product-meta">
            <Stars rating={product.rating} count={product.reviewCount} />
            <span className="product-meta-sep mono">·</span>
            <span className="mono">REF {variant.sku}</span>
          </div>

          <div className="product-tags">
            {product.tags.map((t) => <Tag key={t}>{t}</Tag>)}
          </div>

          <p className="product-desc">{product.description}</p>

          <SpecSheet product={product} variant={variant} />

          {product.variants.length > 1 && (
            <div className="variants">
              <div className="variants-label">VARIANTES</div>
              <div className="variants-list">
                {product.variants.map((v) => (
                  <button
                    key={v.variant_id}
                    className={`variant ${v.variant_id === variantId ? 'is-selected' : ''} ${v.stock === 0 ? 'is-out' : ''}`}
                    onClick={() => v.stock > 0 && setVariantId(v.variant_id)}
                    disabled={v.stock === 0}
                  >
                    <div className="variant-attrs">{slugAttrs(v.attrs)}</div>
                    <div className="variant-price">{fmtARS(v.base_price)}</div>
                    <div className="variant-stock">{v.stock === 0 ? 'sin stock' : `${v.stock} en stock`}</div>
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="price-box">
            <div className="price-box-row">
              <span className="price-box-label">precio unitario</span>
              <span className="price-box-val">{fmtARS(unit)}</span>
            </div>
            {savings > 0 && (
              <div className="price-box-row price-box-savings">
                <span>descuento mayorista</span>
                <span>−{fmtARS(savings)} · cant. ≥ {tier.min_quantity}</span>
              </div>
            )}
            <div className="price-tier-table">
              <div className="price-tier-h">PRECIOS POR CANTIDAD</div>
              {variant.tiers.map((t, i) => (
                <div
                  key={i}
                  className={`price-tier-row ${t.min_quantity === tier.min_quantity ? 'is-active' : ''}`}
                >
                  <span>cant. ≥ {t.min_quantity}</span>
                  <span>{fmtARS(t.unit_price)}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="add-to-cart">
            <div className="qty qty-lg">
              <button onClick={() => setQty(Math.max(1, qty - 1))}>−</button>
              <input
                type="number"
                value={qty}
                min="1"
                max={variant.stock}
                onChange={(e) => setQty(Math.max(1, Math.min(variant.stock, parseInt(e.target.value) || 1)))}
              />
              <button onClick={() => setQty(Math.min(variant.stock, qty + 1))}>+</button>
            </div>
            <Btn
              variant="primary"
              size="lg"
              disabled={variant.stock === 0}
              onClick={handleAdd}
            >
              Agregar al carrito · {fmtARS(unit * qty)}
            </Btn>
          </div>
        </div>
      </div>
    </main>
  );
}

export default Product;
