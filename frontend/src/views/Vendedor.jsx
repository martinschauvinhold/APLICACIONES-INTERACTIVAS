import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Plus, Trash2, TriangleAlert, ImagePlus } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import {
  fetchCatalog,
  createProduct,
  deleteProduct,
  uploadProductImage,
  selectProducts,
  selectProductsLoading,
  selectProductsError,
  selectProductsMutating,
  selectProductsMutateError,
} from '../features/products/productsSlice';
import { createVariant, updateVariant, upsertVariantTier } from '../features/variants/variantsSlice';
import { createInventory, updateInventory, fetchInventoryByVariant } from '../features/inventory/inventorySlice';
import { fetchCategories, selectCategories } from '../features/categories/categoriesSlice';
import { fetchWarehouses, selectWarehouses } from '../features/warehouses/warehousesSlice';
import { fetchSalesBySeller, selectSellerSales } from '../features/seller/sellerSlice';
import Btn from '../components/Btn';
import Field from '../components/Field';
import Empty from '../components/Empty';
import { fmtARS, slugAttrs } from '../utils/format';
import { t, orderStatus } from '../i18n';

function parseAttrsInput(text) {
  const attrs = {};
  (text || '').split(',').forEach((pair) => {
    const [k, ...rest] = pair.split(':');
    const key = k?.trim();
    const val = rest.join(':').trim();
    if (key && val) attrs[key] = val;
  });
  return attrs;
}

function EditableNumber({ value, disabled, onSave, className }) {
  const [val, setVal] = useState(value);
  useEffect(() => { setVal(value); }, [value]);
  const commit = () => {
    const n = Number(val);
    if (!Number.isNaN(n) && n !== Number(value)) onSave(n);
    else setVal(value);
  };
  return (
    <input
      type="number"
      className={className}
      value={val}
      disabled={disabled}
      onChange={(e) => setVal(e.target.value)}
      onBlur={commit}
      onKeyDown={(e) => { if (e.key === 'Enter') e.currentTarget.blur(); }}
    />
  );
}

function Vendedor() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const allProducts = useSelector(selectProducts);
  const loading = useSelector(selectProductsLoading);
  const error = useSelector(selectProductsError);
  const mutating = useSelector(selectProductsMutating);
  const mutateError = useSelector(selectProductsMutateError);
  const categories = useSelector(selectCategories);
  const warehouses = useSelector(selectWarehouses);
  const sales = useSelector(selectSellerSales);

  const products = allProducts.filter((p) => p.seller_id === user.user_id);
  const defaultWarehouseId = warehouses[0]?.warehouse_id ?? null;

  const [tab, setTab] = useState('resumen');

  useEffect(() => {
    dispatch(fetchCatalog());
    dispatch(fetchCategories());
    dispatch(fetchWarehouses());
  }, [dispatch]);

  useEffect(() => {
    dispatch(fetchSalesBySeller(user.user_id));
  }, [dispatch, user.user_id]);

  const reload = () => dispatch(fetchCatalog());

  const handleCreate = async ({ name, brand, description, categoryId, sku, attrs, basePrice, stock, imageFile }) => {
    const product = await dispatch(createProduct({ name, description, brand, categoryId })).unwrap();
    if (imageFile) {
      await dispatch(uploadProductImage({ productId: product.id, file: imageFile })).unwrap();
    }
    const variant = await dispatch(createVariant({
      productId: product.id,
      sku,
      attributes: JSON.stringify(attrs),
      basePrice,
    })).unwrap();
    if (defaultWarehouseId != null) {
      await dispatch(createInventory({
        variantId: variant.id,
        warehouseId: defaultWarehouseId,
        stockQuantity: stock,
      })).unwrap();
    }
    reload();
  };

  const handleDelete = async (product) => {
    if (!window.confirm(t('seller.deleteConfirm', { name: product.name }))) return;
    await dispatch(deleteProduct(product.product_id)).unwrap().catch(() => {});
  };

  const savePrice = async (product, variant, basePrice) => {
    await dispatch(updateVariant({
      id: variant.variant_id,
      body: {
        productId: product.product_id,
        sku: variant.sku,
        attributes: JSON.stringify(variant.attrs),
        basePrice,
      },
    })).unwrap().catch(() => {});
    reload();
  };

  const saveStock = async (variant, stock) => {
    const { data: rows } = await dispatch(fetchInventoryByVariant(variant.variant_id)).unwrap();
    if (rows.length > 0) {
      const row = rows[0];
      await dispatch(updateInventory({
        id: row.inventory_id,
        body: { variantId: variant.variant_id, warehouseId: row.warehouse_id, stockQuantity: stock },
      })).unwrap().catch(() => {});
    } else if (defaultWarehouseId != null) {
      await dispatch(createInventory({
        variantId: variant.variant_id,
        warehouseId: defaultWarehouseId,
        stockQuantity: stock,
      })).unwrap().catch(() => {});
    }
    reload();
  };

  const saveTier = async (variant, tier, unitPrice) => {
    await dispatch(upsertVariantTier({
      variantId: variant.variant_id,
      tier_id: tier.tier_id,
      min_quantity: tier.min_quantity,
      unit_price: unitPrice,
    })).unwrap().catch((err) => console.error('No se pudo guardar el tier', err));
    reload();
  };

  const TABS = [
    { id: 'resumen', label: t('seller.tabs.summary') },
    { id: 'productos', label: t('seller.tabs.products') },
    { id: 'mayorista', label: t('seller.tabs.tiers') },
    { id: 'ventas', label: t('seller.tabs.sales') },
  ];

  return (
    <main className="screen">
      <div className="admin-head">
        <div>
          <div className="eyebrow mono">{t('seller.eyebrow')}</div>
          <h1 className="screen-title admin-title">{user.first_name}</h1>
        </div>
        <Btn variant="ghost" onClick={() => navigate('/catalogo')}>{t('seller.backToStore')}</Btn>
      </div>

      {(error || mutateError) && (
        <span className="field-error"><TriangleAlert size={14} /> {mutateError || error}</span>
      )}

      <nav className="admin-tabs">
        {TABS.map((tb) => (
          <button key={tb.id} className={`admin-tab ${tab === tb.id ? 'is-active' : ''}`} onClick={() => setTab(tb.id)}>
            {tb.label}
          </button>
        ))}
      </nav>

      {loading && products.length === 0 ? (
        <div className="ticket-hint mono">{t('seller.loading')}</div>
      ) : (
        <>
          {tab === 'resumen' && <SellerSummary products={products} sales={sales} />}
          {tab === 'productos' && (
            <SellerProducts
              products={products}
              categories={categories}
              mutating={mutating}
              hasWarehouse={defaultWarehouseId != null}
              onCreate={handleCreate}
              onDelete={handleDelete}
              onSavePrice={savePrice}
              onSaveStock={saveStock}
            />
          )}
          {tab === 'mayorista' && <SellerTiers products={products} onSaveTier={saveTier} />}
          {tab === 'ventas' && <SellerSales sales={sales} />}
        </>
      )}
    </main>
  );
}

function SellerSummary({ products, sales }) {
  const variants = products.flatMap((p) => p.variants);
  const stock = variants.reduce((a, v) => a + v.stock, 0);
  const outOfStock = variants.filter((v) => v.stock === 0).length;
  const revenue = sales.reduce((a, s) => a + s.subtotal, 0);
  return (
    <section className="admin-metrics">
      <div className="metric"><div className="metric-num">{products.length}</div><div className="metric-label">{t('seller.metrics.products')}</div></div>
      <div className="metric"><div className="metric-num">{stock}</div><div className="metric-label">{t('seller.metrics.stock')}</div></div>
      <div className="metric"><div className={`metric-num ${outOfStock > 0 ? 'metric-num-danger' : ''}`}>{outOfStock}</div><div className="metric-label">{t('seller.metrics.outOfStock')}</div></div>
      <div className="metric"><div className="metric-num">{sales.length}</div><div className="metric-label">{t('seller.metrics.sales')}</div></div>
      <div className="metric metric-wide"><div className="metric-num">{fmtARS(revenue)}</div><div className="metric-label">{t('seller.metrics.revenue')}</div></div>
    </section>
  );
}

const EMPTY_FORM = { name: '', brand: '', description: '', categoryId: '', sku: '', attrs: '', basePrice: '', stock: '', imageFile: null, imagePreview: null };

function SellerProducts({ products, categories, mutating, hasWarehouse, onCreate, onDelete, onSavePrice, onSaveStock }) {
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});

  const set = (patch) => setForm((f) => ({ ...f, ...patch }));

  const resetForm = () => {
    setForm((f) => {
      if (f.imagePreview) URL.revokeObjectURL(f.imagePreview);
      return EMPTY_FORM;
    });
  };

  const onPickImage = (e) => {
    const file = e.target.files?.[0] || null;
    setForm((f) => {
      if (f.imagePreview) URL.revokeObjectURL(f.imagePreview);
      return { ...f, imageFile: file, imagePreview: file ? URL.createObjectURL(file) : null };
    });
  };

  const submit = async (e) => {
    e.preventDefault();
    const errs = {};
    if (form.name.trim().length < 2) errs.name = t('seller.create.errName');
    if (form.brand.trim().length < 1) errs.brand = t('seller.create.errBrand');
    if (form.sku.trim().length < 1) errs.sku = t('seller.create.errSku');
    if (!(Number(form.basePrice) > 0)) errs.basePrice = t('seller.create.errPrice');
    setErrors(errs);
    if (Object.keys(errs).length) return;

    try {
      await onCreate({
        name: form.name.trim(),
        brand: form.brand.trim(),
        description: form.description.trim(),
        categoryId: form.categoryId ? Number(form.categoryId) : (categories[0]?.id ?? null),
        sku: form.sku.trim(),
        attrs: parseAttrsInput(form.attrs),
        basePrice: Number(form.basePrice),
        stock: Math.max(0, Number(form.stock) || 0),
        imageFile: form.imageFile,
      });
      resetForm();
      setCreating(false);
    } catch {
      // mutateError se muestra a nivel panel.
    }
  };

  return (
    <div>
      <div className="ticket-toolbar">
        <h2 className="support-h2">{t('seller.title')} <span className="screen-title-meta mono">{t('seller.countMeta', { n: products.length })}</span></h2>
        {!creating && (
          <Btn variant="primary" onClick={() => { setCreating(true); setErrors({}); }}>
            <Plus size={16} /> {t('seller.newProduct')}
          </Btn>
        )}
      </div>

      {creating && (
        <form className="support-form seller-create" onSubmit={submit}>
          <h3 className="support-h2">{t('seller.create.title')}</h3>
          <div className="support-form-row">
            <Field label={t('seller.create.name')} error={errors.name}>
              <input className="input" value={form.name} onChange={(e) => set({ name: e.target.value })} placeholder={t('seller.create.namePlaceholder')} />
            </Field>
            <Field label={t('seller.create.brand')} error={errors.brand}>
              <input className="input" value={form.brand} onChange={(e) => set({ brand: e.target.value })} placeholder={t('seller.create.brandPlaceholder')} />
            </Field>
          </div>
          <Field label={t('seller.create.category')}>
            <select className="select" value={form.categoryId} onChange={(e) => set({ categoryId: e.target.value })}>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </Field>
          <Field label={t('seller.create.description')}>
            <textarea className="input support-textarea" rows={3} value={form.description} onChange={(e) => set({ description: e.target.value })} placeholder={t('seller.create.descriptionPlaceholder')} />
          </Field>

          <Field label={t('seller.create.image')} hint={t('seller.create.imageHint')}>
            <input type="file" accept="image/*" className="input" onChange={onPickImage} />
          </Field>
          <div className="seller-image-preview">
            {form.imagePreview ? (
              <img src={form.imagePreview} alt={t('seller.create.imageAlt')} />
            ) : (
              <div className="seller-image-empty"><ImagePlus size={20} /> {t('seller.create.imageEmpty')}</div>
            )}
          </div>

          <div className="eyebrow mono ticket-section-label">{t('seller.create.variantTitle')}</div>
          <div className="support-form-row">
            <Field label={t('seller.create.sku')} error={errors.sku}>
              <input className="input mono" value={form.sku} onChange={(e) => set({ sku: e.target.value })} placeholder={t('seller.create.skuPlaceholder')} />
            </Field>
            <Field label={t('seller.create.attrs')}>
              <input className="input mono" value={form.attrs} onChange={(e) => set({ attrs: e.target.value })} placeholder={t('seller.create.attrsPlaceholder')} />
            </Field>
          </div>
          <div className="support-form-row">
            <Field label={t('seller.create.basePrice')} error={errors.basePrice}>
              <input className="input mono" type="number" value={form.basePrice} onChange={(e) => set({ basePrice: e.target.value })} />
            </Field>
            <Field label={t('seller.create.stock')}>
              <input className="input mono" type="number" value={form.stock} onChange={(e) => set({ stock: e.target.value })} />
            </Field>
          </div>
          {!hasWarehouse && <span className="field-error"><TriangleAlert size={14} /> {t('seller.create.noWarehouse')}</span>}

          <div className="seller-create-actions">
            <Btn variant="ghost" type="button" onClick={() => { setCreating(false); resetForm(); setErrors({}); }}>
              {t('seller.create.cancel')}
            </Btn>
            <Btn variant="primary" disabled={mutating}>
              {mutating ? t('seller.saving') : t('seller.create.submit')}
            </Btn>
          </div>
        </form>
      )}

      {products.length === 0 ? (
        <Empty title={t('seller.empty')} />
      ) : (
        <div className="admin-table">
          <div className="admin-row admin-row-head admin-row-seller">
            <span>{t('admin.products.colProduct')}</span>
            <span>{t('admin.products.colVariant')}</span>
            <span>{t('admin.products.colBasePrice')}</span>
            <span>{t('admin.products.colStock')}</span>
            <span>{t('admin.products.colStatus')}</span>
            <span>{t('seller.colActions')}</span>
          </div>
          {products.map((p) =>
            p.variants.map((v, vi) => (
              <div key={v.variant_id} className="admin-row admin-row-seller">
                <span>{vi === 0 && <><span className="admin-brand">{p.brand}</span> {p.name}</>}</span>
                <span className="mono admin-sku">{v.sku}<br /><span className="admin-attrs">{slugAttrs(v.attrs)}</span></span>
                <span>
                  <EditableNumber className="admin-input mono" value={v.base_price} disabled={mutating}
                    onSave={(n) => onSavePrice(p, v, n)} />
                </span>
                <span>
                  <EditableNumber className="admin-input mono" value={v.stock} disabled={mutating}
                    onSave={(n) => onSaveStock(v, Math.max(0, n))} />
                </span>
                <span>
                  <span className={`admin-pill ${v.stock === 0 ? 'is-off' : 'is-on'}`}>
                    {v.stock === 0 ? t('admin.products.outOfStock') : t('admin.products.active')}
                  </span>
                </span>
                <span>
                  {vi === 0 && (
                    <Btn variant="ghost" size="sm" disabled={mutating} onClick={() => onDelete(p)}>
                      <Trash2 size={14} /> {t('seller.deleteProduct')}
                    </Btn>
                  )}
                </span>
              </div>
            )),
          )}
        </div>
      )}
    </div>
  );
}

function SellerTiers({ products, onSaveTier }) {
  const [selectedId, setSelectedId] = useState(products[0]?.product_id);
  if (products.length === 0) return <Empty title={t('seller.empty')} />;
  const product = products.find((p) => p.product_id === selectedId) || products[0];

  return (
    <div className="admin-tiers">
      <aside className="admin-tiers-list">
        <div className="admin-tiers-list-h mono">{t('admin.tiers.listTitle')}</div>
        {products.map((p) => (
          <button key={p.product_id} className={`admin-tiers-item ${p.product_id === selectedId ? 'is-active' : ''}`} onClick={() => setSelectedId(p.product_id)}>
            <span className="admin-tiers-item-brand mono">{p.brand}</span>
            <span className="admin-tiers-item-name">{p.name}</span>
          </button>
        ))}
      </aside>

      <div className="admin-tiers-detail">
        <div className="admin-tiers-intro">
          <h2 className="admin-tiers-title">{product.name}</h2>
          <p className="admin-tiers-desc">{t('admin.tiers.desc')}</p>
        </div>
        {product.variants.map((v) => (
          <div key={v.variant_id} className="tier-card">
            <div className="tier-card-head">
              <div>
                <span className="mono tier-card-sku">{v.sku}</span>
                <span className="tier-card-attrs">{slugAttrs(v.attrs)}</span>
              </div>
              <span className="mono tier-card-base">{t('admin.tiers.base', { price: fmtARS(v.base_price) })}</span>
            </div>
            <div className="tier-grid">
              <div className="tier-grid-head mono">
                <span>{t('admin.tiers.colMinQty')}</span>
                <span>{t('admin.tiers.colUnit')}</span>
                <span>{t('admin.tiers.colDiscount')}</span>
              </div>
              {v.tiers.map((tier, ti) => {
                const disc = Math.round((1 - tier.unit_price / v.base_price) * 100);
                return (
                  <div key={ti} className="tier-grid-row">
                    <span className="mono">{t('admin.tiers.minQty', { n: tier.min_quantity })}</span>
                    <EditableNumber className="admin-input mono" value={tier.unit_price}
                      onSave={(n) => onSaveTier(v, tier, n)} />
                    <span className={`mono tier-disc ${disc > 0 ? 'is-disc' : ''}`}>{disc > 0 ? `−${disc}%` : '—'}</span>
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

function SellerSales({ sales }) {
  if (sales.length === 0) return <Empty title={t('seller.sales.empty')} />;
  return (
    <div className="admin-table">
      <div className="admin-row admin-row-head admin-row-sales">
        <span>{t('seller.sales.colOrder')}</span>
        <span>{t('seller.sales.colItems')}</span>
        <span>{t('seller.sales.colUnits')}</span>
        <span>{t('seller.sales.colTotal')}</span>
        <span>{t('seller.sales.colStatus')}</span>
        <span>{t('seller.sales.colDate')}</span>
      </div>
      {sales.map(({ order, items, units, subtotal }) => (
        <div key={order.order_id} className="admin-row admin-row-sales">
          <span className="mono">#{order.order_id}</span>
          <span>{items.map((it) => `${it.product_name} ×${it.quantity}`).join(' · ')}</span>
          <span className="mono">{units}</span>
          <span className="mono admin-order-total">{fmtARS(subtotal)}</span>
          <span><span className={`order-status order-status-${order.status}`}>{orderStatus(order.status)}</span></span>
          <span className="mono admin-attrs">{new Date(order.created_at).toLocaleDateString('es-AR')}</span>
        </div>
      ))}
    </div>
  );
}

export default Vendedor;
