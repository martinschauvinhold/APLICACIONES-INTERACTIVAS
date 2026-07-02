import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { useAuth } from '../hooks/useAuth';
import {
  fetchOrdersByUser,
  fetchOrderDetail,
  cancelOrder,
  selectOrderById,
  selectOrderDetail,
  selectOrdersLoading,
  selectCancelling,
} from '../features/orders/ordersSlice';
import Btn from '../components/Btn';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { t, orderStatus } from '../i18n';

// Estados que admiten cancelación (antes de despachar). El back valida igual.
const CANCELLABLE = new Set(['pending', 'paid', 'preparing', 'processing']);

// Número de seguimiento sintético: el back no lo expone en la orden.
const trackingNumber = (orderId) => 'CA-' + String(orderId).padStart(8, '0');

// Reconstruye una línea de tiempo a partir del estado de la orden (el back no
// guarda checkpoints en la orden; el detalle de envío vive en deliveries).
function buildTracking(order) {
  const steps = [{ checkpoint: t('confirmation.trackReceived'), status: 'pending', recorded_at: order.created_at }];
  if (['paid', 'preparing', 'shipped', 'out_for_delivery', 'delivered'].includes(order.status)) {
    steps.push({ checkpoint: t('confirmation.trackPaid'), status: 'paid', recorded_at: order.created_at });
  }
  if (order.status === 'cancelled') {
    steps.push({ checkpoint: t('confirmation.trackCancelled'), status: 'cancelled', recorded_at: order.created_at });
  }
  return steps;
}

function TrackingTimeline({ tracking, showPending }) {
  return (
    <ol className="tracking">
      {tracking.map((step, i) => {
        const isLast = i === tracking.length - 1;
        return (
          <li key={i} className={`tracking-step ${isLast && !showPending ? 'is-current' : 'is-done'}`}>
            <div className="tracking-dot"></div>
            <div>
              <div className="tracking-label">{step.checkpoint}</div>
              <div className="tracking-meta">
                {new Date(step.recorded_at).toLocaleString('es-AR', { hour12: false })} · {orderStatus(step.status)}
              </div>
            </div>
          </li>
        );
      })}
      {showPending && (
        <li className="tracking-step is-pending">
          <div className="tracking-dot"></div>
          <div>
            <div className="tracking-label">{t('confirmation.deliveredLabel')}</div>
            <div className="tracking-meta">{t('confirmation.deliveredPending')}</div>
          </div>
        </li>
      )}
    </ol>
  );
}

/**
 * Confirmation — recibo del pedido. Lee TODO de Redux (misma fuente que
 * Orders): la cabecera del pedido del listado (fetchOrdersByUser) y el detalle
 * de items/totales (fetchOrderDetail). Así, cancelar acá se refleja sin recargar.
 */
function Confirmation() {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user } = useAuth();
  const orderId = Number(id);

  const order = useSelector(selectOrderById(orderId));
  const detail = useSelector(selectOrderDetail(orderId));
  const ordersLoading = useSelector(selectOrdersLoading);
  const cancelling = useSelector(selectCancelling);
  const userEmail = user.email;

  useEffect(() => {
    if (user.user_id) dispatch(fetchOrdersByUser(user.user_id));
    dispatch(fetchOrderDetail(orderId));
  }, [dispatch, user.user_id, orderId]);

  const handleCancel = async () => {
    if (!window.confirm(t('confirmation.cancelConfirm'))) return;
    // Cancela contra el back; al leer de Redux, la vista refleja el nuevo estado.
    await dispatch(cancelOrder(orderId)).unwrap().catch(() => {});
  };

  // order puede venir de un fetchAllOrders anterior (p. ej. si el usuario pasó
  // por /admin), que carga pedidos de TODOS los usuarios en el mismo state.orders.list
  // — sin este chequeo, cualquiera podría ver el detalle de un pedido ajeno
  // navegando directo a /confirmacion/<id>.
  const isOwnOrder = order && order.user_id === user.user_id;

  if (!isOwnOrder) {
    if (ordersLoading) {
      return (
        <main className="screen">
          <Empty title={t('confirmation.loading')} />
        </main>
      );
    }
    return (
      <main className="screen">
        <Empty
          title={t('confirmation.notFound')}
          action={<Btn variant="primary" onClick={() => navigate('/')}>{t('confirmation.backToCatalog')}</Btn>}
        />
      </main>
    );
  }

  const items = detail?.items || [];
  const totals = detail?.totals;
  const showPending = !['delivered', 'cancelled'].includes(order.status);

  return (
    <main className="screen">
      <div className="confirm-hero">
        <div className="confirm-mark">✓</div>
        <div className="eyebrow mono">{t('confirmation.eyebrow')}</div>
        <h1 className="confirm-title">{t('confirmation.title')}</h1>
        <p className="confirm-sub">
          {t('confirmation.emailSent', { email: userEmail })}
        </p>
        <div className="confirm-id">
          {t('confirmation.idLine', { id: order.order_id, code: trackingNumber(order.order_id) })}
        </div>
      </div>

      <div className="confirm-grid">
        <div className="confirm-block">
          <div className="eyebrow mono">{t('confirmation.trackingEyebrow')}</div>
          <h2 className="confirm-block-title">{t('confirmation.trackingTitle')}</h2>
          <TrackingTimeline tracking={buildTracking(order)} showPending={showPending} />
        </div>

        <div className="confirm-block">
          <div className="eyebrow mono">{t('confirmation.summaryEyebrow')}</div>
          <h2 className="confirm-block-title">{t('confirmation.summaryTitle')}</h2>
          <div className="confirm-items">
            {items.map((it, i) => (
              <div key={i} className="confirm-item">
                <div className="mono">{it.quantity}×</div>
                <div>
                  <div>{it.product_name}</div>
                  <div className="mono confirm-item-attrs">{slugAttrs(it.attrs)}</div>
                </div>
                <div className="mono">{fmtARS(it.subtotal)}</div>
              </div>
            ))}
          </div>
          {totals && (
            <div className="totals totals-compact">
              <div className="total-row"><span>{t('cart.subtotal')}</span><span>{fmtARS(totals.subtotal)}</span></div>
              {totals.discount > 0 && (
                <div className="total-row total-row-savings"><span>{t('confirmation.coupon')}</span><span>−{fmtARS(totals.discount)}</span></div>
              )}
              <div className="total-row"><span>{t('cart.shipping')}</span><span>{totals.shipping === 0 ? t('cart.free') : fmtARS(totals.shipping)}</span></div>
              <div className="total-row total-grand"><span>{t('confirmation.totalPaid')}</span><span>{fmtARS(order.total_amount || totals.total)}</span></div>
            </div>
          )}
          {order.address && (
            <div className="mono confirm-address">
              <div className="eyebrow">{t('confirmation.address')}</div>
              <div>{order.address.street}, {order.address.city}, {order.address.state} {order.address.zip_code}</div>
            </div>
          )}
        </div>
      </div>

      <div className="confirm-foot">
        <Btn variant="ghost" onClick={() => navigate('/')}>{t('confirmation.keepShopping')}</Btn>
        {CANCELLABLE.has(order.status) && (
          <Btn variant="ghost" disabled={cancelling} onClick={handleCancel}>
            {cancelling ? t('confirmation.cancelling') : t('confirmation.cancel')}
          </Btn>
        )}
        <Btn variant="primary" onClick={() => navigate('/pedidos')}>{t('confirmation.seeAllOrders')}</Btn>
      </div>
    </main>
  );
}

export default Confirmation;
