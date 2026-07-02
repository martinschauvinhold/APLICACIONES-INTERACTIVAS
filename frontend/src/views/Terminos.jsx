import { t } from '../i18n';

function Terminos() {
  const secciones = t('info.terms.sections');
  return (
    <main className="screen">
      <div className="eyebrow mono">{t('info.legalEyebrow')}</div>
      <h1 className="screen-title">{t('info.terms.title')} <em>{t('info.terms.titleEm')}</em></h1>
      <p className="mono doc-meta">{t('common.lastUpdated')}</p>

      <div className="doc">
        {secciones.map((s) => (
          <section key={s.titulo} className="doc-section">
            <h2>{s.titulo}</h2>
            <p>{s.texto}</p>
          </section>
        ))}
      </div>
    </main>
  );
}

export default Terminos;
