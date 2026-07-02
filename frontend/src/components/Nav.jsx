import { useState, useEffect } from 'react';
import { useNavigate, NavLink, Link, useLocation } from 'react-router-dom';
import { ShoppingCart, Package, Zap, TrendingUp, Tag, Star } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { useUI } from '../hooks/useUI';
import { useCatalog } from '../hooks/useCatalog';
import { t } from '../i18n';

const TICKER_ICONS = [ShoppingCart, Package, Zap, TrendingUp, ShoppingCart, Tag, Package, Star];
const TICKER_INTERVAL_MS = 4000;

function LiveTicker() {
  const [idx, setIdx] = useState(0);
  const messages = t('nav.ticker');

  useEffect(() => {
    const timer = setInterval(() => setIdx((i) => (i + 1) % messages.length), TICKER_INTERVAL_MS);
    return () => clearInterval(timer);
  }, [messages.length]);

  const Icon = TICKER_ICONS[idx] || ShoppingCart;

  return (
    <div className="ticker">
      <div className="ticker-inner">
        <span className="ticker-tag">{t('nav.live')}</span>
        <span className="ticker-msg">
          <Icon size={14} className="ticker-icon" />
          {messages[idx]}
        </span>
      </div>
    </div>
  );
}

function CatBtn({ id, label }) {
  const { filters, setFilter } = useCatalog();
  const navigate = useNavigate();
  const isActive = filters.categoryId === id;
  const handleClick = () => {
    setFilter({ categoryId: id });
    navigate('/catalogo');
  };
  return (
    <button className={`nav-cat ${isActive ? 'is-active' : ''}`} onClick={handleClick}>
      {label}
    </button>
  );
}

function Nav() {
  const { user, role, isAuthenticated, logout } = useAuth();
  const { count } = useCart();
  const { toggleCartDrawer } = useUI();
  const { filters, categories, setFilter } = useCatalog();
  const navigate = useNavigate();
  const location = useLocation();

  const isSeller = isAuthenticated && role === 'seller';
  const isAdmin = isAuthenticated && role === 'admin';
  const canBuy = isAuthenticated && role === 'buyer';
  const isShopper = !isAuthenticated || role === 'buyer'; 

  const handleSearchFocus = () => {
    if (location.pathname !== '/' && location.pathname !== '/catalogo') navigate('/catalogo');
  };
  const handleSearchChange = (e) => setFilter({ search: e.target.value });
  const clearSearch = () => setFilter({ search: '' });
  const handleLogout = () => { logout(); navigate('/'); };

  return (
    <>
      {isSeller && <LiveTicker />}
      <header className="nav">
        <div className="nav-row">
          <Link to="/" className="nav-brand">
            <span className="nav-brand-mark"></span>
            <span>{t('nav.brand')}</span>
            <span className="nav-brand-tag">{t('nav.brandTag')}</span>
          </Link>

          <div className="nav-search">
            <span className="nav-search-prefix">{'>'}</span>
            <input
              type="text"
              placeholder={t('nav.searchPlaceholder')}
              value={filters.search}
              onFocus={handleSearchFocus}
              onChange={handleSearchChange}
            />
            {filters.search && (
              <button className="nav-search-clear" onClick={clearSearch}>×</button>
            )}
          </div>

          <nav className="nav-actions">
            {canBuy && (
              <NavLink to="/pedidos" className="nav-link">{t('nav.orders')}</NavLink>
            )}
            {isAdmin && (
              <NavLink to="/admin" className="nav-backoffice">{t('nav.panel')}</NavLink>
            )}
            {isSeller && (
              <NavLink to="/vendedor" className="nav-backoffice">{t('nav.myProducts')}</NavLink>
            )}
            {isAuthenticated ? (
              <>
                <div className="nav-user">
                  <span className="nav-user-dot"></span>
                  {user.first_name.toLowerCase()}@
                </div>
                <button className="nav-link" onClick={handleLogout}>{t('nav.logout')}</button>
              </>
            ) : (
              <Link to="/login" className="nav-link">{t('nav.login')}</Link>
            )}
            {/* Solo el comprador ve el carrito */}
            {canBuy && (
              <button className="nav-cart" onClick={() => toggleCartDrawer()}>
                <span>{t('nav.cart')}</span>
                <span className="nav-cart-count">{count}</span>
              </button>
            )}
          </nav>
        </div>

        {isShopper && (
          <div className="nav-row nav-row-cats">
            <div className="nav-cats">
              <CatBtn id={null} label={t('nav.allCategories')} />
              {categories.map((c) => <CatBtn key={c.id} id={c.id} label={c.name} />)}
            </div>
          </div>
        )}
      </header>
    </>
  );
}

export default Nav;
