import { useCatalog } from '../hooks/useCatalog';

/**
 * BrandMarquee — marquee de marcas con scroll infinito horizontal.
 * Las marcas salen del catálogo real (no de una lista hardcodeada): se toman
 * las marcas únicas de los productos. La unidad se repite hasta tener ancho
 * suficiente y luego se duplica para lograr un loop sin cortes.
 */
function BrandMarquee() {
  const { products } = useCatalog();

  const brands = [...new Set(
    products.map((p) => (p.brand || '').trim().toUpperCase()).filter(Boolean),
  )];

  if (brands.length === 0) return null;

  const unit = [];
  while (unit.length < 12) unit.push(...brands);
  const row = [...unit, ...unit];

  return (
    <section className="marquee" aria-hidden="true">
      <div className="marquee-rail">
        {row.map((b, i) => (
          <span key={i} className="marquee-item">
            <span className="marquee-glyph">✦</span>
            <span className="marquee-text">{b}</span>
          </span>
        ))}
      </div>
    </section>
  );
}

export default BrandMarquee;
