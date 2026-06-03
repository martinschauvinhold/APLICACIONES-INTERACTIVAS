import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import ProductCard from '../components/ProductCard';
import TrustBand from '../components/TrustBand';
import Btn from '../components/Btn';
import Empty from '../components/Empty';
import ProductImage from '../components/ProductImage';
import { fmtARS } from '../utils/format';
import {
  selectFilteredProducts,
} from '../store/selectors';
import { setFilter, clearFilters, hydrate as hydrateCatalog } from '../store/slices/catalogSlice';
import { api } from '../api/client';

// ====== Subcomponentes locales del Catálogo ======

function CatalogHero({ featured, totalAll }) {
  const navigate = useNavigate();
  return (
    <section className="hero">
      <div className="hero-grid">
        <div className="hero-left">
          <div>
            <div className="hero-eyebrow">
              <span className="hero-eyebrow-dot"></span>
              <span>EDICIÓN MAYO · 2026 / BUENOS AIRES</span>
            </div>
            <h1 className="hero-headline" style={{ marginTop: 16 }}>
              Tecnología <em>seria</em>,<br/>
              para gente <em>seria</em>.
            </h1>
            <p className="hero-sub" style={{ marginTop: 18 }}>
              Curaduría minorista y mayorista. Trabajamos con vendedores oficiales,
              stock verificado por depósito — sin sorpresas en el checkout.
            </p>
          </div>
          <div className="hero-stats">
            <div>
              <div className="hero-stat-num"><em>{totalAll}</em></div>
              <div className="hero-stat-label">Productos activos</div>
            </div>
            <div>
              <div className="hero-stat-num">04</div>
              <div className="hero-stat-label">Depósitos</div>
            </div>
            <div>
              <div className="hero-stat-num">24h</div>
              <div className="hero-stat-label">Despacho promedio</div>
            </div>
          </div>
        </div>

        <div className="hero-right">
          <div className="hero-right-meta">
            <span>// destacado · {featured?.brand}</span>
            <span>EXP.{String(featured?.product_id || '00').padStart(2, '0')}</span>
          </div>
          <div className="hero-right-num">
            N°<em>0{featured?.product_id || '1'}</em>
          </div>
          <div className="hero-right-foot">
            <div className="hero-right-tag">
              {featured?.name}<br/>
              ★ {featured?.rating} · {featured?.reviewCount} reseñas
            </div>
            <button
              className="hero-cta"
              onClick={() => navigate(`/producto/${featured?.product_id}`)}
            >
              Ver producto →
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}

function CategoryStrip({ categories, activeId, onSelect }) {
  return (
    <section>
      <div className="grid-section-head">
        <h2 className="grid-section-h1">Comprá por <em>categoría</em></h2>
        <span className="grid-section-meta">{categories.length} categorías</span>
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
    ['Marca', product.brand],
    ['Rating', `${product.rating} / 5`],
    ['Reviews', String(product.reviewCount)],
  ];
  const displaySpecs = specs.length >= 2 ? specs : fallbackSpecs;

  return (
    <section
      className="featured-band"
      onClick={() => navigate(`/producto/${product.product_id}`)}
    >
      <div className="featured-img">
        <span className="featured-tag">★ EDITOR'S PICK</span>
        <ProductImage src={product.image_url} alt={product.name} ratio="4/3" />
      </div>
      <div className="featured-body">
        <div>
          <div className="featured-eyebrow">
            <span>NUESTRA SELECCIÓN</span>
            <span className="featured-eyebrow-line"></span>
            <span>{product.tags[0]}</span>
          </div>
          <div className="featured-brand" style={{ marginTop: 18 }}>{product.brand}</div>
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
            <span className="featured-price-from">desde</span>
            <span className="featured-price-val">{fmtARS(v0.base_price)}</span>
          </div>
          <Btn variant="primary" size="lg">Ver producto →</Btn>
        </div>
      </div>
    </section>
  );
}

// ====== Página principal ======

/**
 * Catalog — vista principal del catálogo.
 *
 * Hooks usados:
 *   - useSelector: lee productos filtrados, filtros, categorías
 *   - useDispatch: despacha acciones de Redux
 *   - useState: estado local del loading
 *   - useEffect: hidrata el catálogo desde la API al montar
 *
 * Componentes locales: CatalogHero, CategoryStrip, FeaturedBand
 * Componentes globales: ProductCard, TrustBand
 */
function Catalog() {
  const products = useSelector(selectFilteredProducts);
  const filters = useSelector((s) => s.catalog.filters);
  const categories = useSelector((s) => s.catalog.categories);
  const allProducts = useSelector((s) => s.catalog.products);
  const totalAll = allProducts.length;
  const dispatch = useDispatch();

  const activeCat = categories.find((c) => c.id === filters.categoryId);
  const isHome = !filters.categoryId && !filters.search;
  const featured = [...allProducts].sort((a, b) => b.rating - a.rating)[0];

  // Hidratar catálogo desde la API una sola vez
  useEffect(() => {
    if (window.__catalogHydrated) return;
    window.__catalogHydrated = true;
    api.products.list().then((remote) => {
      if (Array.isArray(remote) && remote.length > 0) {
        dispatch(hydrateCatalog({ products: remote }));
      }
    }).catch(() => { /* fallback a datos semilla */ });
  }, [dispatch]);

  return (
    <main className="screen">
      {isHome && <CatalogHero featured={featured} totalAll={totalAll} />}

      {isHome && (
        <CategoryStrip
          categories={categories}
          activeId={filters.categoryId}
          onSelect={(id) => dispatch(setFilter({ categoryId: id }))}
        />
      )}

      {isHome && featured && <FeaturedBand product={featured} />}

      <div className="grid-section-head">
        <h1 className="grid-section-h1">
          {activeCat ? activeCat.name : (filters.search ? <>Resultados</> : <>Todo el <em>catálogo</em></>)}
        </h1>
        <div className="catalog-controls">
          <span className="grid-section-meta">{products.length} de {totalAll}</span>
          <label className="check mono">
            <input
              type="checkbox"
              checked={filters.inStockOnly}
              onChange={(e) => dispatch(setFilter({ inStockOnly: e.target.checked }))}
            />
            sólo con stock
          </label>
          <select
            className="select mono"
            value={filters.sort}
            onChange={(e) => dispatch(setFilter({ sort: e.target.value }))}
          >
            <option value="relevance">relevancia</option>
            <option value="price-asc">precio · asc</option>
            <option value="price-desc">precio · desc</option>
            <option value="rating">mejor rating</option>
          </select>
        </div>
      </div>

      {products.length === 0 ? (
        <Empty
          title="No hay productos que coincidan"
          hint="Probá limpiar los filtros o cambiar la búsqueda."
          action={<Btn variant="ghost" onClick={() => dispatch(clearFilters())}>Limpiar filtros</Btn>}
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
