import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

// Vista de inicio de sesión. Maneja el estado del formulario con useState.
export default function Login() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)

  function handleSubmit(e) {
    e.preventDefault()
    // Maqueta: no hay backend, navegamos al catálogo.
    navigate('/')
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <span className="eyebrow">ACCESO A TU CUENTA</span>
      <h2>Iniciar sesión</h2>
      <p className="desc">Ingresá tus credenciales para continuar.</p>

      <div className="field">
        <label>EMAIL</label>
        <input
          type="email"
          placeholder="tu@email.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </div>

      <div className="field">
        <label>CONTRASEÑA</label>
        <input
          type="password"
          placeholder="••••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>

      <div className="form-aux">
        <label>
          <input
            type="checkbox"
            checked={remember}
            onChange={(e) => setRemember(e.target.checked)}
          />
          Recordarme
        </label>
        <a href="#" className="link-green">
          ¿Olvidaste tu contraseña?
        </a>
      </div>

      <button type="submit" className="btn btn-dark btn-block">
        Ingresar →
      </button>

      <div className="divider">o continuá con</div>
      <div className="social-row">
        <button type="button" className="btn btn-outline">
          Google
        </button>
        <button type="button" className="btn btn-outline">
          GitHub
        </button>
      </div>

      <p className="auth-bottom">
        ¿No tenés cuenta? <Link to="/registro" className="link-green">Registrate</Link>
      </p>
    </form>
  )
}
