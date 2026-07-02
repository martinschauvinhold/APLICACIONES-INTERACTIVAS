import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TriangleAlert } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import AuthShell from '../components/AuthShell';
import Field from '../components/Field';
import Btn from '../components/Btn';
import { t } from '../i18n';

/**
 * Login — pantalla de inicio de sesión contra el backend real.
 *
 * Hooks: useState (form/errores), useAuth (login), useNavigate (redirige).
 */
function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPw, setShowPw] = useState(false);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = {};
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errs.email = t('auth.login.errEmail');
    if (form.password.length < 4) errs.password = t('auth.login.errPassword');
    setErrors(errs);
    if (Object.keys(errs).length) return;

    setSubmitting(true);
    try {
      await login({ email: form.email, password: form.password });
      navigate('/');
    } catch (err) {
      setErrors({ form: err.message || t('auth.login.errGeneric') });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title={<>{t('auth.login.titlePre')} <em>{t('auth.login.titleEm')}</em></>}
      eyebrow={t('auth.login.eyebrow')}
      route="/login"
      switchTo="/register"
      switchLabel={t('auth.login.switchLabel')}
      switchHint={t('auth.login.switchHint')}
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        <Field label={t('auth.login.email')} error={errors.email}>
          <input
            className="input mono"
            type="email"
            autoFocus
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            placeholder={t('auth.login.emailPlaceholder')}
          />
        </Field>

        <Field label={t('auth.login.password')} error={errors.password}>
          <div className="input-with-action">
            <input
              className="input mono"
              type={showPw ? 'text' : 'password'}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder={t('auth.login.passwordPlaceholder')}
            />
            <button type="button" className="input-action mono" onClick={() => setShowPw(!showPw)}>
              {showPw ? t('auth.login.hide') : t('auth.login.show')}
            </button>
          </div>
        </Field>

        <div className="auth-row">
          <label className="check mono">
            <input type="checkbox" defaultChecked /> {t('auth.login.remember')}
          </label>
          <a href="#" className="auth-link">{t('auth.login.forgot')}</a>
        </div>

        {errors.form && (
          <span className="field-error">
            <TriangleAlert size={13} /> {errors.form}
          </span>
        )}

        <Btn variant="primary" size="lg" className="w-full" disabled={submitting}>
          {submitting ? t('auth.login.submitting') : t('auth.login.submit')}
        </Btn>
      </form>
    </AuthShell>
  );
}

export default Login;
