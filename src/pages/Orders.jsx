import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import ProductImage from '../components/ProductImage';
import Btn from '../components/Btn';
import Empty from '../components/Empty';
import { fmtARS } from '../utils/format';
import { hydrate as hydrateOrders } from '../store/slices/ordersSlice';
import { api } from '../api/client';

/**
 * Orders — listado de pedidos del usuario.
 *
 * Hooks:
 *   - useSelector: lista de pedidos
 *   - useDispatch: hidratar desde la API
 *   - useEffect: trae los pedidos al montar
 *   - useNavigate: navega al detalle de cada pedido
 */
function Orders() {
  const orders = useSelector((s) => s.orders.list);
  const userId = useSelector((s) => s.session.user.user_id);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  // Hidratar pedidos del backend
  useEffect(() => {
    if (window.__ordersHydrated) return;
    window.__ordersHydrated = true;
    api.orders.forUser(userId).then((list) => {
      if (Array.isArray(list) && list.length > 0) {
        dispatch(hydrateOrders({ list }));
      }
    }).catch(() => { /* modo demo */ });
  }, [dispatch, userId]);

  if (orders.length === 0) {
    return (
      <main className="screen">
        <div className="eyebrow mono">MIS PEDIDOS</div>
        <h1 className="screen-title">Mis pedidos</h1>
        <Empty
          title="Todavía no hiciste pedidos"
          hint="Cuando completes una compra vas a verla acá."
          action={<Btn variant="primary" onClick={() => navigate('/')}>Ir al catálogo</Btn>}
        />
      </main>
    );
  }

  return (
    <main className="screen">
      <div className="eyebrow mono">MIS PEDIDOS</div>
      <h1 className="screen-title">
        Mis pedidos <span className="screen-title-meta mono">/ {orders.length}</span>
      </h1>

      <div className="orders-list">
        {orders.map((o) => (
          <article
            key={o.order_id}
            className="order-row"
            onClick={() => navigate(`/confirmacion/${o.order_id}`)}
          >
            <div className="order-row-head">
              <div className="order-row-id">#{o.order_id}</div>
              <div className={`order-status order-status-${o.status}`}>{o.status}</div>
              <div className="order-row-date">{new Date(o.created_at).toLocaleDateString('es-AR')}</div>
              <div className="order-row-total">{fmtARS(o.totals.total)}</div>
            </div>
            <div className="order-row-items">
              {o.items.slice(0, 3).map((it, i) => (
                <div key={i} className="order-row-thumb">
                  <ProductImage src={it.image_url} alt={it.product_name} ratio="1/1" />
                </div>
              ))}
              <div className="order-row-summary">
                <div>
                  {o.items.length} {o.items.length === 1 ? 'producto' : 'productos'} ·{' '}
                  {o.items.reduce((a, i) => a + i.quantity, 0)} unidades
                </div>
                <div className="order-row-track">Seguimiento · {o.tracking_number}</div>
              </div>
              <div className="order-row-arrow">→</div>
            </div>
          </article>
        ))}
      </div>
    </main>
  );
}

export default Orders;
