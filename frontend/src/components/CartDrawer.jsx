import { useNavigate } from 'react-router-dom';
import { useCart } from '../hooks/useCart';
import { useUI } from '../hooks/useUI';
import { useCheckout } from '../hooks/useCheckout';
import ProductImage from './ProductImage';
import Btn from './Btn';
import Empty from './Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { t } from '../i18n';

function CartDrawer() {
  const { lines, totals, setQuantity } = useCart();
  const { cartDrawerOpen: open, closeCartDrawer } = useUI();
  const { setStep } = useCheckout();
  const navigate = useNavigate();

  if (!open) return null;

  const close = () => closeCartDrawer();
  const goToCart = () => { close(); navigate('/carrito'); };
  const goToCheckout = () => { close(); setStep({ step: 0 }); navigate('/checkout'); };

  return (
    <>
      <div className="drawer-scrim" onClick={close}></div>
      <aside className="drawer">
        <header className="drawer-head">
          <span>{t('cart.drawer.head', { n: totals.itemCount })}</span>
          <button className="drawer-close" onClick={close}>×</button>
        </header>

        {lines.length === 0 ? (
          <Empty title={t('cart.drawer.emptyTitle')} hint={t('cart.drawer.emptyHint')} />
        ) : (
          <>
            <div className="drawer-lines">
              {lines.map((l) => (
                <div key={l.variant.variant_id} className="drawer-line">
                  <div className="drawer-line-img">
                    <ProductImage src={l.product.image_url} alt={l.product.name} ratio="1/1" />
                  </div>
                  <div className="drawer-line-info">
                    <div className="drawer-line-brand">{l.product.brand}</div>
                    <div className="drawer-line-name">{l.product.name}</div>
                    <div className="drawer-line-attrs">{slugAttrs(l.variant.attrs)}</div>
                    <div className="drawer-line-foot">
                      <div className="qty">
                        <button onClick={() => setQuantity({ variantId: l.variant.variant_id, quantity: l.quantity - 1 })}>−</button>
                        <span>{l.quantity}</span>
                        <button onClick={() => setQuantity({ variantId: l.variant.variant_id, quantity: l.quantity + 1 })}>+</button>
                      </div>
                      <div className="drawer-line-price">{fmtARS(l.subtotal)}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <footer className="drawer-foot">
              <div className="drawer-totals">
                <div className="drawer-total-row"><span>{t('cart.subtotal')}</span><span>{fmtARS(totals.subtotal)}</span></div>
                <div className="drawer-total-row"><span>{t('cart.shipping')}</span><span>{fmtARS(totals.shipping)}</span></div>
                <div className="drawer-total-row drawer-total-grand"><span>{t('cart.drawer.total')}</span><span>{fmtARS(totals.total)}</span></div>
              </div>
              <div className="drawer-actions">
                <Btn variant="ghost" onClick={goToCart}>{t('cart.drawer.viewCart')}</Btn>
                <Btn variant="primary" onClick={goToCheckout}>{t('cart.drawer.goCheckout')}</Btn>
              </div>
            </footer>
          </>
        )}
      </aside>
    </>
  );
}

export default CartDrawer;
