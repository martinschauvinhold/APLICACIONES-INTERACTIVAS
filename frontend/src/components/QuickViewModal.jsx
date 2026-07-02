import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { useUI } from '../hooks/useUI';
import { useCatalog } from '../hooks/useCatalog';
import ProductImage from './ProductImage';
import Stars from './Stars';
import Btn from './Btn';
import { fmtARS, slugAttrs } from '../utils/format';
import { t } from '../i18n';

function QuickViewModal() {
  const { addToCart } = useCart();
  const { quickViewId: id, closeQuickView, toggleCartDrawer } = useUI();
  const { productById } = useCatalog();
  const { isAuthenticated, role } = useAuth();
  const canBuy = isAuthenticated && role === 'buyer';
  const product = id ? productById(id) : null;
  const navigate = useNavigate();
  const [variantId, setVariantId] = useState(null);
  const [qty, setQty] = useState(1);

  useEffect(() => {
    if (product) setVariantId(product.variants[0].variant_id);
    setQty(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (!id) return;
    const onKey = (e) => { if (e.key === 'Escape') closeQuickView(); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (!product) return null;
  const variant = product.variants.find((v) => v.variant_id === variantId) || product.variants[0];

  const handleAdd = () => {
    addToCart({ variantId: variant.variant_id, quantity: qty });
    closeQuickView();
    toggleCartDrawer();
  };

  const handleDetail = () => {
    closeQuickView();
    navigate(`/producto/${product.product_id}`);
  };

  return (
    <>
      <div className="qv-scrim" onClick={() => closeQuickView()}></div>
      <div className="qv-modal">
        <button className="qv-close mono" onClick={() => closeQuickView()}>{t('product.qv.close')}</button>

        <div className="qv-grid">
          <div className="qv-img">
            <ProductImage src={product.image_url} alt={product.name} ratio="1/1" />
          </div>

          <div className="qv-info">
            <div className="qv-eyebrow mono">{t('product.qv.eyebrow', { id: String(product.product_id).padStart(3, '0') })}</div>
            <div className="qv-brand mono">{product.brand}</div>
            <h2 className="qv-name">{product.name}</h2>
            <div className="qv-rating">
              <Stars rating={product.rating} count={product.reviewCount} />
            </div>
            <p className="qv-desc">{product.description}</p>

            {product.variants.length > 1 && (
              <div className="qv-variants">
                <div className="qv-variants-h mono">{t('product.qv.variant')}</div>
                <div className="qv-variants-list">
                  {product.variants.map((v) => (
                    <button
                      key={v.variant_id}
                      className={`qv-variant mono ${v.variant_id === variantId ? 'is-selected' : ''}`}
                      disabled={v.stock === 0}
                      onClick={() => setVariantId(v.variant_id)}
                    >
                      {slugAttrs(v.attrs)}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="qv-price-row">
              <div>
                <div className="qv-price-label mono">{t('product.qv.priceLabel')}</div>
                <div className="qv-price-val">{fmtARS(variant.base_price)}</div>
              </div>
              <div className="qty qty-lg">
                <button onClick={() => setQty(Math.max(1, qty - 1))}>−</button>
                <span className="mono">{qty}</span>
                <button onClick={() => setQty(Math.min(variant.stock, qty + 1))}>+</button>
              </div>
            </div>

            <div className="qv-actions">
              {canBuy && (
                <Btn variant="primary" size="lg" disabled={variant.stock === 0} onClick={handleAdd}>
                  {t('product.addToCart')} · {fmtARS(variant.base_price * qty)}
                </Btn>
              )}
              {!canBuy && !isAuthenticated && (
                <Btn variant="primary" size="lg" onClick={() => { closeQuickView(); navigate('/login'); }}>
                  {t('product.loginToBuy')}
                </Btn>
              )}
              {!canBuy && isAuthenticated && <div className="seller-note mono">{t('product.sellerNotice')}</div>}
              <Btn variant="ghost" onClick={handleDetail}>{t('product.qv.detail')}</Btn>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default QuickViewModal;
