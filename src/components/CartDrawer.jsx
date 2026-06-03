import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import ProductImage from './ProductImage';
import Btn from './Btn';
import Empty from './Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { selectCartLines, selectCartTotals } from '../store/selectors';
import { setQuantity, clear as clearCart } from '../store/slices/cartSlice';
import { closeCartDrawer } from '../store/slices/uiSlice';
import { setStep } from '../store/slices/checkoutSlice';

/**
 * CartDrawer — drawer lateral del carrito.
 * Usa useSelector para leer cart + totals.
 */
function CartDrawer() {
  const open = useSelector((s) => s.ui.cartDrawerOpen);
  const lines = useSelector(selectCartLines);
  const totals = useSelector(selectCartTotals);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  if (!open) return null;

  const close = () => dispatch(closeCartDrawer());
  const goToCart = () => { close(); navigate('/carrito'); };
  const goToCheckout = () => { close(); dispatch(setStep({ step: 0 })); navigate('/checkout'); };

  return (
    <>
      <div className="drawer-scrim" onClick={close}></div>
      <aside className="drawer">
        <header className="drawer-head">
          <span>CARRITO · {totals.itemCount} items</span>
          <button className="drawer-close" onClick={close}>×</button>
        </header>

        {lines.length === 0 ? (
          <Empty title="Carrito vacío" hint="Agregá productos del catálogo para empezar." />
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
                        <button onClick={() => dispatch(setQuantity({ variantId: l.variant.variant_id, quantity: l.quantity - 1 }))}>−</button>
                        <span>{l.quantity}</span>
                        <button onClick={() => dispatch(setQuantity({ variantId: l.variant.variant_id, quantity: l.quantity + 1 }))}>+</button>
                      </div>
                      <div className="drawer-line-price">{fmtARS(l.subtotal)}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <footer className="drawer-foot">
              <div className="drawer-totals">
                <div className="drawer-total-row"><span>subtotal</span><span>{fmtARS(totals.subtotal)}</span></div>
                <div className="drawer-total-row"><span>envío</span><span>{fmtARS(totals.shipping)}</span></div>
                <div className="drawer-total-row drawer-total-grand"><span>total</span><span>{fmtARS(totals.total)}</span></div>
              </div>
              <div className="drawer-actions">
                <Btn variant="ghost" onClick={goToCart}>Ver carrito</Btn>
                <Btn variant="primary" onClick={goToCheckout}>Ir al checkout →</Btn>
              </div>
            </footer>
          </>
        )}
      </aside>
    </>
  );
}

export default CartDrawer;
