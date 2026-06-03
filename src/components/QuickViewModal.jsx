import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import ProductImage from './ProductImage';
import Stars from './Stars';
import Btn from './Btn';
import { fmtARS, slugAttrs } from '../utils/format';
import { selectProductById } from '../store/selectors';
import { add } from '../store/slices/cartSlice';
import { closeQuickView, toggleCartDrawer } from '../store/slices/uiSlice';

/**
 * QuickViewModal — preview rápido del producto sin cambiar de ruta.
 * Se cierra con Esc o tocando fuera del modal.
 */
function QuickViewModal() {
  const id = useSelector((s) => s.ui.quickViewId);
  const product = useSelector(id ? selectProductById(id) : () => null);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [variantId, setVariantId] = useState(null);
  const [qty, setQty] = useState(1);

  // Reset al cambiar de producto
  useEffect(() => {
    if (product) setVariantId(product.variants[0].variant_id);
    setQty(1);
  }, [id]);

  // Esc para cerrar
  useEffect(() => {
    if (!id) return;
    const onKey = (e) => { if (e.key === 'Escape') dispatch(closeQuickView()); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [id]);

  if (!product) return null;
  const variant = product.variants.find((v) => v.variant_id === variantId) || product.variants[0];

  const handleAdd = () => {
    dispatch(add({ variantId: variant.variant_id, quantity: qty }));
    dispatch(closeQuickView());
    dispatch(toggleCartDrawer());
  };

  const handleDetail = () => {
    dispatch(closeQuickView());
    navigate(`/producto/${product.product_id}`);
  };

  return (
    <>
      <div
        onClick={() => dispatch(closeQuickView())}
        style={{
          position: 'fixed', inset: 0, zIndex: 90,
          background: 'color-mix(in oklch, var(--fg) 50%, transparent)',
          backdropFilter: 'blur(4px)',
        }}
      />
      <div
        style={{
          position: 'fixed', top: '50%', left: '50%',
          transform: 'translate(-50%, -50%)',
          zIndex: 91,
          width: 'min(900px, 92vw)',
          maxHeight: '90vh',
          overflow: 'auto',
          background: 'var(--bg)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius)',
          boxShadow: '0 32px 96px -16px rgba(0,0,0,.35)',
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
        }}
      >
        <button
          onClick={() => dispatch(closeQuickView())}
          style={{
            position: 'absolute', top: 14, right: 14, zIndex: 2,
            padding: '6px 12px',
            background: 'var(--bg-2)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius)',
            color: 'var(--fg-muted)',
            fontSize: 10,
            fontFamily: 'var(--font-mono)',
            letterSpacing: '0.08em',
          }}
        >× CERRAR · ESC</button>

        <div style={{ background: 'var(--bg-2)', borderRight: '1px solid var(--border)' }}>
          <ProductImage src={product.image_url} alt={product.name} ratio="1/1" />
        </div>

        <div style={{ padding: 'var(--pad-lg)', display: 'flex', flexDirection: 'column', gap: 12 }}>
          <div className="eyebrow mono">QUICK VIEW</div>
          <div className="product-brand">{product.brand}</div>
          <h2 className="product-name" style={{ fontSize: 32 }}>{product.name}</h2>
          <Stars rating={product.rating} count={product.reviewCount} />
          <p style={{ color: 'var(--fg-2)', fontSize: 13, lineHeight: 1.6, paddingBottom: 12, borderBottom: '1px solid var(--border)' }}>
            {product.description}
          </p>

          {product.variants.length > 1 && (
            <div>
              <div className="variants-label" style={{ marginBottom: 6 }}>VARIANTE</div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                {product.variants.map((v) => (
                  <button
                    key={v.variant_id}
                    disabled={v.stock === 0}
                    onClick={() => setVariantId(v.variant_id)}
                    className={`mono ${v.variant_id === variantId ? 'is-selected' : ''}`}
                    style={{
                      padding: '6px 12px',
                      border: `1px solid ${v.variant_id === variantId ? 'var(--accent)' : 'var(--border)'}`,
                      borderRadius: 'var(--radius)',
                      fontSize: 11,
                      background: v.variant_id === variantId ? 'color-mix(in oklch, var(--accent) 8%, var(--bg))' : 'var(--bg)',
                      opacity: v.stock === 0 ? 0.4 : 1,
                    }}
                  >
                    {slugAttrs(v.attrs)}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderTop: '1px solid var(--border)', borderBottom: '1px solid var(--border)' }}>
            <div>
              <div className="price-box-label">PRECIO UNITARIO</div>
              <div className="price-box-val" style={{ fontFamily: 'var(--font-display)', fontSize: 32 }}>
                {fmtARS(variant.base_price)}
              </div>
            </div>
            <div className="qty qty-lg">
              <button onClick={() => setQty(Math.max(1, qty - 1))}>−</button>
              <span>{qty}</span>
              <button onClick={() => setQty(Math.min(variant.stock, qty + 1))}>+</button>
            </div>
          </div>

          <Btn variant="primary" size="lg" className="w-full" disabled={variant.stock === 0} onClick={handleAdd}>
            Agregar al carrito · {fmtARS(variant.base_price * qty)}
          </Btn>
          <Btn variant="ghost" onClick={handleDetail}>Ver detalle completo →</Btn>
        </div>
      </div>
    </>
  );
}

export default QuickViewModal;
