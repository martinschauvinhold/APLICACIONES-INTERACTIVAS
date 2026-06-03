/**
 * SpecSheet — ficha técnica del producto.
 * Props: product, variant
 */
function SpecSheet({ product, variant }) {
  const rows = [
    ['Marca', product.brand],
    ['Modelo', product.name],
    ['Referencia', variant.sku],
    ...Object.entries(variant.attrs).map(([k, v]) => [k.charAt(0).toUpperCase() + k.slice(1), String(v)]),
    ['Stock disponible', `${variant.stock} unidades`],
    ['Tags', product.tags.join(' · ')],
    ['Garantía', '12 meses oficial'],
  ];
  return (
    <div className="spec-sheet">
      <h3 className="spec-sheet-title">Ficha técnica</h3>
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
