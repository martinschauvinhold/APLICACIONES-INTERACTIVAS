import { t } from '../i18n';

/**
 * TrustBand — banda con 4 promesas debajo del catálogo.
 * Los textos vienen del diccionario i18n y los estilos de index.css (.trust-band*).
 */
function TrustBand() {
  const cells = t('trust.cells');
  return (
    <div className="trust-band">
      {cells.map((cell) => (
        <div key={cell.label} className="trust-cell">
          <div className="trust-num">
            <em>{cell.num}</em>
          </div>
          <div className="trust-label">{cell.label}</div>
          <div className="trust-sub mono">{cell.sub}</div>
        </div>
      ))}
    </div>
  );
}

export default TrustBand;
