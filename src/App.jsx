import { BrowserRouter, Routes, Route } from 'react-router-dom'

import StoreLayout from './layouts/StoreLayout.jsx'
import AuthLayout from './layouts/AuthLayout.jsx'

import Login from './pages/Login.jsx'
import Registro from './pages/Registro.jsx'
import Catalogo from './pages/Catalogo.jsx'
import DetalleProducto from './pages/DetalleProducto.jsx'
import Carrito from './pages/Carrito.jsx'
import Checkout from './pages/Checkout.jsx'
import Confirmacion from './pages/Confirmacion.jsx'
import MisPedidos from './pages/MisPedidos.jsx'
import Contacto from './pages/Contacto.jsx'

import { useCart } from './hooks/useCart.js'

// Componente raíz: define TODAS las rutas con React Router DOM.
// El estado del carrito vive acá (useCart) y se baja por props a las vistas.
export default function App() {
  const cart = useCart()

  return (
    <BrowserRouter>
      <Routes>
        {/* --- Rutas de autenticación (layout partido, sin navbar de tienda) --- */}
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />
        </Route>

        {/* --- Rutas de la tienda (comparten Navbar + Footer + ticker) --- */}
        <Route element={<StoreLayout cart={cart} />}>
          <Route path="/" element={<Catalogo cart={cart} />} />
          <Route path="/productos" element={<Catalogo cart={cart} />} />
          <Route path="/detalle/:id" element={<DetalleProducto cart={cart} />} />
          <Route path="/carrito" element={<Carrito cart={cart} />} />
          <Route path="/mis-pedidos" element={<MisPedidos />} />
          <Route path="/contacto" element={<Contacto />} />
        </Route>

        {/* --- Rutas con cabecera propia --- */}
        <Route path="/checkout" element={<Checkout cart={cart} />} />
        <Route path="/confirmacion" element={<Confirmacion />} />
      </Routes>
    </BrowserRouter>
  )
}
