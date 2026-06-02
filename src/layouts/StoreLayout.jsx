import { Outlet } from 'react-router-dom'
import { Package, ShoppingCart } from 'lucide-react'
import LiveTicker from '../components/LiveTicker.jsx'
import Navbar from '../components/Navbar.jsx'
import Footer from '../components/Footer.jsx'

// Layout compartido por las vistas de la tienda:
// ticker + navbar + contenido (Outlet) + footer.
// Recibe el carrito por props para mostrar el contador en el Navbar.
export default function StoreLayout({ cart }) {
  return (
    <>
      <LiveTicker
        messages={[
          { Icon: Package, text: 'Envío #4827 despachado hace 4 min' },
          { Icon: ShoppingCart, text: 'Nuevo pedido mayorista recibido' },
        ]}
      />
      <Navbar cartCount={cart.count} />
      <main className="container">
        <Outlet />
      </main>
      <Footer />
    </>
  )
}
