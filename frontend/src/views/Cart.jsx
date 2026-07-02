import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TriangleAlert } from 'lucide-react';
import { useCart } from '../hooks/useCart';
import { useCheckout } from '../hooks/useCheckout';
import ProductImage from '../components/ProductImage';
import Btn from '../components/Btn';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { t } from '../i18n';

function Cart() {
  const { lines, totals, coupon, couponCode, setQuantity, removeFromCart, applyCoupon, removeCoupon } = useCart();
  const { setStep } = useCheckout();
  const navigate = useNavigate();

  const [couponInput, setCouponInput] = useState('');

  const goToCheckout = () => {
    setStep({ step: 0 });
    navigate('/checkout');
  };

  if (lines.length === 0) {
    return (
      <main className="screen">
        <div className="eyebrow mono">{t('cart.eyebrow')}</div>
        <h1 className="screen-title">{t('cart.title')}</h1>
        <Empty
          title={t('cart.emptyTitle')}
          hint={t('cart.emptyHint')}
          action={<Btn variant="primary" onClick={() => navigate('/')}>{t('cart.goCatalog')}</Btn>}
        />
      </main>
    );
  }

  const handleSubmitCoupon = (e) => {
    e.preventDefault();
    applyCoupon({ code: couponInput });
    setCouponInput('');
  };

  return (
    <main className="screen">
      <div className="eyebrow mono">{t('cart.eyebrow')}</div>
      <h1 className="screen-title">
        {t('cart.title')}{' '}
        <span className="screen-title-meta mono">{t('cart.itemsMeta', { n: totals.itemCount })}</span>
      </h1>

      <div className="cart-grid">
        <div className="cart-lines">
          <div className="cart-lines-head">
            <span>{t('cart.colProduct')}</span>
            <span>{t('cart.colQty')}</span>
            <span>{t('cart.colUnit')}</span>
            <span>{t('cart.colSubtotal')}</span>
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
                  <div className="cart-line-attrs">{slugAttrs(l.variant.attrs)}</div>
                </div>
              </div>
              <div>
                <div className="qty">
                  <button onClick={() => setQuantity({ variantId: l.variant.variant_id, quantity: l.quantity - 1 })}>−</button>
                  <span>{l.quantity}</span>
                  <button onClick={() => setQuantity({ variantId: l.variant.variant_id, quantity: l.quantity + 1 })}>+</button>
                </div>
                {l.tierSavings > 0 && (
                  <div className="cart-tier-tag">{t('cart.volumeDiscount')}</div>
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
                title={t('cart.remove')}
                onClick={() => removeFromCart({ variantId: l.variant.variant_id })}
              >×</button>
            </div>
          ))}
        </div>

        <aside className="cart-summary">
          <div className="eyebrow mono">{t('cart.summary')}</div>
          <h2 className="cart-summary-title">{t('cart.summaryTitle')}</h2>

          <div className="coupon-box">
            <div className="coupon-label">{t('cart.coupon')}</div>
            {coupon ? (
              <div className="coupon-applied">
                <div>
                  <div className="coupon-code">{coupon.code}</div>
                  <div className="coupon-name">{coupon.name}</div>
                </div>
                <button className="coupon-remove" onClick={() => removeCoupon()}>{t('cart.remove')}</button>
              </div>
            ) : (
              <form className="coupon-input" onSubmit={handleSubmitCoupon}>
                <input
                  type="text"
                  placeholder={t('cart.couponPlaceholder')}
                  value={couponInput}
                  onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
                />
                <button type="submit">{t('cart.apply')}</button>
                {couponCode && !coupon && (
                  <div className="coupon-error">
                    <TriangleAlert size={13} /> {t('cart.couponInvalid', { code: couponCode })}
                  </div>
                )}
              </form>
            )}
          </div>

          <div className="totals">
            <div className="total-row"><span>{t('cart.subtotal')}</span><span>{fmtARS(totals.subtotal)}</span></div>
            {totals.tierSavings > 0 && (
              <div className="total-row total-row-savings">
                <span>{t('cart.wholesaleSaving')}</span>
                <span>−{fmtARS(totals.tierSavings)}</span>
              </div>
            )}
            {totals.discount > 0 && (
              <div className="total-row total-row-savings">
                <span>{t('cart.couponLine', { code: coupon.code })}</span>
                <span>−{fmtARS(totals.discount)}</span>
              </div>
            )}
            <div className="total-row">
              <span>{t('cart.shipping')}</span>
              <span>{totals.shipping === 0 ? t('cart.free') : fmtARS(totals.shipping)}</span>
            </div>
            <div className="total-row total-grand">
              <span>{t('cart.total')}</span>
              <span>{fmtARS(totals.total)}</span>
            </div>
          </div>

          <Btn variant="primary" size="lg" className="w-full" onClick={goToCheckout}>
            {t('cart.checkout')}
          </Btn>
        </aside>
      </div>
    </main>
  );
}

export default Cart;
