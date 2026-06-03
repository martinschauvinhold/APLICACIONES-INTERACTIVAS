import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import Btn from '../components/Btn';
import { fmtARS, slugAttrs } from '../utils/format';
import {
  updateTier, updateStock, updateBasePrice, toggleProductActive,
} from '../store/slices/catalogSlice';

/**
 * Admin — panel de administración (rol admin / seller).
 *
 * Tabs:
 *   - Resumen     → métricas del catálogo y pedidos
 *   - Productos   → ABM: stock, precio base, activar/desactivar
 *   - Price Tiers → editor de precios escalonados (mayorista) por variante
 *   - Pedidos     → tabla de pedidos con estado
 *
 * Hooks: useState (tab activa), useSelector, useDispatch, useNavigate
 */
function Admin() {
  const products = useSelector((s) => s.catalog.products);
  const orders = useSelector((s) => s.orders.list);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [tab, setTab] = useState('resumen');

  // Métricas para el resumen
  const totalVariants = products.reduce((a, p) => a + p.variants.length, 0);
  const totalStock = products.reduce((a, p) => a + p.variants.reduce((b, v) => b + v.stock, 0), 0);
  const outOfStock = products.reduce((a, p) => a + p.variants.filter((v) => v.stock === 0).length, 0);
  const revenue = orders.reduce((a, o) => a + (o.totals?.total || 0), 0);

  const TABS = [
    { id: 'resumen',  label: 'Resumen' },
    { id: 'productos', label: 'Productos' },
    { id: 'tiers',    label: 'Price Tiers' },
    { id: 'pedidos',  label: 'Pedidos' },
  ];

  return (
    <main className="screen">
      <div className="admin-head">
        <div>
          <div className="eyebrow mono">PANEL DE ADMINISTRACIÓN · ROL ADMIN</div>
          <h1 className="screen-title" style={{ marginTop: 6 }}>
            Back<em style={{ fontStyle: 'italic', color: 'var(--accent)' }}>office</em>
          </h1>
        </div>
        <Btn variant="ghost" onClick={() => navigate('/')}>← Volver a la tienda</Btn>
      </div>

      <nav className="admin-tabs">
        {TABS.map((t) => (
          <button
            key={t.id}
            className={`admin-tab ${tab === t.id ? 'is-active' : ''}`}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </nav>

      {tab === 'resumen' && (
        <section className="admin-metrics">
          <div className="metric">
            <div className="metric-num">{products.length}</div>
            <div className="metric-label">Productos</div>
          </div>
          <div className="metric">
            <div className="metric-num">{totalVariants}</div>
            <div className="metric-label">Variantes (SKU)</div>
          </div>
          <div className="metric">
            <div className="metric-num">{totalStock}</div>
            <div className="metric-label">Unidades en stock</div>
          </div>
          <div className="metric">
            <div className="metric-num" style={{ color: outOfStock > 0 ? 'var(--danger)' : 'inherit' }}>{outOfStock}</div>
            <div className="metric-label">Variantes sin stock</div>
          </div>
          <div className="metric">
            <div className="metric-num">{orders.length}</div>
            <div className="metric-label">Pedidos</div>
          </div>
          <div className="metric metric-wide">
            <div className="metric-num">{fmtARS(revenue)}</div>
            <div className="metric-label">Facturación (sesión)</div>
          </div>
        </section>
      )}

      {tab === 'productos' && <AdminProducts products={products} dispatch={dispatch} />}
      {tab === 'tiers' && <AdminTiers products={products} dispatch={dispatch} />}
      {tab === 'pedidos' && <AdminOrders orders={orders} navigate={navigate} />}
    </main>
  );
}

// ── PRODUCTOS ──────────────────────────────────────────────
function AdminProducts({ products, dispatch }) {
  return (
    <div className="admin-table">
      <div className="admin-row admin-row-head">
        <span>Producto</span>
        <span>Variante</span>
        <span>Precio base</span>
        <span>Stock</span>
        <span>Estado</span>
      </div>
      {products.map((p) =>
        p.variants.map((v, vi) => (
          <div key={v.variant_id} className="admin-row">
            <span>
              {vi === 0 && <><span className="admin-brand">{p.brand}</span> {p.name}</>}
            </span>
            <span className="mono admin-sku">{v.sku}<br /><span className="admin-attrs">{slugAttrs(v.attrs)}</span></span>
            <span>
              <input
                type="number"
                className="admin-input mono"
                value={v.base_price}
                onChange={(e) => dispatch(updateBasePrice({ variantId: v.variant_id, base_price: Number(e.target.value) }))}
              />
            </span>
            <span>
              <input
                type="number"
                className="admin-input mono"
                value={v.stock}
                onChange={(e) => dispatch(updateStock({ variantId: v.variant_id, stock: Math.max(0, Number(e.target.value)) }))}
              />
            </span>
            <span>
              <span className={`admin-pill ${v.stock === 0 ? 'is-off' : 'is-on'}`}>
                {v.stock === 0 ? 'sin stock' : 'activo'}
              </span>
            </span>
          </div>
        ))
      )}
    </div>
  );
}

// ── PRICE TIERS ────────────────────────────────────────────
function AdminTiers({ products, dispatch }) {
  const [selectedId, setSelectedId] = useState(products[0]?.product_id);
  const product = products.find((p) => p.product_id === selectedId) || products[0];

  return (
    <div className="admin-tiers">
      <aside className="admin-tiers-list">
        <div className="admin-tiers-list-h mono">PRODUCTOS</div>
        {products.map((p) => (
          <button
            key={p.product_id}
            className={`admin-tiers-item ${p.product_id === selectedId ? 'is-active' : ''}`}
            onClick={() => setSelectedId(p.product_id)}
          >
            <span className="admin-tiers-item-brand mono">{p.brand}</span>
            <span className="admin-tiers-item-name">{p.name}</span>
          </button>
        ))}
      </aside>

      <div className="admin-tiers-detail">
        <div className="admin-tiers-intro">
          <h2 className="admin-tiers-title">{product.name}</h2>
          <p className="admin-tiers-desc">
            Los <strong>price tiers</strong> definen el precio unitario según la cantidad comprada.
            Al alcanzar la cantidad mínima de un tramo, se aplica automáticamente el precio mayorista
            en el carrito y el checkout.
          </p>
        </div>

        {product.variants.map((v) => (
          <div key={v.variant_id} className="tier-card">
            <div className="tier-card-head">
              <div>
                <span className="mono tier-card-sku">{v.sku}</span>
                <span className="tier-card-attrs">{slugAttrs(v.attrs)}</span>
              </div>
              <span className="mono tier-card-base">base: {fmtARS(v.base_price)}</span>
            </div>
            <div className="tier-grid">
              <div className="tier-grid-head mono">
                <span>Cant. mínima</span>
                <span>Precio unitario</span>
                <span>Descuento</span>
              </div>
              {v.tiers.map((t, ti) => {
                const disc = Math.round((1 - t.unit_price / v.base_price) * 100);
                return (
                  <div key={ti} className="tier-grid-row">
                    <span className="mono">≥ {t.min_quantity} u.</span>
                    <input
                      type="number"
                      className="admin-input mono"
                      value={t.unit_price}
                      onChange={(e) => dispatch(updateTier({ variantId: v.variant_id, tierIndex: ti, unit_price: Number(e.target.value) }))}
                    />
                    <span className={`mono tier-disc ${disc > 0 ? 'is-disc' : ''}`}>
                      {disc > 0 ? `−${disc}%` : '—'}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ── PEDIDOS ────────────────────────────────────────────────
function AdminOrders({ orders, navigate }) {
  if (orders.length === 0) {
    return (
      <div className="admin-empty">
        <p className="mono">// No hay pedidos registrados en esta sesión.</p>
        <p>Completá una compra en la tienda para verla acá.</p>
      </div>
    );
  }
  return (
    <div className="admin-table">
      <div className="admin-row admin-row-head admin-row-orders">
        <span>Pedido</span>
        <span>Estado</span>
        <span>Items</span>
        <span>Total</span>
        <span>Fecha</span>
        <span></span>
      </div>
      {orders.map((o) => (
        <div key={o.order_id} className="admin-row admin-row-orders">
          <span className="mono">#{o.order_id}</span>
          <span><span className={`order-status order-status-${o.status}`}>{o.status}</span></span>
          <span className="mono">{o.items.length}</span>
          <span className="mono" style={{ fontWeight: 600 }}>{fmtARS(o.totals.total)}</span>
          <span className="mono admin-attrs">{new Date(o.created_at).toLocaleDateString('es-AR')}</span>
          <span>
            <button className="admin-link mono" onClick={() => navigate(`/confirmacion/${o.order_id}`)}>ver →</button>
          </span>
        </div>
      ))}
    </div>
  );
}

export default Admin;
