import { useNavigate } from 'react-router-dom';
import { useCatalog } from '../hooks/useCatalog';
import ProductCard from '../components/ProductCard';
import TrustBand from '../components/TrustBand';
import BrandMarquee from '../components/BrandMarquee';
import SkeletonGrid from '../components/SkeletonGrid';
import Btn from '../components/Btn';
import Empty from '../components/Empty';
import ProductImage from '../components/ProductImage';
import { fmtARS } from '../utils/format';
import { t } from '../i18n';

function CatalogHero({ featured, totalAll }) {
  const navigate = useNavigate();
  return (
    <section className="hero">
      <div className="hero-grid">
        <div className="hero-left">
          <div>
            <div className="hero-eyebrow">
              <span className="hero-eyebrow-dot"></span>
              <span>{t('catalog.hero.eyebrow')}</span>
            </div>
            <h1 className="hero-headline">
              {t('catalog.hero.titlePre')} <em>{t('catalog.hero.titleEm1')}</em>,<br/>
              {t('catalog.hero.titleMid')} <em>{t('catalog.hero.titleEm2')}</em>.
            </h1>
            <p className="hero-sub">{t('catalog.hero.sub')}</p>
          </div>
          <div className="hero-stats">
            <div>
              <div className="hero-stat-num"><em>{totalAll}</em></div>
              <div className="hero-stat-label">{t('catalog.hero.statSkus')}</div>
            </div>
            <div>
              <div className="hero-stat-num">{t('catalog.hero.depositsValue')}</div>
              <div className="hero-stat-label">{t('catalog.hero.statDeposits')}</div>
            </div>
            <div>
              <div className="hero-stat-num">{t('catalog.hero.dispatchValue')}</div>
              <div className="hero-stat-label">{t('catalog.hero.statDispatch')}</div>
            </div>
          </div>
        </div>

        <div className="hero-right">
          <div className="hero-right-meta">
            <span>{t('catalog.hero.featuredEyebrow', { brand: featured?.brand })}</span>
            <span>{t('catalog.hero.featuredExp', { n: String(featured?.product_id || '00').padStart(2, '0') })}</span>
          </div>
          <div className="hero-right-num">
            N°<em>0{featured?.product_id || '1'}</em>
          </div>
          <div className="hero-right-foot">
            <div className="hero-right-tag">
              {featured?.name}<br/>
              {t('catalog.hero.featuredMeta', { rating: featured?.rating, count: featured?.reviewCount })}
            </div>
            <button
              className="hero-cta"
              onClick={() => navigate(`/producto/${featured?.product_id}`)}
            >
              {t('catalog.hero.seeProduct')}
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}

function CategoryStrip({ categories, activeId, onSelect, totalAll }) {
  return (
    <section>
      <div className="grid-section-head">
        <h2 className="grid-section-h1">
          {t('catalog.categoryStrip.titlePre')} <em>{t('catalog.categoryStrip.titleEm')}</em>
        </h2>
        <span className="grid-section-meta">
          {t('catalog.categoryStrip.meta', { categories: categories.length, total: totalAll })}
        </span>
      </div>
      <div className="cat-strip">
        {categories.map((c, i) => (
          <button
            key={c.id}
            className={`cat-tile ${activeId === c.id ? 'is-active' : ''}`}
            onClick={() => onSelect(c.id)}
          >
            <span className="cat-tile-num">0{i + 1} / 0{categories.length}</span>
            <span className="cat-tile-name">{c.name}</span>
            <span className="cat-tile-glyph">{c.name[0]}</span>
          </button>
        ))}
      </div>
    </section>
  );
}

function FeaturedBand({ product }) {
  const navigate = useNavigate();
  const v0 = product.variants[0];
  const specs = Object.entries(v0.attrs || {}).slice(0, 3);
  const fallbackSpecs = [
    [t('catalog.featured.specBrand'), product.brand],
    [t('catalog.featured.specRating'), `${product.rating} / 5`],
    [t('catalog.featured.specReviews'), String(product.reviewCount)],
  ];
  const displaySpecs = specs.length >= 2 ? specs : fallbackSpecs;

  return (
    <section
      className="featured-band"
      onClick={() => navigate(`/producto/${product.product_id}`)}
    >
      <div className="featured-img">
        <span className="featured-tag">{t('catalog.featured.pick')}</span>
        <ProductImage src={product.image_url} alt={product.name} ratio="4/3" />
      </div>
      <div className="featured-body">
        <div>
          <div className="featured-eyebrow">
            <span>{t('catalog.featured.selection')}</span>
            <span className="featured-eyebrow-line"></span>
            <span>{product.tags[0]}</span>
          </div>
          <div className="featured-brand">{product.brand}</div>
          <h2 className="featured-name">{product.name}</h2>
          <p className="featured-desc">{product.description}</p>
        </div>
        <div className="featured-specs">
          {displaySpecs.map(([k, v]) => (
            <div key={k}>
              <div className="featured-spec-val">{v}</div>
              <div className="featured-spec-label">{k}</div>
            </div>
          ))}
        </div>
        <div className="featured-foot">
          <div className="featured-price">
            <span className="featured-price-from">{t('catalog.featured.from')}</span>
            <span className="featured-price-val">{fmtARS(v0.base_price)}</span>
          </div>
          <Btn variant="primary" size="lg">{t('catalog.featured.seeProduct')}</Btn>
        </div>
      </div>
    </section>
  );
}

function Catalog() {
  const { filtered: products, filters, categories, products: allProducts, catalogLoading, setFilter, clearFilters } = useCatalog();
  const totalAll = allProducts.length;

  const activeCat = categories.find((c) => c.id === filters.categoryId);
  const isHome = !filters.categoryId && !filters.search;
  const featured = [...allProducts].sort((a, b) => b.rating - a.rating)[0];

  return (
    <main className="screen">
      {isHome && <CatalogHero featured={featured} totalAll={totalAll} />}

      {isHome && (
        <CategoryStrip
          categories={categories}
          activeId={filters.categoryId}
          totalAll={totalAll}
          onSelect={(id) => setFilter({ categoryId: id })}
        />
      )}

      {isHome && featured && <FeaturedBand product={featured} />}

      {isHome && <BrandMarquee />}

      <div className="grid-section-head">
        <h1 className="grid-section-h1">
          {activeCat
            ? activeCat.name
            : filters.search
              ? <>{t('catalog.grid.titleResults')}</>
              : <>{t('catalog.grid.titleAllPre')} <em>{t('catalog.grid.titleAllEm')}</em></>}
        </h1>
        <div className="catalog-controls">
          <span className="grid-section-meta">
            {t('catalog.grid.count', { shown: products.length, total: totalAll })}
          </span>
          <label className="check mono">
            <input
              type="checkbox"
              checked={filters.inStockOnly}
              onChange={(e) => setFilter({ inStockOnly: e.target.checked })}
            />
            {t('catalog.grid.inStockOnly')}
          </label>
          <select
            className="select mono"
            value={filters.sort}
            onChange={(e) => setFilter({ sort: e.target.value })}
          >
            <option value="relevance">{t('catalog.grid.sort.relevance')}</option>
            <option value="price-asc">{t('catalog.grid.sort.priceAsc')}</option>
            <option value="price-desc">{t('catalog.grid.sort.priceDesc')}</option>
            <option value="rating">{t('catalog.grid.sort.rating')}</option>
          </select>
        </div>
      </div>

      {catalogLoading ? (
        <SkeletonGrid count={8} />
      ) : products.length === 0 ? (
        <Empty
          title={t('catalog.grid.emptyTitle')}
          hint={t('catalog.grid.emptyHint')}
          action={<Btn variant="ghost" onClick={() => clearFilters()}>{t('catalog.grid.clearFilters')}</Btn>}
        />
      ) : (
        <div className="grid">
          {products.map((p) => <ProductCard key={p.product_id} product={p} />)}
        </div>
      )}

      <TrustBand />
    </main>
  );
}

export default Catalog;
