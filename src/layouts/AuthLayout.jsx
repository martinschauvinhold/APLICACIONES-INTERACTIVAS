import { Outlet, useLocation } from 'react-router-dom'

// Layout partido para Login y Registro: panel oscuro a la izquierda
// (cambia su mensaje según la ruta) y formulario a la derecha (Outlet).
export default function AuthLayout() {
  const { pathname } = useLocation()
  const isRegister = pathname === '/registro'

  return (
    <div className="auth">
      <aside className="auth-side">
        <div className="brand">
          <span className="diamond" />
          Vector / tech
        </div>

        {isRegister ? (
          <>
            <h1>
              Creá tu
              <br />
              cuenta hoy. <span className="accent">hoy.</span>
            </h1>
            <p className="sub">
              Accedé a miles de productos tech con precios mayoristas y minoristas.
            </p>
            <ul>
              <li>Precios mayoristas desde 5 unidades</li>
              <li>Seguimiento de pedidos en tiempo real</li>
              <li>Cupones de descuento exclusivos</li>
            </ul>
          </>
        ) : (
          <>
            <h1>
              Tecnología
              <br />
              que funciona. <span className="accent">funciona.</span>
            </h1>
            <p className="sub">
              E-commerce de productos tecnológicos en modalidad minorista y mayorista.
            </p>
          </>
        )}
      </aside>

      <section className="auth-main">
        <Outlet />
      </section>
    </div>
  )
}
