import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';

/**
 * AuthShell — layout compartido para Login y Register.
 *
 * Props:
 *   title, eyebrow, route, children (form),
 *   switchTo (ruta del switch), switchLabel, switchHint
 */
function AuthShell({ title, eyebrow, route, children, switchTo, switchLabel, switchHint }) {
  const navigate = useNavigate();
  return (
    <main className="screen screen-auth">
      <div className="auth-grid">
        <aside className="auth-pitch">
          <div className="auth-pitch-mark">
            <span className="auth-pitch-mark-glyph"></span>
            <span>Vector / tech</span>
            <span className="auth-pitch-mark-tag">/ acceso</span>
          </div>

          <div>
            <div className="eyebrow mono">
              <span style={{
                display: 'inline-block',
                width: 8,
                height: 8,
                borderRadius: 99,
                background: 'var(--accent)',
                marginRight: 10,
              }}></span>
              {eyebrow}
            </div>
            <h1 className="auth-pitch-h">Tecnología <em>seria</em>,<br />al alcance de un click.</h1>
            <p className="auth-pitch-sub">
              Curaduría profesional, precios mayoristas y despacho en 24 horas. Cuenta gratis, sin spam.
            </p>
          </div>

          <ul className="auth-pitch-list">
            <li><b>▸ 24h</b><span>Despacho garantizado en CABA + GBA</span></li>
            <li><b>▸ B2B</b><span>Precios mayoristas automáticos por cantidad</span></li>
            <li><b>▸ 30d</b><span>Devolución sin cargo</span></li>
            <li><b>▸ ★</b><span>Vendedores oficiales verificados</span></li>
          </ul>

          <div className="auth-pitch-foot">VECTOR.TECH · 2026</div>
        </aside>

        <section className="auth-card">
          <div className="auth-card-inner">
            <header className="auth-card-head">
              <button className="auth-back" onClick={() => navigate('/')}>← Volver al catálogo</button>
              <span className="auth-route">{route}</span>
            </header>
            <div className="auth-card-body">
              <h2 className="auth-card-h">{title}</h2>
              {children}
            </div>
            <footer className="auth-card-foot">
              <span>{switchHint}</span>
              <button className="auth-switch" onClick={() => navigate(switchTo)}>{switchLabel} →</button>
            </footer>
          </div>
        </section>
      </div>
    </main>
  );
}

export default AuthShell;
