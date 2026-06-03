import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import ProductImage from '../components/ProductImage';
import Btn from '../components/Btn';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { selectCartLines, selectCartTotals } from '../store/selectors';
import { setQuantity, remove, applyCoupon, removeCoupon } from '../store/slices/cartSlice';
import { setStep } from '../store/slices/checkoutSlice';

/**
 * Cart — carrito de compras con cupón y resumen.
 *
 * Hooks:
 *   - useSelector: cartLines + totales + coupon
 *   - useDispatch: setQuantity, remove, applyCoupon, removeCoupon, setStep
 *   - useState: estado local del input de cupón
 *   - useNavigate: para ir al checkout
 */
function Cart() {
  const lines = useSelector(selectCartLines);
  const totals = useSelector(selectCartTotals);
  const coupon = useSelector((s) => s.cart.coupon);
  const couponCode = useSelector((s) => s.cart.couponCode);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [couponInput, setCouponInput] = useState('');

  const goToCheckout = () => {
    dispatch(setStep({ step: 0 }));
    navigate('/checkout');
  };

  if (lines.length === 0) {
    return (
      <main className="screen">
        <div className="eyebrow mono">CARRITO</div>
        <h1 className="screen-title">Tu carrito</h1>
        <Empty
          title="No hay productos en tu carrito"
          hint="Explorá el catálogo y agregá lo que necesites."
          action={<Btn variant="primary" onClick={() => navigate('/')}>Ir al catálogo</Btn>}
        />
      </main>
    );
  }

  const handleSubmitCoupon = (e) => {
    e.preventDefault();
    dispatch(applyCoupon({ code: couponInput }));
    setCouponInput('');
  };

  return (
    <main className="screen">
      <div className="eyebrow mono">CARRITO</div>
      <h1 className="screen-title">
        Tu carrito <span className="screen-title-meta mono">/ {totals.itemCount} items</span>
      </h1>

      <div className="cart-grid">
        <div className="cart-lines">
          <div className="cart-lines-head">
            <span>Producto</span>
            <span>Cantidad</span>
            <span>Precio</span>
            <span>Subtotal</span>
            <span></span>
          </div>
          {lines.map((l) => (
            <div key={l.variant.variant_id} className="cart-line">
              <div className="cart-line-product">
                <div className="cart-line-img">
                  <ProductImage src={l.product.image_url} alt={l.product.name} ratio="1/1" />
                </div>
                <div>
                  <div className="cart-line-brand">{l.product.brand}</div>
                  <div className="cart-line-name">{l.product.name}</div>
                  <div className="cart-line-attrs">{slugAttrs(l.variant.attrs)} · REF {l.variant.sku}</div>
                </div>
              </div>
              <div>
                <div className="qty">
                  <button onClick={() => dispatch(setQuantity({ variantId: l.variant.variant_id, quantity: l.quantity - 1 }))}>−</button>
                  <span>{l.quantity}</span>
                  <button onClick={() => dispatch(setQuantity({ variantId: l.variant.variant_id, quantity: l.quantity + 1 }))}>+</button>
                </div>
                {l.tierSavings > 0 && (
                  <div className="cart-tier-tag">descuento por volumen</div>
                )}
              </div>
              <div className="cart-cell-price">
                {l.unit_price < l.variant.base_price && (
                  <span className="cart-strike mono">{fmtARS(l.variant.base_price)}</span>
                )}
                {fmtARS(l.unit_price)}
              </div>
              <div className="cart-cell-subtotal">{fmtARS(l.subtotal)}</div>
              <button
                className="cart-line-remove"
                title="Quitar"
                onClick={() => dispatch(remove({ variantId: l.variant.variant_id }))}
              >×</button>
            </div>
          ))}
        </div>

        <aside className="cart-summary">
          <div className="eyebrow mono">RESUMEN</div>
          <h2 className="cart-summary-title">Pedido</h2>

          <div className="coupon-box">
            <div className="coupon-label">CUPÓN</div>
            {coupon ? (
              <div className="coupon-applied">
                <div>
                  <div className="coupon-code">{coupon.code}</div>
                  <div className="coupon-name">{coupon.name}</div>
                </div>
                <button className="coupon-remove" onClick={() => dispatch(removeCoupon())}>quitar</button>
              </div>
            ) : (
              <form className="coupon-input" onSubmit={handleSubmitCoupon}>
                <input
                  type="text"
                  placeholder="TECH10, BIENVENIDA, ENVIOGRATIS…"
                  value={couponInput}
                  onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
                />
                <button type="submit">aplicar</button>
                {couponCode && !coupon && (
                  <div className="coupon-error">⚠ código inválido: {couponCode}</div>
                )}
              </form>
            )}
          </div>

          <div className="totals">
            <div className="total-row"><span>subtotal</span><span>{fmtARS(totals.subtotal)}</span></div>
            {totals.tierSavings > 0 && (
              <div className="total-row total-row-savings">
                <span>ahorro mayorista</span>
                <span>−{fmtARS(totals.tierSavings)}</span>
              </div>
            )}
            {totals.discount > 0 && (
              <div className="total-row total-row-savings">
                <span>cupón {coupon.code}</span>
                <span>−{fmtARS(totals.discount)}</span>
              </div>
            )}
            <div className="total-row">
              <span>envío</span>
              <span>{totals.shipping === 0 ? 'GRATIS' : fmtARS(totals.shipping)}</span>
            </div>
            <div className="total-row total-grand">
              <span>TOTAL</span>
              <span>{fmtARS(totals.total)}</span>
            </div>
          </div>

          <Btn variant="primary" size="lg" className="w-full" onClick={goToCheckout}>
            Continuar al checkout →
          </Btn>
        </aside>
      </div>
    </main>
  );
}

export default Cart;
