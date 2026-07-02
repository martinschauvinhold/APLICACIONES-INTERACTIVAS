import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { ArrowLeft, Plus, MessageSquare, Send, TriangleAlert } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import {
  fetchTickets,
  createTicket,
  fetchTicketMessages,
  addTicketMessage,
  selectTickets,
  selectTicketsLoading,
  selectTicketsError,
  selectTicketMessages,
  selectMessagesLoading,
  selectTicketMutating,
  selectTicketMutateError,
} from '../features/tickets/ticketsSlice';
import { fetchOrdersByUser, selectOrders } from '../features/orders/ordersSlice';
import Btn from '../components/Btn';
import Field from '../components/Field';
import Empty from '../components/Empty';
import { t, ticketStatus } from '../i18n';

const fmtDate = (iso) => (iso ? new Date(iso).toLocaleDateString('es-AR') : '');
const fmtDateTime = (iso) => (iso ? new Date(iso).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' }) : '');

function Soporte() {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const allTickets = useSelector(selectTickets);
  const ticketsLoading = useSelector(selectTicketsLoading);
  const ticketsError = useSelector(selectTicketsError);
  const mutating = useSelector(selectTicketMutating);
  const mutateError = useSelector(selectTicketMutateError);
  const allOrders = useSelector(selectOrders);

  const [view, setView] = useState('list'); 
  const [selectedId, setSelectedId] = useState(null);
  const [form, setForm] = useState({ motivo: 'pedido', orderId: '', mensaje: '' });
  const [errors, setErrors] = useState({});
  const [reply, setReply] = useState('');

  const messages = useSelector(selectTicketMessages(selectedId));
  const messagesLoading = useSelector(selectMessagesLoading);

  const motivos = t('info.support.motivos');
  const faqs = t('info.support.faqs');

  const myTickets = allTickets.filter((tk) => tk.user_id === user.user_id);
  const deliveredOrders = allOrders.filter((o) => o.status === 'delivered');
  const selectedTicket = myTickets.find((tk) => tk.ticket_id === selectedId) || null;

  useEffect(() => {
    if (!isAuthenticated) return;
    dispatch(fetchTickets());
    dispatch(fetchOrdersByUser(user.user_id));
  }, [dispatch, isAuthenticated, user.user_id]);

  useEffect(() => {
    if (view === 'detail' && selectedId != null) dispatch(fetchTicketMessages(selectedId));
  }, [dispatch, view, selectedId]);

  const openTicket = (id) => {
    setSelectedId(id);
    setView('detail');
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    const errs = {};
    if (form.mensaje.trim().length < 10) errs.mensaje = t('info.support.errMessage');
    setErrors(errs);
    if (Object.keys(errs).length) return;

    const subject = motivos.find((m) => m.id === form.motivo)?.label || form.motivo;
    try {
      const ticket = await dispatch(createTicket({
        userId: user.user_id,
        subject,
        status: 'OPEN',
        orderId: form.orderId || null,
      })).unwrap();
      await dispatch(addTicketMessage({
        id: ticket.ticket_id,
        body: { senderId: user.user_id, content: form.mensaje.trim() },
      })).unwrap();
      setForm({ motivo: 'pedido', orderId: '', mensaje: '' });
      openTicket(ticket.ticket_id);
    } catch {
      // El error queda reflejado en mutateError (render condicional).
    }
  };

  const handleReply = async (e) => {
    e.preventDefault();
    if (reply.trim().length === 0 || selectedId == null) return;
    try {
      await dispatch(addTicketMessage({
        id: selectedId,
        body: { senderId: user.user_id, content: reply.trim() },
      })).unwrap();
      setReply('');
    } catch {
      // mutateError refleja el fallo.
    }
  };

  function renderLeft() {
    if (!isAuthenticated) {
      return (
        <Empty
          title={t('info.support.loginRequired')}
          action={<Btn variant="primary" onClick={() => navigate('/login')}>{t('info.support.loginCta')}</Btn>}
        />
      );
    }

    if (view === 'create') {
      return (
        <form className="support-form" onSubmit={handleCreate}>
          <div className="ticket-toolbar">
            <Btn variant="ghost" type="button" onClick={() => { setView('list'); setErrors({}); }}>
              <ArrowLeft size={16} /> {t('info.support.backToList')}
            </Btn>
          </div>
          <h2 className="support-h2">{t('info.support.createTitle')}</h2>

          <Field label={t('info.support.reason')}>
            <select className="select" value={form.motivo} onChange={(e) => setForm({ ...form, motivo: e.target.value })}>
              {motivos.map((m) => <option key={m.id} value={m.id}>{m.label}</option>)}
            </select>
          </Field>

          <Field label={t('info.support.relatedOrder')}>
            {deliveredOrders.length === 0 ? (
              <div className="ticket-hint mono">{t('info.support.noDelivered')}</div>
            ) : (
              <select className="select" value={form.orderId} onChange={(e) => setForm({ ...form, orderId: e.target.value })}>
                <option value="">{t('info.support.noOrder')}</option>
                {deliveredOrders.map((o) => (
                  <option key={o.order_id} value={o.order_id}>
                    {t('info.support.orderOption', { id: o.order_id, date: fmtDate(o.created_at) })}
                  </option>
                ))}
              </select>
            )}
          </Field>

          <Field label={t('info.support.message')} error={errors.mensaje}>
            <textarea
              className="input support-textarea"
              rows={5}
              value={form.mensaje}
              onChange={(e) => setForm({ ...form, mensaje: e.target.value })}
              placeholder={t('info.support.messagePlaceholder')}
            />
          </Field>

          {mutateError && (
            <span className="field-error"><TriangleAlert size={14} /> {mutateError}</span>
          )}

          <Btn variant="primary" size="lg" disabled={mutating}>
            {mutating ? t('info.support.sending') : t('info.support.submit')}
          </Btn>
        </form>
      );
    }

    if (view === 'detail' && selectedTicket) {
      return (
        <div className="ticket-detail">
          <div className="ticket-toolbar">
            <Btn variant="ghost" type="button" onClick={() => setView('list')}>
              <ArrowLeft size={16} /> {t('info.support.backToList')}
            </Btn>
          </div>

          <div className="ticket-detail-head">
            <h2 className="support-h2">{selectedTicket.subject}</h2>
            <span className={`order-status order-status-${selectedTicket.status}`}>
              {ticketStatus(selectedTicket.status)}
            </span>
          </div>
          <div className="ticket-row-meta">
            #{selectedTicket.ticket_id} · {fmtDate(selectedTicket.created_at)}
            {selectedTicket.order_id != null && ` · ${t('info.support.relatedOrderTag', { id: selectedTicket.order_id })}`}
          </div>

          <div className="eyebrow mono ticket-section-label">{t('info.support.conversation')}</div>
          {messagesLoading && messages.length === 0 ? (
            <div className="ticket-hint mono">{t('info.support.loadingMessages')}</div>
          ) : messages.length === 0 ? (
            <div className="ticket-hint mono">{t('info.support.noMessages')}</div>
          ) : (
            <div className="ticket-messages">
              {messages.map((m) => {
                const mine = m.sender_id === user.user_id;
                return (
                  <div key={m.message_id} className={`ticket-msg ${mine ? 'ticket-msg-mine' : ''}`}>
                    <div className="ticket-msg-content">{m.content}</div>
                    <div className="ticket-msg-meta">
                      {mine ? t('info.support.you') : t('info.support.team')} · {fmtDateTime(m.created_at)}
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          <form className="ticket-reply" onSubmit={handleReply}>
            <textarea
              className="input support-textarea"
              rows={3}
              value={reply}
              onChange={(e) => setReply(e.target.value)}
              placeholder={t('info.support.replyPlaceholder')}
            />
            {mutateError && (
              <span className="field-error"><TriangleAlert size={14} /> {mutateError}</span>
            )}
            <Btn variant="primary" disabled={mutating || reply.trim().length === 0}>
              {mutating ? t('info.support.sendingReply') : <><Send size={16} /> {t('info.support.send')}</>}
            </Btn>
          </form>
        </div>
      );
    }

    return (
      <>
        <div className="ticket-toolbar">
          <h2 className="support-h2">{t('info.support.ticketsTitle')}</h2>
          <Btn variant="primary" onClick={() => { setView('create'); setErrors({}); }}>
            <Plus size={16} /> {t('info.support.newTicket')}
          </Btn>
        </div>

        {ticketsError && (
          <span className="field-error"><TriangleAlert size={14} /> {ticketsError}</span>
        )}

        {ticketsLoading && myTickets.length === 0 ? (
          <div className="ticket-hint mono">{t('info.support.loadingTickets')}</div>
        ) : myTickets.length === 0 ? (
          <Empty title={t('info.support.ticketsEmpty')} />
        ) : (
          <div className="tickets-list">
            {myTickets.map((tk) => (
              <article key={tk.ticket_id} className="ticket-row" onClick={() => openTicket(tk.ticket_id)}>
                <MessageSquare size={18} className="ticket-row-icon" />
                <div className="ticket-row-main">
                  <div className="ticket-row-subject">{tk.subject}</div>
                  <div className="ticket-row-meta">
                    #{tk.ticket_id} · {fmtDate(tk.created_at)}
                    {tk.order_id != null && ` · ${t('info.support.relatedOrderTag', { id: tk.order_id })}`}
                  </div>
                </div>
                <span className={`order-status order-status-${tk.status}`}>{ticketStatus(tk.status)}</span>
              </article>
            ))}
          </div>
        )}
      </>
    );
  }

  return (
    <main className="screen">
      <div className="eyebrow mono">{t('info.support.eyebrow')}</div>
      <h1 className="screen-title">{t('info.support.title')} <em>{t('info.support.titleEm')}</em></h1>

      <div className="info-2col">
        <div>{renderLeft()}</div>

        <div>
          <h2 className="support-h2">{t('info.support.faqTitle')}</h2>
          <div className="faq-list">
            {faqs.map((faq) => (
              <details key={faq.q} className="faq">
                <summary>{faq.q} <span>+</span></summary>
                <p>{faq.a}</p>
              </details>
            ))}
          </div>

          <div className="support-hours">
            <div className="eyebrow mono">{t('info.support.hoursTitle')}</div>
            <div className="support-hours-body">
              <div>{t('info.support.hoursWeek')}</div>
              <div>{t('info.support.hoursSat')}</div>
              <div className="mono">{t('info.support.contactEmail')}</div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

export default Soporte;
