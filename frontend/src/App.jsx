import { useEffect } from "react";
import { Routes, Route, useLocation, Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { selectIsAuthenticated, selectUser } from "./features/auth/authSlice";
import Nav from "./components/Nav";
import Footer from "./components/Footer";
import CartDrawer from "./components/CartDrawer";
import QuickViewModal from "./components/QuickViewModal";
import Catalog from "./views/Catalog";
import Product from "./views/Product";
import Cart from "./views/Cart";
import Checkout from "./views/Checkout";
import Confirmation from "./views/Confirmation";
import Orders from "./views/Orders";
import Login from "./views/Login";
import Register from "./views/Register";
import Admin from "./views/Admin";
import Vendedor from "./views/Vendedor";
import SobreNosotros from "./views/SobreNosotros";
import Soporte from "./views/Soporte";
import Direcciones from "./views/Direcciones";
import Devoluciones from "./views/Devoluciones";
import Terminos from "./views/Terminos";
import Privacidad from "./views/Privacidad";

function RequireAdmin({ children }) {
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectUser);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== "admin") return <Navigate to="/" replace />;
  return children;
}

function RequireSeller({ children }) {
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectUser);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== "seller") return <Navigate to="/" replace />;
  return children;
}

function RequireBuyer({ children }) {
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectUser);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== "buyer") return <Navigate to="/" replace />;
  return children;
}

function Home() {
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectUser);
  if (isAuthenticated && user?.role === "admin") return <Navigate to="/admin" replace />;
  if (isAuthenticated && user?.role === "seller") return <Navigate to="/vendedor" replace />;
  return <Catalog />;
}

function App() {
  const location = useLocation();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location.pathname]);

  const isAuth =
    location.pathname === "/login" || location.pathname === "/register";

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
        <Route path="/" element={<Home />} />
        <Route path="/catalogo" element={<Catalog />} />
        <Route path="/producto/:id" element={<Product />} />
        <Route
          path="/carrito"
          element={
            <RequireBuyer>
              <Cart />
            </RequireBuyer>
          }
        />
        <Route
          path="/checkout"
          element={
            <RequireBuyer>
              <Checkout />
            </RequireBuyer>
          }
        />
        <Route
          path="/confirmacion/:id"
          element={
            <RequireBuyer>
              <Confirmation />
            </RequireBuyer>
          }
        />
        <Route
          path="/pedidos"
          element={
            <RequireBuyer>
              <Orders />
            </RequireBuyer>
          }
        />
        <Route
          path="/admin"
          element={
            <RequireAdmin>
              <Admin />
            </RequireAdmin>
          }
        />
        <Route
          path="/vendedor"
          element={
            <RequireSeller>
              <Vendedor />
            </RequireSeller>
          }
        />
        <Route path="/sobre-nosotros" element={<SobreNosotros />} />
        <Route path="/soporte" element={<Soporte />} />
        <Route path="/direcciones" element={<Direcciones />} />
        <Route path="/devoluciones" element={<Devoluciones />} />
        <Route path="/terminos" element={<Terminos />} />
        <Route path="/privacidad" element={<Privacidad />} />
        <Route path="*" element={<Home />} />
      </Routes>
      <Footer />
      <CartDrawer />
      <QuickViewModal />
    </>
  );
}

export default App;
