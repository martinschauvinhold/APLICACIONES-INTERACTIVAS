import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { TriangleAlert, X } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import {
  fetchOrdersByUser,
  cancelOrder,
  selectOrders,
  selectOrdersLoading,
  selectOrdersError,
  selectCancelling,
  selectCancelError,
} from '../features/orders/ordersSlice';
import Btn from '../components/Btn';
import Empty from '../components/Empty';
import { fmtARS } from '../utils/format';
import { t, orderStatus } from '../i18n';

const CANCELLABLE = new Set(['pending', 'paid', 'preparing', 'processing']);

function Orders() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user } = useAuth();

  const orders = useSelector(selectOrders);
  const loading = useSelector(selectOrdersLoading);
  const error = useSelector(selectOrdersError);
  const cancelling = useSelector(selectCancelling);
  const cancelError = useSelector(selectCancelError);

  useEffect(() => {
    if (user.user_id) dispatch(fetchOrdersByUser(user.user_id));
  }, [dispatch, user.user_id]);

  const handleCancel = (orderId) => {
    if (!window.confirm(t('orders.cancelConfirm', { id: orderId }))) return;
    dispatch(cancelOrder(orderId));
  };

  if (loading && orders.length === 0) {
    return (
      <main className="screen">
        <div className="eyebrow mono">{t('orders.eyebrow')}</div>
        <h1 className="screen-title">{t('orders.title')}</h1>
        <div className="ticket-hint mono">{t('orders.loading')}</div>
      </main>
    );
  }

  if (orders.length === 0) {
    return (
      <main className="screen">
        <div className="eyebrow mono">{t('orders.eyebrow')}</div>
        <h1 className="screen-title">{t('orders.title')}</h1>
        {error && <span className="field-error"><TriangleAlert size={14} /> {error}</span>}
        <Empty
          title={t('orders.emptyTitle')}
          hint={t('orders.emptyHint')}
          action={<Btn variant="primary" onClick={() => navigate('/')}>{t('orders.goCatalog')}</Btn>}
        />
      </main>
    );
  }

  return (
    <main className="screen">
      <div className="eyebrow mono">{t('orders.eyebrow')}</div>
      <h1 className="screen-title">
        {t('orders.title')}{' '}
        <span className="screen-title-meta mono">{t('orders.countMeta', { n: orders.length })}</span>
      </h1>

      {(error || cancelError) && (
        <span className="field-error"><TriangleAlert size={14} /> {cancelError || error}</span>
      )}

      <div className="orders-list">
        {orders.map((o) => (
          <article key={o.order_id} className="order-row">
            <div className="order-row-head">
              <div className="order-row-id">#{o.order_id}</div>
              <div className={`order-status order-status-${o.status}`}>{orderStatus(o.status)}</div>
              <div className="order-row-date">{new Date(o.created_at).toLocaleDateString('es-AR')}</div>
              <div className="order-row-total">{fmtARS(o.total_amount)}</div>
            </div>
            {CANCELLABLE.has(o.status) && (
              <div className="order-row-actions">
                <Btn variant="ghost" size="sm" disabled={cancelling} onClick={() => handleCancel(o.order_id)}>
                  <X size={14} /> {cancelling ? t('orders.cancelling') : t('orders.cancel')}
                </Btn>
              </div>
            )}
          </article>
        ))}
      </div>
    </main>
  );
}

export default Orders;
