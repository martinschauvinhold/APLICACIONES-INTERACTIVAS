import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import ProductImage from '../components/ProductImage';
import Btn from '../components/Btn';
import Stepper from '../components/Stepper';
import Field from '../components/Field';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { selectCartLines, selectCartTotals } from '../store/selectors';
import { setStep, setAddress, setPayment, reset as resetCheckout } from '../store/slices/checkoutSlice';
import { addAddress } from '../store/slices/sessionSlice';
import { clear as clearCart } from '../store/slices/cartSlice';
import { place } from '../store/slices/ordersSlice';
import { api } from '../api/client';

// ====== PASO 1: DIRECCIÓN ======
function CheckoutAddress() {
  const addresses = useSelector((s) => s.session.addresses);
  const addressId = useSelector((s) => s.checkout.addressId);
  const dispatch = useDispatch();

  const [showNew, setShowNew] = useState(false);
  const [form, setForm] = useState({ street: '', city: '', state: '', zip_code: '', reference_note: '' });

  const handleSaveNew = () => {
    dispatch(addAddress(form));
    setShowNew(false);
    setForm({ street: '', city: '', state: '', zip_code: '', reference_note: '' });
  };

  return (
    <div className="step-card">
      <h2 className="step-title">¿Dónde lo enviamos?</h2>
      <p className="step-hint">Elegí una dirección guardada o agregá una nueva.</p>

      <div className="addr-list">
        {addresses.map((a) => (
          <label key={a.address_id} className={`addr ${addressId === a.address_id ? 'is-selected' : ''}`}>
            <input
              type="radio"
              name="address"
              checked={addressId === a.address_id}
              onChange={() => dispatch(setAddress({ addressId: a.address_id }))}
            />
            <div>
              <div className="addr-street">{a.street}</div>
              <div className="addr-city">{a.city} · {a.state} · {a.zip_code}</div>
              {a.reference_note && <div className="addr-note">{a.reference_note}</div>}
            </div>
            <div className="addr-id">#{a.address_id}</div>
          </label>
        ))}
      </div>

      {!showNew ? (
        <button
          onClick={() => setShowNew(true)}
          style={{
            marginTop: 8,
            padding: 'var(--pad)',
            width: '100%',
            border: '1px dashed var(--border-2)',
            borderRadius: 'var(--radius)',
            color: 'var(--fg-muted)',
            fontSize: 12,
            fontFamily: 'var(--font-mono)',
          }}
        >+ Agregar nueva dirección</button>
      ) : (
        <div style={{
          marginTop: 'var(--pad)',
          padding: 'var(--pad)',
          background: 'var(--bg-2)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius)',
          display: 'flex', flexDirection: 'column', gap: 'var(--pad)',
        }}>
          <Field label="CALLE Y NÚMERO">
            <input className="input" value={form.street} onChange={(e) => setForm({ ...form, street: e.target.value })} placeholder="Av. Corrientes 1234" />
          </Field>
          <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr', gap: 'var(--pad)' }}>
            <Field label="CIUDAD">
              <input className="input" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} placeholder="Buenos Aires" />
            </Field>
            <Field label="PROVINCIA">
              <input className="input" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} placeholder="CABA" />
            </Field>
            <Field label="CÓDIGO POSTAL">
              <input className="input" value={form.zip_code} onChange={(e) => setForm({ ...form, zip_code: e.target.value })} placeholder="1043" />
            </Field>
          </div>
          <Field label="REFERENCIA (OPCIONAL)">
            <input className="input" value={form.reference_note} onChange={(e) => setForm({ ...form, reference_note: e.target.value })} placeholder="Piso, depto, timbre…" />
          </Field>
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <Btn variant="ghost" onClick={() => setShowNew(false)}>Cancelar</Btn>
            <Btn variant="primary" disabled={!form.street || !form.city} onClick={handleSaveNew}>Guardar dirección</Btn>
          </div>
        </div>
      )}

      <div className="step-foot">
        <Btn variant="primary" size="lg" onClick={() => dispatch(setStep({ step: 1 }))}>
          Continuar al pago →
        </Btn>
      </div>
    </div>
  );
}

// ====== PASO 2: PAGO ======
function CheckoutPayment() {
  const co = useSelector((s) => s.checkout);
  const dispatch = useDispatch();
  const [method, setMethod] = useState(co.paymentMethod);
  const [card, setCard] = useState({
    number: '4242 4242 4242 ' + co.cardLast4,
    holder: co.cardHolder,
    expiry: co.cardExpiry,
    cvc: '123',
  });
  const [errors, setErrors] = useState({});

  const validate = () => {
    if (method !== 'credit_card' && method !== 'debit_card') return true;
    const errs = {};
    const digits = card.number.replace(/\D/g, '');
    if (digits.length < 13) errs.number = 'Número incompleto';
    if (!card.holder.trim()) errs.holder = 'Nombre del titular';
    if (!/^\d{2}\/\d{2}$/.test(card.expiry)) errs.expiry = 'Formato MM/AA';
    if (!/^\d{3,4}$/.test(card.cvc)) errs.cvc = 'CVC inválido';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleNext = () => {
    if (!validate()) return;
    const last4 = card.number.replace(/\D/g, '').slice(-4);
    dispatch(setPayment({
      paymentMethod: method,
      cardLast4: last4,
      cardHolder: card.holder,
      cardExpiry: card.expiry,
    }));
    dispatch(setStep({ step: 2 }));
  };

  return (
    <div className="step-card">
      <h2 className="step-title">Método de pago</h2>
      <p className="step-hint">Procesado por nuestro gateway. Transacción cifrada.</p>

      <div className="pay-methods">
        {[
          { id: 'credit_card',   label: 'Tarjeta de crédito',     sub: 'Visa · Mastercard · Amex' },
          { id: 'debit_card',    label: 'Tarjeta de débito',      sub: 'VISA Debit · Maestro' },
          { id: 'mercadopago',   label: 'Mercado Pago',           sub: 'Saldo en cuenta · cuotas sin interés' },
          { id: 'bank_transfer', label: 'Transferencia bancaria', sub: '5% off por transferencia' },
        ].map((m) => (
          <label key={m.id} className={`pay-method ${method === m.id ? 'is-selected' : ''}`}>
            <input type="radio" name="pay" checked={method === m.id} onChange={() => setMethod(m.id)} />
            <div>
              <div className="pay-method-name">{m.label}</div>
              <div className="pay-method-sub">{m.sub}</div>
            </div>
          </label>
        ))}
      </div>

      {(method === 'credit_card' || method === 'debit_card') && (
        <div className="card-form">
          <Field label="NÚMERO DE TARJETA" error={errors.number}>
            <input className="input mono" value={card.number} onChange={(e) => setCard({ ...card, number: e.target.value })} placeholder="0000 0000 0000 0000" />
          </Field>
          <Field label="TITULAR" error={errors.holder}>
            <input className="input" value={card.holder} onChange={(e) => setCard({ ...card, holder: e.target.value.toUpperCase() })} placeholder="COMO FIGURA EN LA TARJETA" />
          </Field>
          <div className="card-form-row">
            <Field label="VENCIMIENTO" error={errors.expiry}>
              <input className="input mono" value={card.expiry} onChange={(e) => setCard({ ...card, expiry: e.target.value })} placeholder="MM/AA" />
            </Field>
            <Field label="CVC" error={errors.cvc}>
              <input className="input mono" value={card.cvc} onChange={(e) => setCard({ ...card, cvc: e.target.value })} placeholder="123" />
            </Field>
          </div>
        </div>
      )}

      {method === 'mercadopago' && (
        <div className="info-block">{'> Te vamos a redirigir a Mercado Pago para completar el pago.'}</div>
      )}
      {method === 'bank_transfer' && (
        <div className="info-block">
          {'> CBU 0170012340000000001234'}<br />
          {'> Alias: VECTOR.TECH.AR'}<br />
          {'> Enviá el comprobante a pagos@vector.tech'}
        </div>
      )}

      <div className="step-foot step-foot-split">
        <Btn variant="ghost" onClick={() => dispatch(setStep({ step: 0 }))}>← Volver</Btn>
        <Btn variant="primary" size="lg" onClick={handleNext}>Revisar pedido →</Btn>
      </div>
    </div>
  );
}

// ====== PASO 3: REVISIÓN ======
function CheckoutReview() {
  const checkout = useSelector((s) => s.checkout);
  const addresses = useSelector((s) => s.session.addresses);
  const lines = useSelector(selectCartLines);
  const totals = useSelector(selectCartTotals);
  const user = useSelector((s) => s.session.user);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [placing, setPlacing] = useState(false);

  const address = addresses.find((a) => a.address_id === checkout.addressId);

  const placeOrder = async () => {
    setPlacing(true);

    // Intenta el checkout completo contra el backend
    let realOrderId = null;
    let realTrackingNumber = null;
    try {
      const res = await api.checkout({
        userId: user.user_id,
        shippingAddressId: address.address_id,
        lines: lines.map((l) => ({ variantId: l.variant.variant_id, quantity: l.quantity })),
        paymentMethod: checkout.paymentMethod,
      });
      realOrderId = res.orderId;
      realTrackingNumber = res.trackingNumber;
    } catch (err) {
      // Fallback: orden local (modo demo)
    }

    setTimeout(() => {
      const orderId = realOrderId || (1000 + Math.floor(Math.random() * 9000));
      const order = {
        order_id: orderId,
        user_id: user.user_id,
        shipping_address_id: address.address_id,
        address,
        status: 'paid',
        items: lines.map((l) => ({
          product_id: l.product.product_id,
          image_url: l.product.image_url,
          variant_id: l.variant.variant_id,
          sku: l.variant.sku,
          product_name: l.product.name,
          attrs: l.variant.attrs,
          quantity: l.quantity,
          unit_price_at_time: l.unit_price,
          subtotal: l.subtotal,
        })),
        totals,
        payment: { method: checkout.paymentMethod, card_last4: checkout.cardLast4, transaction_id: 'TXN-' + Date.now() },
        tracking_number: realTrackingNumber || ('CA-' + Date.now().toString().slice(-8)),
        tracking: [
          { checkpoint: 'Pedido recibido', status: 'pending', recorded_at: new Date().toISOString() },
          { checkpoint: 'Pago confirmado', status: 'paid', recorded_at: new Date().toISOString() },
        ],
        created_at: new Date().toISOString(),
      };
      dispatch(place(order));
      dispatch(clearCart());
      dispatch(resetCheckout());
      navigate(`/confirmacion/${orderId}`);
    }, realOrderId ? 200 : 1100);
  };

  const methodLabels = {
    credit_card:   'Tarjeta de crédito',
    debit_card:    'Tarjeta de débito',
    mercadopago:   'Mercado Pago',
    bank_transfer: 'Transferencia bancaria',
  };

  return (
    <div className="step-card">
      <h2 className="step-title">Confirmá tu pedido</h2>
      <p className="step-hint">Revisá los datos antes de confirmar.</p>

      <div className="review">
        <div className="review-block">
          <div className="review-h">DIRECCIÓN DE ENVÍO</div>
          <div>
            <div>{address.street}</div>
            <div className="mono">{address.city} · {address.state} · {address.zip_code}</div>
            {address.reference_note && <div className="mono">{address.reference_note}</div>}
          </div>
          <button className="review-edit" onClick={() => dispatch(setStep({ step: 0 }))}>editar</button>
        </div>

        <div className="review-block">
          <div className="review-h">PAGO</div>
          <div>
            <div>{methodLabels[checkout.paymentMethod]}</div>
            {(checkout.paymentMethod === 'credit_card' || checkout.paymentMethod === 'debit_card') && (
              <div className="mono">**** **** **** {checkout.cardLast4}</div>
            )}
          </div>
          <button className="review-edit" onClick={() => dispatch(setStep({ step: 1 }))}>editar</button>
        </div>

        <div className="review-block">
          <div className="review-h">PRODUCTOS · {totals.itemCount} ITEMS</div>
          <div className="review-items">
            {lines.map((l) => (
              <div key={l.variant.variant_id} className="review-item">
                <div className="mono">{l.quantity}×</div>
                <div>
                  <div>{l.product.name}</div>
                  <div className="mono" style={{ color: 'var(--fg-muted)', fontSize: 11, marginTop: 2 }}>{slugAttrs(l.variant.attrs)}</div>
                </div>
                <div className="mono" style={{ fontWeight: 600 }}>{fmtARS(l.subtotal)}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="step-foot step-foot-split">
        <Btn variant="ghost" onClick={() => dispatch(setStep({ step: 1 }))}>← Volver</Btn>
        <Btn variant="primary" size="lg" disabled={placing} onClick={placeOrder}>
          {placing ? 'Procesando pago…' : `Confirmar y pagar · ${fmtARS(totals.total)}`}
        </Btn>
      </div>
    </div>
  );
}

// ====== Página principal ======

/**
 * Checkout — flujo de 3 pasos: Dirección, Pago, Revisión.
 *
 * Hooks:
 *   - useSelector: step actual + líneas del carrito + totales
 *   - useDispatch: setStep, etc.
 *   - useNavigate: redirige al catálogo si el carrito está vacío
 */
function Checkout() {
  const step = useSelector((s) => s.checkout.step);
  const lines = useSelector(selectCartLines);
  const totals = useSelector(selectCartTotals);
  const navigate = useNavigate();

  if (lines.length === 0) {
    return (
      <main className="screen">
        <Empty
          title="No hay nada para checkout"
          hint="Tu carrito está vacío."
          action={<Btn variant="primary" onClick={() => navigate('/')}>Ir al catálogo</Btn>}
        />
      </main>
    );
  }

  return (
    <main className="screen">
      <div className="eyebrow mono">CHECKOUT</div>
      <h1 className="screen-title">Checkout</h1>

      <Stepper steps={['Dirección', 'Pago', 'Revisión']} current={step} />

      <div className="checkout-grid">
        <div>
          {step === 0 && <CheckoutAddress />}
          {step === 1 && <CheckoutPayment />}
          {step === 2 && <CheckoutReview />}
        </div>

        <aside className="checkout-summary">
          <div className="eyebrow mono">RESUMEN</div>
          {lines.map((l) => (
            <div key={l.variant.variant_id} className="checkout-summary-line">
              <div className="checkout-summary-img">
                <ProductImage src={l.product.image_url} alt={l.product.name} ratio="1/1" />
              </div>
              <div>
                <div className="checkout-summary-name">{l.product.name}</div>
                <div className="checkout-summary-attrs">{slugAttrs(l.variant.attrs)}</div>
                <div className="checkout-summary-qty">x{l.quantity} · {fmtARS(l.unit_price)}</div>
              </div>
              <div className="checkout-summary-sub">{fmtARS(l.subtotal)}</div>
            </div>
          ))}
          <div className="totals totals-compact">
            <div className="total-row"><span>subtotal</span><span>{fmtARS(totals.subtotal)}</span></div>
            {totals.discount > 0 && (
              <div className="total-row total-row-savings"><span>cupón</span><span>−{fmtARS(totals.discount)}</span></div>
            )}
            <div className="total-row"><span>envío</span><span>{totals.shipping === 0 ? 'GRATIS' : fmtARS(totals.shipping)}</span></div>
            <div className="total-row total-grand"><span>TOTAL</span><span>{fmtARS(totals.total)}</span></div>
          </div>
        </aside>
      </div>
    </main>
  );
}

export default Checkout;
