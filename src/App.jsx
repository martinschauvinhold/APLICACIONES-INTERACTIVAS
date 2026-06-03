import { Routes, Route, useLocation, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Nav from './components/Nav';
import Footer from './components/Footer';
import CartDrawer from './components/CartDrawer';
import QuickViewModal from './components/QuickViewModal';
import Catalog from './pages/Catalog';
import Product from './pages/Product';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import Confirmation from './pages/Confirmation';
import Orders from './pages/Orders';
import Login from './pages/Login';
import Register from './pages/Register';
import Admin from './pages/Admin';

/**
 * RequireAdmin — guard de ruta: solo deja pasar a admin/seller.
 * Si un comprador entra a /admin a mano, lo redirige al catálogo.
 */
function RequireAdmin({ children }) {
  const role = useSelector((s) => s.session.user.role);
  if (role !== 'admin' && role !== 'seller') return <Navigate to="/" replace />;
  return children;
}

/**
 * App — define el ruteo de toda la aplicación con React Router DOM.
 *
 * Las pantallas de auth (login / register) son full-screen: no muestran
 * Nav ni Footer. El resto comparte el layout (Nav + Footer + drawers).
 */
function App() {
  const location = useLocation();
  const isAuth = location.pathname === '/login' || location.pathname === '/register';

  if (isAuth) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Routes>
    );
  }

  return (
    <>
      <Nav />
      <Routes>
        <Route path="/" element={<Catalog />} />
        <Route path="/producto/:id" element={<Product />} />
        <Route path="/carrito" element={<Cart />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route path="/confirmacion/:id" element={<Confirmation />} />
        <Route path="/pedidos" element={<Orders />} />
        <Route path="/admin" element={<RequireAdmin><Admin /></RequireAdmin>} />
        <Route path="*" element={<Catalog />} />
      </Routes>
      <Footer />
      <CartDrawer />
      <QuickViewModal />
    </>
  );
}

export default App;
