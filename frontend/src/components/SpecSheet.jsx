import { t } from '../i18n';

/**
 * SpecSheet — ficha técnica del producto.
 * Props: product, variant
 */
function SpecSheet({ product, variant }) {
  const rows = [
    [t('spec.brand'), product.brand],
    [t('spec.model'), product.name],
    ...Object.entries(variant.attrs).map(([k, v]) => [k.charAt(0).toUpperCase() + k.slice(1), String(v)]),
    [t('spec.stock'), t('spec.stockValue', { n: variant.stock })],
    [t('spec.tags'), product.tags.join(' · ')],
    [t('spec.warranty'), t('spec.warrantyValue')],
  ];
  return (
    <div className="spec-sheet">
      <h3 className="spec-sheet-title">{t('spec.title')}</h3>
      {rows.map(([k, v]) => (
        <div key={k} className="spec-row">
          <span className="spec-row-k">{k}</span>
          <span className="spec-row-v">{v}</span>
        </div>
      ))}
    </div>
  );
}

export default SpecSheet;
