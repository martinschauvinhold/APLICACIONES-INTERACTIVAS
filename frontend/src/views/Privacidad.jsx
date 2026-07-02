import { t } from '../i18n';

function Privacidad() {
  const secciones = t('info.privacy.sections');
  return (
    <main className="screen">
      <div className="eyebrow mono">{t('info.legalEyebrow')}</div>
      <h1 className="screen-title">{t('info.privacy.title')} <em>{t('info.privacy.titleEm')}</em></h1>
      <p className="mono doc-meta">{t('common.lastUpdated')}</p>

      <div className="doc">
        {secciones.map((s) => (
          <section key={s.titulo} className="doc-section">
            <h2>{s.titulo}</h2>
            <p>{s.texto}</p>
          </section>
        ))}
        <div className="mono doc-note">{t('info.privacy.contact')}</div>
      </div>
    </main>
  );
}

export default Privacidad;
