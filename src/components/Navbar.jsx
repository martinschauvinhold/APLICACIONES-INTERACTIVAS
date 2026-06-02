import { Link, NavLink } from 'react-router-dom'
import { Search, ShoppingCart } from 'lucide-react'

// Barra de navegación principal de la tienda.
// Recibe la cantidad de items del carrito por props (cartCount).
export default function Navbar({ cartCount = 0 }) {
  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand">
          <span className="diamond" />
          Vector / tech
        </Link>

        <div className="search">
          <Search size={16} />
          <input type="text" placeholder="Buscar productos, marcas..." />
        </div>

        <div className="nav-links">
          <NavLink to="/productos">Novedades</NavLink>
          <NavLink to="/productos">Ofertas</NavLink>
          <NavLink to="/productos">Mayorista</NavLink>
          <NavLink to="/mis-pedidos">Mi cuenta</NavLink>
          <NavLink to="/contacto">Contacto</NavLink>
        </div>

        <Link to="/carrito" className={`cart-pill ${cartCount === 0 ? 'empty' : ''}`}>
          <ShoppingCart size={16} /> {cartCount}
        </Link>
      </div>
    </nav>
  )
}
