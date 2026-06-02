import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import RoleToggle from '../components/RoleToggle.jsx'

// Calcula la fortaleza de la contraseña (0 a 3) a partir de su longitud/variedad.
function passwordStrength(pw) {
  let score = 0
  if (pw.length >= 6) score++
  if (pw.length >= 10) score++
  if (/[0-9]/.test(pw) && /[a-zA-Z]/.test(pw)) score++
  return score
}
const strengthLabels = ['Débil', 'Débil', 'Media', 'Fuerte']

// Vista de registro. Varios estados con useState: datos, rol y términos.
export default function Registro() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    nombre: '',
    apellido: '',
    email: '',
    usuario: '',
    password: '',
  })
  const [role, setRole] = useState('COMPRADOR')
  const [accepted, setAccepted] = useState(false)

  // Actualiza un campo del formulario manteniendo el resto.
  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    navigate('/')
  }

  const strength = passwordStrength(form.password)

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <span className="eyebrow">CREAR CUENTA</span>
      <h2>Registro</h2>
      <p className="desc">Completá tus datos para crear tu cuenta.</p>

      <div className="field-row">
        <div className="field">
          <label>NOMBRE</label>
          <input
            placeholder="Juan"
            value={form.nombre}
            onChange={(e) => update('nombre', e.target.value)}
          />
        </div>
        <div className="field">
          <label>APELLIDO</label>
          <input
            placeholder="Pérez"
            value={form.apellido}
            onChange={(e) => update('apellido', e.target.value)}
          />
        </div>
      </div>

      <div className="field">
        <label>EMAIL</label>
        <input
          type="email"
          placeholder="tu@email.com"
          value={form.email}
          onChange={(e) => update('email', e.target.value)}
        />
      </div>

      <div className="field">
        <label>USUARIO</label>
        <input
          placeholder="@juanperez"
          value={form.usuario}
          onChange={(e) => update('usuario', e.target.value)}
        />
      </div>

      <div className="field">
        <label>CONTRASEÑA</label>
        <input
          type="password"
          placeholder="••••••••"
          value={form.password}
          onChange={(e) => update('password', e.target.value)}
        />
        {form.password && (
          <div className="strength">
            <span>Fortaleza:</span>
            <div className="bars">
              {[0, 1, 2].map((i) => (
                <div key={i} className={`bar ${i < strength ? 'on' : ''}`} />
              ))}
            </div>
            <span className="label">{strengthLabels[strength]}</span>
          </div>
        )}
      </div>

      <div className="field">
        <label>ROL</label>
        <RoleToggle value={role} onChange={setRole} />
      </div>

      <div className="form-aux" style={{ justifyContent: 'flex-start' }}>
        <label>
          <input
            type="checkbox"
            checked={accepted}
            onChange={(e) => setAccepted(e.target.checked)}
          />
          Acepto los Términos y condiciones y la Política de privacidad
        </label>
      </div>

      <button type="submit" className="btn btn-dark btn-block" disabled={!accepted}>
        Crear cuenta →
      </button>

      <p className="auth-bottom">
        ¿Ya tenés cuenta? <Link to="/login" className="link-green">Iniciar sesión</Link>
      </p>
    </form>
  )
}
