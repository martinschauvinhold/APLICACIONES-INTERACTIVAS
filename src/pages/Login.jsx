import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import AuthShell from '../components/AuthShell';
import Field from '../components/Field';
import Btn from '../components/Btn';
import { login as loginAction, loginAs } from '../store/slices/sessionSlice';
import { api, resolveRole } from '../api/client';

/**
 * Login — pantalla de inicio de sesión.
 *
 * Hooks:
 *   - useState: estado del formulario, mostrar password, errores, loading
 *   - useDispatch: despacha login al store al autenticar
 *   - useNavigate: redirige al catálogo
 *
 * Demuestra:
 *   - validación client-side antes de la API
 *   - integración con backend (POST /auth/login) + fallback demo
 *   - manejo de errores del servidor
 */
function Login() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: 'martin@mail.com', password: '' });
  const [showPw, setShowPw] = useState(false);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [serverErr, setServerErr] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = {};
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errs.email = 'Email inválido';
    if (form.password.length < 4) errs.password = 'Mínimo 4 caracteres';
    setErrors(errs);
    setServerErr(null);
    if (Object.keys(errs).length) return;

    setLoading(true);
    try {
      const res = await api.auth.login(form.email, form.password);
      if (res && res.token) api.config.setToken(res.token);
      // El login del backend solo devuelve { token }; el rol se resuelve aparte
      // (claim del JWT o /users/me). Si no se puede, default buyer.
      const role = (res && res.token) ? (await resolveRole(res.token)) || 'buyer' : 'buyer';
      dispatch(loginAction({ email: form.email, role }));
      navigate('/');
    } catch (err) {
      if (err.message === 'API_DISABLED') {
        // Modo demo: login local
        await new Promise((r) => setTimeout(r, 400));
        dispatch(loginAction({ email: form.email }));
        navigate('/');
      } else {
        setServerErr(err.status === 401 ? 'Email o contraseña incorrectos' : 'No se pudo conectar al servidor');
      }
    } finally {
      setLoading(false);
    }
  };

  // Demo: entrar directamente con un rol (sin backend). El registro del
  // backend siempre crea buyer, así que esto permite ver el panel admin.
  const enterAs = (role) => {
    dispatch(loginAs({ role }));
    navigate(role === 'admin' ? '/admin' : '/');
  };

  return (
    <AuthShell
      title={<>Iniciá <em>sesión</em></>}
      eyebrow="ACCESO · BIENVENIDO/A"
      route="/login"
      switchTo="/register"
      switchLabel="Crear cuenta"
      switchHint="¿No tenés cuenta?"
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        {serverErr && <div className="auth-server-err">⚠ {serverErr}</div>}

        <Field label="EMAIL" error={errors.email}>
          <input
            className="input mono"
            type="email"
            autoFocus
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            placeholder="vos@ejemplo.com"
          />
        </Field>

        <Field label="CONTRASEÑA" error={errors.password}>
          <div style={{ position: 'relative' }}>
            <input
              className="input mono"
              type={showPw ? 'text' : 'password'}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="••••••••"
              style={{ paddingRight: 70 }}
            />
            <button
              type="button"
              onClick={() => setShowPw(!showPw)}
              style={{
                position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)',
                color: 'var(--fg-muted)', fontSize: 11, padding: '4px 8px',
                fontFamily: 'var(--font-mono)',
              }}
            >{showPw ? 'ocultar' : 'ver'}</button>
          </div>
        </Field>

        <div className="auth-row">
          <label className="check mono">
            <input type="checkbox" defaultChecked /> recordarme en este dispositivo
          </label>
          <a href="#" className="auth-link">¿olvidaste tu contraseña?</a>
        </div>

        <Btn variant="primary" size="lg" className="w-full" disabled={loading}>
          {loading ? 'Autenticando…' : 'Iniciar sesión →'}
        </Btn>

        <div className="auth-demo">
          <div className="auth-demo-h mono">— o entrá como (demo) —</div>
          <div className="auth-demo-btns">
            <button type="button" className="auth-demo-btn" onClick={() => enterAs('buyer')}>
              👤 Comprador
            </button>
            <button type="button" className="auth-demo-btn is-admin" onClick={() => enterAs('admin')}>
              🛠️ Administrador
            </button>
          </div>
        </div>
      </form>
    </AuthShell>
  );
}

export default Login;
