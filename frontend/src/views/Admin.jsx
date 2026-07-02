import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Trash2, Pencil, TriangleAlert } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import {
  fetchUsers,
  createUser,
  updateUser,
  deleteUser,
  selectUsers,
  selectUsersLoading,
  selectUsersError,
  selectUsersMutating,
} from '../features/users/usersSlice';
import { fetchAllOrders, selectOrders, selectOrdersLoading } from '../features/orders/ordersSlice';
import { fetchCatalog, selectProducts } from '../features/products/productsSlice';
import Btn from '../components/Btn';
import Field from '../components/Field';
import Empty from '../components/Empty';
import { fmtARS } from '../utils/format';
import { t, orderStatus } from '../i18n';

/**
 * Admin — backoffice de plataforma sobre Redux. Gestiona usuarios (usersSlice:
 * alta/baja real) y permite ver todas las órdenes (ordersSlice). No gestiona
 * productos (eso es del vendedor); solo los cuenta para el resumen.
 */
function Admin() {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const users = useSelector(selectUsers);
  const orders = useSelector(selectOrders);
  const products = useSelector(selectProducts);
  const [tab, setTab] = useState('resumen');

  useEffect(() => {
    dispatch(fetchUsers());
    dispatch(fetchAllOrders());
    dispatch(fetchCatalog());
  }, [dispatch]);

  const sellers = users.filter((u) => u.role === 'seller').length;
  const revenue = orders.filter((o) => o.status === 'paid').reduce((a, o) => a + o.total_amount, 0);

  const TABS = [
    { id: 'resumen', label: t('admin.tabs.summary') },
    { id: 'usuarios', label: t('admin.tabs.users') },
    { id: 'ordenes', label: t('admin.tabs.orders') },
  ];

  return (
    <main className="screen">
      <div className="admin-head">
        <div>
          <div className="eyebrow mono">{t('admin.eyebrow')}</div>
          <h1 className="screen-title admin-title">
            Back<em className="admin-title-em">office</em>
          </h1>
        </div>
        <Btn variant="ghost" onClick={() => navigate('/catalogo')}>{t('admin.backToStore')}</Btn>
      </div>

      <nav className="admin-tabs">
        {TABS.map((tb) => (
          <button key={tb.id} className={`admin-tab ${tab === tb.id ? 'is-active' : ''}`} onClick={() => setTab(tb.id)}>
            {tb.label}
          </button>
        ))}
      </nav>

      {tab === 'resumen' && (
        <section className="admin-metrics">
          <div className="metric">
            <div className="metric-num">{users.length}</div>
            <div className="metric-label">{t('admin.metrics.users')}</div>
          </div>
          <div className="metric">
            <div className="metric-num">{sellers}</div>
            <div className="metric-label">{t('admin.metrics.sellers')}</div>
          </div>
          <div className="metric">
            <div className="metric-num">{products.length}</div>
            <div className="metric-label">{t('admin.metrics.products')}</div>
          </div>
          <div className="metric">
            <div className="metric-num">{orders.length}</div>
            <div className="metric-label">{t('admin.metrics.orders')}</div>
          </div>
          <div className="metric metric-wide">
            <div className="metric-num">{fmtARS(revenue)}</div>
            <div className="metric-label">{t('admin.metrics.revenue')}</div>
          </div>
        </section>
      )}

      {tab === 'usuarios' && <AdminUsers />}
      {tab === 'ordenes' && <AdminOrders />}
    </main>
  );
}

const EMPTY_USER = { first_name: '', last_name: '', email: '', username: '', password: '', role: 'seller' };

function AdminUsers() {
  const dispatch = useDispatch();
  const { user: currentUser } = useAuth();
  const users = useSelector(selectUsers);
  const loading = useSelector(selectUsersLoading);
  const error = useSelector(selectUsersError);
  const mutating = useSelector(selectUsersMutating);
  const roles = t('admin.users.roles');
  const [form, setForm] = useState(EMPTY_USER);
  const [editingId, setEditingId] = useState(null);
  const [localError, setLocalError] = useState('');

  const isEditing = editingId !== null;

  const startEdit = (u) => {
    setEditingId(u.user_id);
    setForm({ first_name: u.first_name || '', last_name: u.last_name || '', email: u.email || '', username: u.username || '', password: '', role: u.role });
    setLocalError('');
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(EMPTY_USER);
    setLocalError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.first_name.trim() || !form.email.trim() || form.password.length < 8) return;
    setLocalError('');
    const body = {
      username: form.username.trim(),
      email: form.email.trim(),
      password: form.password,
      firstName: form.first_name.trim(),
      lastName: form.last_name.trim(),
      role: form.role,
      phone: null,
    };
    try {
      if (isEditing) {
        await dispatch(updateUser({ id: editingId, body })).unwrap();
      } else {
        await dispatch(createUser(body)).unwrap();
      }
      cancelEdit();
    } catch (err) {
      setLocalError(typeof err === 'string' ? err : 'No se pudo guardar el usuario.');
    }
  };

  const handleDelete = (u) => {
    if (!window.confirm(t('admin.users.deleteConfirm', { name: `${u.first_name} ${u.last_name}`.trim() || u.username }))) return;
    dispatch(deleteUser(u.user_id));
  };

  return (
    <div className="admin-users">
      <form className="admin-user-form" onSubmit={handleSubmit}>
        <h3 className="admin-user-form-title">{isEditing ? t('admin.users.editTitle') : t('admin.users.createTitle')}</h3>
        <div className="admin-user-form-grid">
          <Field label={t('admin.users.fName')}>
            <input className="input" value={form.first_name} onChange={(e) => setForm({ ...form, first_name: e.target.value })} />
          </Field>
          <Field label={t('admin.users.lName')}>
            <input className="input" value={form.last_name} onChange={(e) => setForm({ ...form, last_name: e.target.value })} />
          </Field>
          <Field label={t('admin.users.email')}>
            <input className="input mono" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </Field>
          <Field label={t('admin.users.username')}>
            <input className="input mono" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
          </Field>
          <Field label={t('admin.users.password')} hint={isEditing ? t('admin.users.passwordEditHint') : t('admin.users.passwordHint')}>
            <input className="input mono" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </Field>
          <Field label={t('admin.users.role')}>
            <select className="select" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
              <option value="buyer">{roles.buyer}</option>
              <option value="seller">{roles.seller}</option>
              <option value="admin">{roles.admin}</option>
            </select>
          </Field>
        </div>
        {(localError || error) && <span className="field-error"><TriangleAlert size={14} /> {localError || error}</span>}
        <div className="admin-user-form-actions">
          {isEditing && (
            <Btn variant="ghost" type="button" onClick={cancelEdit}>{t('admin.users.cancel')}</Btn>
          )}
          <Btn variant="primary" type="submit" disabled={mutating}>
            {mutating
              ? (isEditing ? t('admin.users.updating') : t('admin.users.creating'))
              : (isEditing ? t('admin.users.editSubmit') : t('admin.users.submit'))}
          </Btn>
        </div>
      </form>

      {loading && users.length === 0 ? (
        <div className="ticket-hint mono">{t('admin.users.loading')}</div>
      ) : users.length === 0 ? (
        <Empty title={t('admin.users.empty')} />
      ) : (
        <div className="admin-table">
          <div className="admin-row admin-row-head admin-row-users-mng">
            <span>{t('admin.users.colName')}</span>
            <span>{t('admin.users.colUsername')}</span>
            <span>{t('admin.users.colEmail')}</span>
            <span>{t('admin.users.colRole')}</span>
            <span>{t('admin.users.colActions')}</span>
          </div>
          {users.map((u) => (
            <div key={u.user_id} className="admin-row admin-row-users-mng">
              <span>{u.first_name} {u.last_name}</span>
              <span className="mono">@{u.username}</span>
              <span className="mono admin-attrs">{u.email}</span>
              <span><span className={`admin-pill role-${u.role}`}>{roles[u.role] || u.role}</span></span>
              <span className="admin-cell-actions">
                <Btn variant="ghost" size="sm" disabled={mutating} onClick={() => startEdit(u)}>
                  <Pencil size={14} /> {t('admin.users.edit')}
                </Btn>
                {u.user_id !== currentUser.user_id && (
                  <Btn variant="ghost" size="sm" disabled={mutating} onClick={() => handleDelete(u)}>
                    <Trash2 size={14} /> {t('admin.users.delete')}
                  </Btn>
                )}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function AdminOrders() {
  const orders = useSelector(selectOrders);
  const loading = useSelector(selectOrdersLoading);

  if (loading && orders.length === 0) return <div className="ticket-hint mono">{t('admin.orders.loading')}</div>;
  if (orders.length === 0) return <Empty title={t('admin.orders.empty')} />;

  return (
    <div className="admin-table">
      <div className="admin-row admin-row-head admin-row-all-orders">
        <span>{t('admin.orders.colOrder')}</span>
        <span>{t('admin.orders.colUser')}</span>
        <span>{t('admin.orders.colStatus')}</span>
        <span>{t('admin.orders.colTotal')}</span>
        <span>{t('admin.orders.colDate')}</span>
      </div>
      {orders.map((o) => (
        <div key={o.order_id} className="admin-row admin-row-all-orders">
          <span className="mono">#{o.order_id}</span>
          <span className="mono admin-attrs">{t('admin.orders.user', { id: o.user_id })}</span>
          <span><span className={`order-status order-status-${o.status}`}>{orderStatus(o.status)}</span></span>
          <span className="mono admin-order-total">{fmtARS(o.total_amount)}</span>
          <span className="mono admin-attrs">{new Date(o.created_at).toLocaleDateString('es-AR')}</span>
        </div>
      ))}
    </div>
  );
}

export default Admin;
