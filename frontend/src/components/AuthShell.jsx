import { useNavigate } from 'react-router-dom';
import { t } from '../i18n';

function AuthShell({ title, eyebrow, route, children, switchTo, switchLabel, switchHint }) {
  const navigate = useNavigate();
  const pitchList = t('auth.shell.pitchList');
  return (
    <main className="screen screen-auth">
      <div className="auth-grid">
        <aside className="auth-pitch">
          <div className="auth-pitch-mark">
            <span className="auth-pitch-mark-glyph"></span>
            <span>{t('auth.shell.brand')}</span>
            <span className="auth-pitch-mark-tag">{t('auth.shell.brandTag')}</span>
          </div>

          <div>
            <div className="eyebrow mono">
              <span className="eyebrow-dot"></span>
              {eyebrow}
            </div>
            <h1 className="auth-pitch-h">
              {t('auth.shell.pitchTitlePre')} <em>{t('auth.shell.pitchTitleEm')}</em>,<br />
              {t('auth.shell.pitchTitlePost')}
            </h1>
            <p className="auth-pitch-sub">{t('auth.shell.pitchSub')}</p>
          </div>

          <ul className="auth-pitch-list">
            {pitchList.map((item) => (
              <li key={item.k}>
                <b>▸ {item.k}</b>
                <span>{item.v}</span>
              </li>
            ))}
          </ul>

          <div className="auth-pitch-foot">{t('auth.shell.foot')}</div>
        </aside>

        <section className="auth-card">
          <div className="auth-card-inner">
            <header className="auth-card-head">
              <button className="auth-back" onClick={() => navigate('/')}>{t('common.backToCatalog')}</button>
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
