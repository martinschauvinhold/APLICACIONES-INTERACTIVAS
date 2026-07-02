import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TriangleAlert } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import AuthShell from '../components/AuthShell';
import Field from '../components/Field';
import Btn from '../components/Btn';
import { t } from '../i18n';

function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    username: '',
    password: '',
  });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const pwStrength = (() => {
    const p = form.password;
    if (p.length === 0) return null;
    if (p.length < 6)  return { level: 1, label: t('auth.register.strengthWeak'),   color: 'var(--danger)' };
    if (p.length < 10) return { level: 2, label: t('auth.register.strengthMedium'), color: 'var(--warn)' };
    return                    { level: 3, label: t('auth.register.strengthStrong'), color: 'var(--accent)' };
  })();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = {};
    if (form.first_name.trim().length < 2) errs.first_name = t('auth.register.errFirstName');
    if (form.last_name.trim().length < 2)  errs.last_name = t('auth.register.errLastName');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errs.email = t('auth.register.errEmail');
    if (form.username.length < 3) errs.username = t('auth.register.errUsername');
    if (form.password.length < 8) errs.password = t('auth.register.errPassword');
    setErrors(errs);
    if (Object.keys(errs).length) return;

    setSubmitting(true);
    try {
      await register(form);
      navigate('/');
    } catch (err) {
      setErrors({ form: err.message || t('auth.register.errGeneric') });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title={<>{t('auth.register.titlePre')} <em>{t('auth.register.titleEm')}</em></>}
      eyebrow={t('auth.register.eyebrow')}
      route="/register"
      switchTo="/login"
      switchLabel={t('auth.register.switchLabel')}
      switchHint={t('auth.register.switchHint')}
    >
      <form className="auth-form" onSubmit={handleSubmit}>

        <div className="auth-form-row">
          <Field label={t('auth.register.firstName')} error={errors.first_name}>
            <input className="input" value={form.first_name} onChange={(e) => setForm({ ...form, first_name: e.target.value })} placeholder={t('auth.register.firstNamePlaceholder')} />
          </Field>
          <Field label={t('auth.register.lastName')} error={errors.last_name}>
            <input className="input" value={form.last_name} onChange={(e) => setForm({ ...form, last_name: e.target.value })} placeholder={t('auth.register.lastNamePlaceholder')} />
          </Field>
        </div>

        <Field label={t('auth.register.email')} error={errors.email}>
          <input className="input mono" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder={t('auth.register.emailPlaceholder')} />
        </Field>

        <Field label={t('auth.register.username')} error={errors.username} hint={t('auth.register.usernameHint')}>
          <input
            className="input mono"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value.toLowerCase().replace(/\s/g, '') })}
            placeholder={t('auth.register.usernamePlaceholder')}
          />
        </Field>

        <Field label={t('auth.register.password')} error={errors.password}>
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

        <label className="check mono" style={{ fontSize: 11, padding: '4px 0' }}>
          <input type="checkbox" defaultChecked />
          {t('auth.terms.pre')} <a href="#" className="auth-link">{t('auth.terms.terms')}</a> {t('auth.terms.mid')} <a href="#" className="auth-link">{t('auth.terms.privacy')}</a>.
        </label>

        {errors.form && (
          <span className="field-error">
            <TriangleAlert size={13} /> {errors.form}
          </span>
        )}

        <Btn variant="primary" size="lg" className="w-full" disabled={submitting}>
          {submitting ? t('auth.register.submitting') : t('auth.register.submit')}
        </Btn>
      </form>
    </AuthShell>
  );
}

export default Register;
