import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate, NavLink, Link, useLocation } from 'react-router-dom';
import { setFilter } from '../store/slices/catalogSlice';
import { toggleCartDrawer } from '../store/slices/uiSlice';
import { selectCartTotals } from '../store/selectors';
import { subscribeStatus } from '../api/client';

const TICKER_MESSAGES = [
  '🛒 Alguien en Córdoba compró ThinkPad X1 Carbon · hace 2 min',
  '📦 Envío #4827 despachado · hace 4 min',
  '⚡ Nueva oferta: ENVIOGRATIS hasta el viernes',
  '📈 Restock: Galaxy S24 Ultra disponible nuevamente',
  '🛒 Alguien en Rosario compró MX Keys S · hace 8 min',
  '🏷️ Aplicá TECH10 al pagar y ahorrá 10%',
  '📦 Pedido #4625 entregado · hace 11 min',
  '⭐ Nueva reseña 5★ en MacBook Air M3',
];

// Live activity ticker
function LiveTicker() {
  const [idx, setIdx] = useState(0);
  const [apiStatus, setApiStatus] = useState('idle');

  useEffect(() => {
    const t = setInterval(() => setIdx((i) => (i + 1) % TICKER_MESSAGES.length), 4000);
    return () => clearInterval(t);
  }, []);

  useEffect(() => subscribeStatus((s) => setApiStatus(s)), []);

  const statusMap = {
    idle:     { label: 'Conectando…',       cls: 'is-idle' },
    ok:       { label: 'Backend conectado', cls: 'is-ok' },
    error:    { label: 'Sin conexión',      cls: 'is-error' },
    disabled: { label: 'Modo demo',         cls: 'is-off' },
  };
  const badge = statusMap[apiStatus] || statusMap.idle;

  return (
    <div className="ticker">
      <div className="ticker-inner">
        <span className="ticker-tag">EN VIVO</span>
        <span className="ticker-msg">{TICKER_MESSAGES[idx]}</span>
        <div className={`api-badge ${badge.cls}`}>
          <span className="api-badge-dot"></span>
          {badge.label}
        </div>
      </div>
    </div>
  );
}

// Botón de categoría en la nav
function CatBtn({ id, label }) {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const current = useSelector((s) => s.catalog.filters.categoryId);
  const isActive = current === id;
  const handleClick = () => {
    dispatch(setFilter({ categoryId: id }));
    navigate('/');
  };
  return (
    <button className={`nav-cat ${isActive ? 'is-active' : ''}`} onClick={handleClick}>
      {label}
    </button>
  );
}

/**
 * Nav — header de la app con búsqueda, categorías y carrito.
 * Usa react-router-dom (Link, NavLink, useNavigate, useLocation)
 * y Redux para el state global.
 */
function Nav() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const totals = useSelector(selectCartTotals);
  const search = useSelector((s) => s.catalog.filters.search);
  const user = useSelector((s) => s.session.user);
  const role = useSelector((s) => s.session.user.role);
  const categories = useSelector((s) => s.catalog.categories);

  const handleSearchFocus = () => { if (location.pathname !== '/') navigate('/'); };
  const handleSearchChange = (e) => dispatch(setFilter({ search: e.target.value }));
  const clearSearch = () => dispatch(setFilter({ search: '' }));

  return (
    <>
      <LiveTicker />
      <header className="nav">
        <div className="nav-row">
          <Link to="/" className="nav-brand">
            <span className="nav-brand-mark"></span>
            <span>Vector</span>
            <span className="nav-brand-tag">/ tech</span>
          </Link>

          <div className="nav-search">
            <span className="nav-search-prefix">{'>'}</span>
            <input
              type="text"
              placeholder="Buscar productos, marcas, sku…"
              value={search}
              onFocus={handleSearchFocus}
              onChange={handleSearchChange}
            />
            {search && (
              <button className="nav-search-clear" onClick={clearSearch}>×</button>
            )}
          </div>

          <nav className="nav-actions">
            <NavLink to="/pedidos" className="nav-link">Mis pedidos</NavLink>
            {(role === 'admin' || role === 'seller') && (
              <NavLink to="/admin" className="nav-link nav-link-admin">admin</NavLink>
            )}
            <div className="nav-user">
              <span className="nav-user-dot"></span>
              {user.first_name.toLowerCase()}@
            </div>
            <Link to="/login" className="nav-link">login</Link>
            <button className="nav-cart" onClick={() => dispatch(toggleCartDrawer())}>
              <span>CARRITO</span>
              <span className="nav-cart-count">{totals.itemCount}</span>
            </button>
          </nav>
        </div>

        <div className="nav-row nav-row-cats">
          <div className="nav-cats">
            <CatBtn id={null} label="Todos" />
            {categories.map((c) => <CatBtn key={c.id} id={c.id} label={c.name} />)}
          </div>
        </div>
      </header>
    </>
  );
}

export default Nav;
