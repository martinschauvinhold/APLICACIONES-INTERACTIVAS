import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TriangleAlert } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { useCheckout } from '../hooks/useCheckout';
import { useOrders } from '../hooks/useOrders';
import ProductImage from '../components/ProductImage';
import Btn from '../components/Btn';
import Stepper from '../components/Stepper';
import Field from '../components/Field';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { t } from '../i18n';

const METHOD_IDS = ['credit_card', 'debit_card'];

function CheckoutAddress() {
  const { addresses, addAddress } = useAuth();
  const { addressId, setAddress, setStep } = useCheckout();

  const [showNew, setShowNew] = useState(false);
  const [form, setForm] = useState({ street: '', city: '', state: '', zip_code: '', reference_note: '' });
  const [saveError, setSaveError] = useState('');

  const handleSaveNew = async () => {
    try {
      const created = await addAddress(form);
      setAddress({ addressId: created.address_id });
      setShowNew(false);
      setForm({ street: '', city: '', state: '', zip_code: '', reference_note: '' });
      setSaveError('');
    } catch (err) {
      setSaveError(err.message || 'No se pudo guardar la dirección.');
    }
  };

  return (
    <div className="step-card">
      <h2 className="step-title">{t('checkout.address.title')}</h2>
      <p className="step-hint">{t('checkout.address.hint')}</p>

      <div className="addr-list">
        {addresses.map((a) => (
          <label key={a.address_id} className={`addr ${addressId === a.address_id ? 'is-selected' : ''}`}>
            <input
              type="radio"
              name="address"
              checked={addressId === a.address_id}
              onChange={() => setAddress({ addressId: a.address_id })}
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
        <button className="addr-add-btn mono" onClick={() => setShowNew(true)}>
          {t('checkout.address.addNew')}
        </button>
      ) : (
        <div className="addr-form">
          <Field label={t('checkout.address.street')}>
            <input className="input" value={form.street} onChange={(e) => setForm({ ...form, street: e.target.value })} placeholder={t('checkout.address.streetPlaceholder')} />
          </Field>
          <div className="addr-form-row">
            <Field label={t('checkout.address.city')}>
              <input className="input" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} placeholder={t('checkout.address.cityPlaceholder')} />
            </Field>
            <Field label={t('checkout.address.province')}>
              <input className="input" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} placeholder={t('checkout.address.provincePlaceholder')} />
            </Field>
            <Field label={t('checkout.address.zip')}>
              <input className="input" value={form.zip_code} onChange={(e) => setForm({ ...form, zip_code: e.target.value })} placeholder={t('checkout.address.zipPlaceholder')} />
            </Field>
          </div>
          <Field label={t('checkout.address.reference')} error={saveError}>
            <input className="input" value={form.reference_note} onChange={(e) => setForm({ ...form, reference_note: e.target.value })} placeholder={t('checkout.address.referencePlaceholder')} />
          </Field>
          <div className="addr-form-actions">
            <Btn variant="ghost" onClick={() => setShowNew(false)}>{t('checkout.address.cancel')}</Btn>
            <Btn variant="primary" disabled={!form.street || !form.city} onClick={handleSaveNew}>{t('checkout.address.save')}</Btn>
          </div>
        </div>
      )}

      <div className="step-foot">
        <Btn variant="primary" size="lg" onClick={() => setStep({ step: 1 })}>
          {t('checkout.address.continue')}
        </Btn>
      </div>
    </div>
  );
}

function CheckoutPayment() {
  const co = useCheckout();
  const { setPayment, setStep } = co;
  const methods = t('checkout.methods');
  const [method, setMethod] = useState(co.paymentMethod);
  const [card, setCard] = useState({
    number: '4242 4242 4242 ' + co.cardLast4,
    holder: co.cardHolder,
    expiry: co.cardExpiry,
    cvc: '123',
  });
  const [errors, setErrors] = useState({});

  const validate = () => {
    const errs = {};
    const digits = card.number.replace(/\D/g, '');
    if (digits.length < 13) errs.number = t('checkout.payment.errNumber');
    if (!card.holder.trim()) errs.holder = t('checkout.payment.errHolder');
    if (!/^\d{2}\/\d{2}$/.test(card.expiry)) errs.expiry = t('checkout.payment.errExpiry');
    if (!/^\d{3,4}$/.test(card.cvc)) errs.cvc = t('checkout.payment.errCvc');
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleNext = () => {
    if (!validate()) return;
    const last4 = card.number.replace(/\D/g, '').slice(-4);
    setPayment({ paymentMethod: method, cardLast4: last4, cardHolder: card.holder, cardExpiry: card.expiry });
    setStep({ step: 2 });
  };

  return (
    <div className="step-card">
      <h2 className="step-title">{t('checkout.payment.title')}</h2>
      <p className="step-hint">{t('checkout.payment.hint')}</p>

      <div className="pay-methods">
        {METHOD_IDS.map((id) => (
          <label key={id} className={`pay-method ${method === id ? 'is-selected' : ''}`}>
            <input type="radio" name="pay" checked={method === id} onChange={() => setMethod(id)} />
            <div>
              <div className="pay-method-name">{methods[id].label}</div>
              <div className="pay-method-sub">{methods[id].sub}</div>
            </div>
          </label>
        ))}
      </div>

      <div className="card-form">
        <Field label={t('checkout.payment.cardNumber')} error={errors.number}>
          <input className="input mono" value={card.number} onChange={(e) => setCard({ ...card, number: e.target.value })} placeholder={t('checkout.payment.cardNumberPlaceholder')} />
        </Field>
        <Field label={t('checkout.payment.holder')} error={errors.holder}>
          <input className="input" value={card.holder} onChange={(e) => setCard({ ...card, holder: e.target.value.toUpperCase() })} placeholder={t('checkout.payment.holderPlaceholder')} />
        </Field>
        <div className="card-form-row">
          <Field label={t('checkout.payment.expiry')} error={errors.expiry}>
            <input className="input mono" value={card.expiry} onChange={(e) => setCard({ ...card, expiry: e.target.value })} placeholder="MM/AA" />
          </Field>
          <Field label={t('checkout.payment.cvc')} error={errors.cvc}>
            <input className="input mono" value={card.cvc} onChange={(e) => setCard({ ...card, cvc: e.target.value })} placeholder="123" />
          </Field>
        </div>
      </div>

      <div className="step-foot step-foot-split">
        <Btn variant="ghost" onClick={() => setStep({ step: 0 })}>{t('checkout.payment.back')}</Btn>
        <Btn variant="primary" size="lg" onClick={handleNext}>{t('checkout.payment.next')}</Btn>
      </div>
    </div>
  );
}

function CheckoutReview() {
  const { lines, totals, clearCart } = useCart();
  const checkout = useCheckout();
  const { setStep, resetCheckout } = checkout;
  const { addresses } = useAuth();
  const { placeOrder } = useOrders();
  const navigate = useNavigate();
  const [placing, setPlacing] = useState(false);
  const [placeError, setPlaceError] = useState('');

  const address = addresses.find((a) => a.address_id === checkout.addressId);
  const methods = t('checkout.methods');

  const handlePlaceOrder = async () => {
    setPlacing(true);
    setPlaceError('');
    try {
      const order = await placeOrder({
        addressId: address.address_id,
        paymentMethod: checkout.paymentMethod,
      });
      clearCart();
      resetCheckout();
      navigate(`/confirmacion/${order.order_id}`);
    } catch (err) {
      setPlaceError(err.message || 'No se pudo completar la compra. Intentá de nuevo.');
      setPlacing(false);
    }
  };

  return (
    <div className="step-card">
      <h2 className="step-title">{t('checkout.review.title')}</h2>
      <p className="step-hint">{t('checkout.review.hint')}</p>

      <div className="review">
        <div className="review-block">
          <div className="review-h">{t('checkout.review.shipTo')}</div>
          <div>
            <div>{address.street}</div>
            <div className="mono">{address.city} · {address.state} · {address.zip_code}</div>
            {address.reference_note && <div className="mono">{address.reference_note}</div>}
          </div>
          <button className="review-edit" onClick={() => setStep({ step: 0 })}>{t('checkout.review.edit')}</button>
        </div>

        <div className="review-block">
          <div className="review-h">{t('checkout.review.payment')}</div>
          <div>
            <div>{methods[checkout.paymentMethod].label}</div>
            {(checkout.paymentMethod === 'credit_card' || checkout.paymentMethod === 'debit_card') && (
              <div className="mono">**** **** **** {checkout.cardLast4}</div>
            )}
          </div>
          <button className="review-edit" onClick={() => setStep({ step: 1 })}>{t('checkout.review.edit')}</button>
        </div>

        <div className="review-block">
          <div className="review-h">{t('checkout.review.products', { n: totals.itemCount })}</div>
          <div className="review-items">
            {lines.map((l) => (
              <div key={l.variant.variant_id} className="review-item">
                <div className="mono">{l.quantity}×</div>
                <div>
                  <div>{l.product.name}</div>
                  <div className="mono review-item-attrs">{slugAttrs(l.variant.attrs)}</div>
                </div>
                <div className="mono review-item-sub">{fmtARS(l.subtotal)}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {placeError && (
        <span className="field-error">
          <TriangleAlert size={13} /> {placeError}
        </span>
      )}

      <div className="step-foot step-foot-split">
        <Btn variant="ghost" onClick={() => setStep({ step: 1 })}>{t('checkout.review.back')}</Btn>
        <Btn variant="primary" size="lg" disabled={placing} onClick={handlePlaceOrder}>
          {placing ? t('checkout.review.placing') : t('checkout.review.confirm', { total: fmtARS(totals.total) })}
        </Btn>
      </div>
    </div>
  );
}

function Checkout() {
  const { lines, totals } = useCart();
  const { step } = useCheckout();
  const navigate = useNavigate();

  if (lines.length === 0) {
    return (
      <main className="screen">
        <Empty
          title={t('checkout.emptyTitle')}
          hint={t('checkout.emptyHint')}
          action={<Btn variant="primary" onClick={() => navigate('/')}>{t('checkout.goCatalog')}</Btn>}
        />
      </main>
    );
  }

  const steps = [t('checkout.steps.address'), t('checkout.steps.payment'), t('checkout.steps.review')];

  return (
    <main className="screen">
      <div className="eyebrow mono">{t('checkout.eyebrow')}</div>
      <h1 className="screen-title">{t('checkout.title')}</h1>

      <Stepper steps={steps} current={step} />

      <div className="checkout-grid">
        <div>
          {step === 0 && <CheckoutAddress />}
          {step === 1 && <CheckoutPayment />}
          {step === 2 && <CheckoutReview />}
        </div>

        <aside className="checkout-summary">
          <div className="eyebrow mono">{t('checkout.summary')}</div>
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
            <div className="total-row"><span>{t('cart.subtotal')}</span><span>{fmtARS(totals.subtotal)}</span></div>
            {totals.discount > 0 && (
              <div className="total-row total-row-savings"><span>{t('confirmation.coupon')}</span><span>−{fmtARS(totals.discount)}</span></div>
            )}
            <div className="total-row"><span>{t('cart.shipping')}</span><span>{totals.shipping === 0 ? t('cart.free') : fmtARS(totals.shipping)}</span></div>
            <div className="total-row total-grand"><span>{t('cart.total')}</span><span>{fmtARS(totals.total)}</span></div>
          </div>
        </aside>
      </div>
    </main>
  );
}

export default Checkout;
