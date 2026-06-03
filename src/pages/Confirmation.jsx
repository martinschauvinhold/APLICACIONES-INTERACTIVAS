import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import Btn from '../components/Btn';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { advanceTracking } from '../store/slices/ordersSlice';

function TrackingTimeline({ tracking }) {
  return (
    <ol className="tracking">
      {tracking.map((t, i) => {
        const isLast = i === tracking.length - 1;
        return (
          <li key={i} className={`tracking-step ${isLast ? 'is-current' : 'is-done'}`}>
            <div className="tracking-dot"></div>
            <div>
              <div className="tracking-label">{t.checkpoint}</div>
              <div className="tracking-meta">
                {new Date(t.recorded_at).toLocaleString('es-AR', { hour12: false })} · {t.status}
              </div>
            </div>
          </li>
        );
      })}
      <li className="tracking-step is-pending">
        <div className="tracking-dot"></div>
        <div>
          <div className="tracking-label">Entregado</div>
          <div className="tracking-meta">pendiente · estimado en 48–72hs</div>
        </div>
      </li>
    </ol>
  );
}

/**
 * Confirmation — pantalla de confirmación + tracking en vivo.
 *
 * Hooks:
 *   - useParams: lee el orderId de la URL
 *   - useSelector: trae el pedido del store
 *   - useEffect: avanza el tracking simuladamente cada 4s
 */
function Confirmation() {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const orderId = Number(id);
  const order = useSelector((s) => s.orders.list.find((o) => o.order_id === orderId));
  const userEmail = useSelector((s) => s.session.user.email);

  // Simulación de avance de tracking
  useEffect(() => {
    if (!order) return;
    const steps = [
      { checkpoint: 'En depósito · Pellegrini',          status: 'preparing',        delay: 3000 },
      { checkpoint: 'En tránsito · Correo Argentino',    status: 'shipped',          delay: 7000 },
      { checkpoint: 'En centro de distribución · CABA',  status: 'shipped',          delay: 11000 },
      { checkpoint: 'En reparto',                        status: 'out_for_delivery', delay: 15000 },
    ];
    const timers = steps.map((s) =>
      setTimeout(() => {
        dispatch(advanceTracking({
          orderId: order.order_id,
          checkpoint: { ...s, recorded_at: new Date().toISOString() },
          status: s.status,
        }));
      }, s.delay)
    );
    return () => timers.forEach(clearTimeout);
  }, [orderId, dispatch]);

  if (!order) {
    return (
      <main className="screen">
        <Empty
          title="Pedido no encontrado"
          action={<Btn variant="primary" onClick={() => navigate('/')}>Volver al catálogo</Btn>}
        />
      </main>
    );
  }

  return (
    <main className="screen">
      <div className="confirm-hero">
        <div className="confirm-mark">✓</div>
        <div className="eyebrow mono">PEDIDO CONFIRMADO</div>
        <h1 className="confirm-title">Pedido confirmado</h1>
        <p className="confirm-sub">
          Te enviamos un email a <span className="mono">{userEmail}</span> con el detalle.
        </p>
        <div className="confirm-id">
          Pedido #{order.order_id} · Seguimiento {order.tracking_number}
        </div>
      </div>

      <div className="confirm-grid">
        <div className="confirm-block">
          <div className="eyebrow mono">SEGUIMIENTO EN VIVO</div>
          <h2 className="confirm-block-title">Tu pedido en camino</h2>
          <TrackingTimeline tracking={order.tracking} />
        </div>

        <div className="confirm-block">
          <div className="eyebrow mono">RESUMEN</div>
          <h2 className="confirm-block-title">Detalle del pedido</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 'var(--pad)', paddingBottom: 'var(--pad)', borderBottom: '1px solid var(--border)' }}>
            {order.items.map((it, i) => (
              <div key={i} style={{ display: 'grid', gridTemplateColumns: '28px 1fr auto', gap: 10, alignItems: 'center' }}>
                <div className="mono">{it.quantity}×</div>
                <div>
                  <div>{it.product_name}</div>
                  <div className="mono" style={{ color: 'var(--fg-muted)', fontSize: 11, marginTop: 2 }}>
                    {slugAttrs(it.attrs)} · REF {it.sku}
                  </div>
                </div>
                <div className="mono">{fmtARS(it.subtotal)}</div>
              </div>
            ))}
          </div>
          <div className="totals totals-compact">
            <div className="total-row"><span>subtotal</span><span>{fmtARS(order.totals.subtotal)}</span></div>
            {order.totals.discount > 0 && (
              <div className="total-row total-row-savings"><span>cupón</span><span>−{fmtARS(order.totals.discount)}</span></div>
            )}
            <div className="total-row"><span>envío</span><span>{order.totals.shipping === 0 ? 'GRATIS' : fmtARS(order.totals.shipping)}</span></div>
            <div className="total-row total-grand"><span>TOTAL PAGADO</span><span>{fmtARS(order.totals.total)}</span></div>
          </div>
          <div className="mono" style={{ marginTop: 'var(--pad)', paddingTop: 'var(--pad)', borderTop: '1px solid var(--border)', color: 'var(--fg-2)' }}>
            <div className="eyebrow">DIRECCIÓN</div>
            <div>{order.address.street}, {order.address.city}, {order.address.state} {order.address.zip_code}</div>
          </div>
        </div>
      </div>

      <div className="confirm-foot">
        <Btn variant="ghost" onClick={() => navigate('/')}>Seguir comprando</Btn>
        <Btn variant="primary" onClick={() => navigate('/pedidos')}>Ver todos mis pedidos →</Btn>
      </div>
    </main>
  );
}

export default Confirmation;
