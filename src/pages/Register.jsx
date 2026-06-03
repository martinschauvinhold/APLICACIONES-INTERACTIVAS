import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import AuthShell from '../components/AuthShell';
import Field from '../components/Field';
import Btn from '../components/Btn';
import { register as registerAction } from '../store/slices/sessionSlice';
import { api } from '../api/client';

/**
 * Register — alta de usuario nuevo.
 *
 * Hooks: useState (form, errores, loading), useDispatch, useNavigate
 *
 * Demuestra:
 *   - validación de múltiples campos con feedback
 *   - medidor de fortaleza de contraseña
 *   - selector de rol (buyer / seller)
 *   - integración con backend (POST /auth/register o /users)
 */
function Register() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    username: '',
    password: '',
    role: 'buyer',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [serverErr, setServerErr] = useState(null);

  // Calcula fortaleza de contraseña
  const pwStrength = (() => {
    const p = form.password;
    if (p.length === 0) return null;
    if (p.length < 6)  return { level: 1, label: 'débil',  color: 'var(--danger)' };
    if (p.length < 10) return { level: 2, label: 'media',  color: 'var(--warn)' };
    return                    { level: 3, label: 'fuerte', color: 'var(--accent)' };
  })();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = {};
    if (form.first_name.trim().length < 2) errs.first_name = 'Mínimo 2 caracteres';
    if (form.last_name.trim().length < 2)  errs.last_name = 'Mínimo 2 caracteres';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errs.email = 'Email inválido';
    if (form.username.length < 3) errs.username = 'Mínimo 3 caracteres';
    if (form.password.length < 6) errs.password = 'Mínimo 6 caracteres';
    setErrors(errs);
    setServerErr(null);
    if (Object.keys(errs).length) return;

    setLoading(true);
    try {
      const payload = {
        username: form.username,
        email: form.email,
        passwordHash: form.password,
        firstName: form.first_name,
        lastName: form.last_name,
        role: form.role,
        phone: '',
      };
      let res;
      try {
        res = await api.auth.register(payload);
      } catch (err) {
        if (err.status === 404) res = await api.users.create(payload);
        else throw err;
      }
      if (res && res.token) api.config.setToken(res.token);
      dispatch(registerAction(form));
      navigate('/');
    } catch (err) {
      if (err.message === 'API_DISABLED') {
        await new Promise((r) => setTimeout(r, 400));
        dispatch(registerAction(form));
        navigate('/');
      } else if (err.status === 409) {
        setServerErr('Ese email o username ya está registrado');
      } else {
        setServerErr('No se pudo crear la cuenta. Probá de nuevo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      title={<>Creá tu <em>cuenta</em></>}
      eyebrow="CREAR CUENTA · GRATIS"
      route="/register"
      switchTo="/login"
      switchLabel="Iniciar sesión"
      switchHint="¿Ya tenés cuenta?"
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        {serverErr && <div className="auth-server-err">⚠ {serverErr}</div>}

        <div className="auth-form-row">
          <Field label="NOMBRE" error={errors.first_name}>
            <input className="input" value={form.first_name} onChange={(e) => setForm({ ...form, first_name: e.target.value })} placeholder="Martín" />
          </Field>
          <Field label="APELLIDO" error={errors.last_name}>
            <input className="input" value={form.last_name} onChange={(e) => setForm({ ...form, last_name: e.target.value })} placeholder="García" />
          </Field>
        </div>

        <Field label="EMAIL" error={errors.email}>
          <input className="input mono" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="vos@ejemplo.com" />
        </Field>

        <Field label="USERNAME" error={errors.username} hint="Cómo te verán otros usuarios">
          <input
            className="input mono"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value.toLowerCase().replace(/\s/g, '') })}
            placeholder="martin123"
          />
        </Field>

        <Field label="CONTRASEÑA" error={errors.password}>
          <input className="input mono" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="••••••••" />
          {pwStrength && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 6 }}>
              <div style={{ flex: 1, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 4 }}>
                {[1, 2, 3].map((l) => (
                  <div
                    key={l}
                    style={{
                      height: 4,
                      borderRadius: 99,
                      background: l <= pwStrength.level ? pwStrength.color : 'var(--bg-3)',
                      transition: 'background var(--t)',
                    }}
                  />
                ))}
              </div>
              <span className="mono" style={{ fontSize: 10, letterSpacing: '0.1em', textTransform: 'uppercase', color: pwStrength.color }}>
                {pwStrength.label}
              </span>
            </div>
          )}
        </Field>

        <Field label="TIPO DE CUENTA">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
            {[
              { v: 'buyer',  n: 'Comprador', s: 'Para uso personal' },
              { v: 'seller', n: 'Vendedor',  s: 'Vender en Vector · requiere validación' },
            ].map((r) => (
              <label
                key={r.v}
                style={{
                  display: 'grid', gridTemplateColumns: '20px 1fr', gap: 10,
                  padding: 'var(--pad)',
                  border: `1px solid ${form.role === r.v ? 'var(--accent)' : 'var(--border)'}`,
                  borderRadius: 'var(--radius)',
                  cursor: 'pointer',
                  background: form.role === r.v ? 'color-mix(in oklch, var(--accent) 6%, var(--bg))' : 'var(--bg)',
                }}
              >
                <input
                  type="radio"
                  name="role"
                  checked={form.role === r.v}
                  onChange={() => setForm({ ...form, role: r.v })}
                  style={{ accentColor: 'var(--accent)', marginTop: 4 }}
                />
                <div>
                  <div style={{ fontWeight: 600, fontSize: 13 }}>{r.n}</div>
                  <div className="mono" style={{ color: 'var(--fg-muted)', fontSize: 10, marginTop: 2 }}>{r.s}</div>
                </div>
              </label>
            ))}
          </div>
        </Field>

        <label className="check mono" style={{ fontSize: 11, padding: '4px 0' }}>
          <input type="checkbox" defaultChecked />
          Acepto los <a href="#" className="auth-link">términos</a> y la <a href="#" className="auth-link">política de privacidad</a>.
        </label>

        <Btn variant="primary" size="lg" className="w-full" disabled={loading}>
          {loading ? 'Creando cuenta…' : 'Crear cuenta →'}
        </Btn>
      </form>
    </AuthShell>
  );
}

export default Register;
