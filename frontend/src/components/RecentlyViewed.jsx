import { useNavigate } from 'react-router-dom';
import { useUI } from '../hooks/useUI';
import { useCatalog } from '../hooks/useCatalog';
import ProductImage from './ProductImage';
import { fmtARS } from '../utils/format';
import { t } from '../i18n';

/**
 * RecentlyViewed — rail de productos vistos recientemente (al pie del detalle).
 * Lee los ids desde ui.recentlyViewed y los hidrata contra el catálogo.
 * Props: currentId (se excluye el producto que se está viendo).
 */
function RecentlyViewed({ currentId }) {
  const navigate = useNavigate();
  const { recentlyViewed: recentlyIds } = useUI();
  const { products } = useCatalog();

  const items = recentlyIds
    .filter((id) => id !== currentId)
    .map((id) => products.find((p) => p.product_id === id))
    .filter(Boolean)
    .slice(0, 5);

  if (items.length === 0) return null;

  return (
    <section className="recently">
      <div className="recently-head">
        <h2 className="grid-section-h1">
          {t('recently.titlePre')} <em>{t('recently.titleEm')}</em>
        </h2>
        <span className="grid-section-meta">
          {t(items.length === 1 ? 'recently.metaOne' : 'recently.metaMany', { n: items.length })}
        </span>
      </div>
      <div className="recently-rail">
        {items.map((p) => (
          <article
            key={p.product_id}
            className="recently-card"
            onClick={() => navigate(`/producto/${p.product_id}`)}
          >
            <div className="recently-card-img">
              <ProductImage src={p.image_url} alt={p.name} ratio="1/1" />
            </div>
            <div className="recently-card-brand mono">{p.brand}</div>
            <div className="recently-card-name">{p.name}</div>
            <div className="recently-card-price mono">
              {fmtARS(Math.min(...p.variants.map((v) => v.base_price)))}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

export default RecentlyViewed;
