import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProductById, selectProductDetail, selectDetailLoading } from '../features/products/productsSlice';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { useUI } from '../hooks/useUI';
import { useCatalog } from '../hooks/useCatalog';
import ProductImage from '../components/ProductImage';
import Stars from '../components/Stars';
import Tag from '../components/Tag';
import Btn from '../components/Btn';
import SpecSheet from '../components/SpecSheet';
import RecentlyViewed from '../components/RecentlyViewed';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { t } from '../i18n';

/**
 * Product — detalle del producto.
 *
 * Hooks:
 *   - useParams: extrae el id del producto de la URL
 *   - useNavigate: para volver al catálogo
 *   - useApp: trae el producto del estado global
 *   - useState: estado local de variante seleccionada y cantidad
 */
function Product() {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { addToCart } = useCart();
  const { toggleCartDrawer, trackView } = useUI();
  const { productById, sellerById } = useCatalog();
  const { isAuthenticated, role } = useAuth();

  const detail = useSelector(selectProductDetail(Number(id)));
  const detailLoading = useSelector(selectDetailLoading);
  const product = detail || productById(id);
  const canBuy = isAuthenticated && role === 'buyer';
  const seller = product && sellerById(product.seller_id);

  const [variantId, setVariantId] = useState(product?.variants[0]?.variant_id);
  const [qty, setQty] = useState(1);

  useEffect(() => {
    dispatch(fetchProductById(id));
  }, [id, dispatch]);

  useEffect(() => {
    if (product) trackView({ productId: product.product_id });
    // Solo al cambiar el producto de la URL.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (!product && detailLoading) {
    return (
      <main className="screen">
        <Empty title="Cargando producto…" />
      </main>
    );
  }

  if (!product) {
    return (
      <main className="screen">
        <Empty
          title={t('product.notFoundTitle')}
          hint={t('product.notFoundHint')}
          action={<Btn variant="primary" onClick={() => navigate('/')}>{t('product.backToCatalog')}</Btn>}
        />
      </main>
    );
  }

  const variant = product.variants.find((v) => v.variant_id === variantId) || product.variants[0];

  const tier = [...variant.tiers].reverse().find((t) => qty >= t.min_quantity) || variant.tiers[0];
  const unit = tier.unit_price;
  const savings = variant.base_price - unit;

  const handleAdd = () => {
    addToCart({ variantId: variant.variant_id, quantity: qty });
    toggleCartDrawer();
  };

  return (
    <main className="screen">
      <nav className="crumbs">
        <Link to="/">{t('product.crumbCatalog')}</Link>
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
          <div className="eyebrow mono">{t('product.eyebrow')}</div>
          <div className="product-brand">{product.brand}</div>
          <h1 className="product-name">{product.name}</h1>
          <div className="product-meta">
            <Stars rating={product.rating} count={product.reviewCount} />
          </div>
          {seller && <div className="product-seller mono">{t('product.soldBy', { seller: seller.first_name })}</div>}

          <div className="product-tags">
            {product.tags.map((t) => <Tag key={t}>{t}</Tag>)}
          </div>

          <p className="product-desc">{product.description}</p>

          <SpecSheet product={product} variant={variant} />

          {product.variants.length > 1 && (
            <div className="variants">
              <div className="variants-label">{t('product.variants')}</div>
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
                    <div className="variant-stock">{v.stock === 0 ? t('product.outOfStock') : t('product.inStock', { n: v.stock })}</div>
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="price-box">
            <div className="price-box-row">
              <span className="price-box-label">{t('product.unitPrice')}</span>
              <span className="price-box-val">{fmtARS(unit)}</span>
            </div>
            {savings > 0 && (
              <div className="price-box-row price-box-savings">
                <span>{t('product.wholesaleDiscount')}</span>
                <span>{t('product.savingsQty', { amount: fmtARS(savings), min: tier.min_quantity })}</span>
              </div>
            )}
            <div className="price-tier-table">
              <div className="price-tier-h">{t('product.tiersTitle')}</div>
              {variant.tiers.map((row, i) => (
                <div
                  key={i}
                  className={`price-tier-row ${row.min_quantity === tier.min_quantity ? 'is-active' : ''}`}
                >
                  <span>{t('product.qtyAtLeast', { n: row.min_quantity })}</span>
                  <span>{fmtARS(row.unit_price)}</span>
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
            {canBuy ? (
              <Btn
                variant="primary"
                size="lg"
                disabled={variant.stock === 0}
                onClick={handleAdd}
              >
                {t('product.addToCart')} · {fmtARS(unit * qty)}
              </Btn>
            ) : !isAuthenticated ? (
              <Btn variant="primary" size="lg" onClick={() => navigate('/login')}>
                {t('product.loginToBuy')}
              </Btn>
            ) : (
              <div className="seller-note mono">{t('product.sellerNotice')}</div>
            )}
          </div>

          <div className="trust mono">
            <div className="trust-item">▸ {t('product.trust1')}</div>
            <div className="trust-item">▸ {t('product.trust2')}</div>
            <div className="trust-item">▸ {t('product.trust3')}</div>
          </div>
        </div>
      </div>

      <RecentlyViewed currentId={product.product_id} />
    </main>
  );
}

export default Product;
