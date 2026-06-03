// Selectores derivados — calculan cosas a partir del state

// Producto por ID
export const selectProductById = (id) => (state) =>
  state.catalog.products.find((p) => p.product_id === Number(id));

// Variante + producto al que pertenece
export const selectVariantById = (variantId) => (state) => {
  for (const p of state.catalog.products) {
    const v = p.variants.find((v) => v.variant_id === variantId);
    if (v) return { product: p, variant: v };
  }
  return null;
};

// Catálogo filtrado y ordenado
export const selectFilteredProducts = (state) => {
  const { categoryId, search, sort, inStockOnly } = state.catalog.filters;
  let list = state.catalog.products.slice();
  if (categoryId) list = list.filter((p) => p.category_id === categoryId);
  if (search) {
    const q = search.toLowerCase();
    list = list.filter((p) =>
      p.name.toLowerCase().includes(q) ||
      p.brand.toLowerCase().includes(q) ||
      p.tags.some((t) => t.toLowerCase().includes(q))
    );
  }
  if (inStockOnly) list = list.filter((p) => p.variants.some((v) => v.stock > 0));
  if (sort === 'price-asc')  list.sort((a, b) => a.variants[0].base_price - b.variants[0].base_price);
  if (sort === 'price-desc') list.sort((a, b) => b.variants[0].base_price - a.variants[0].base_price);
  if (sort === 'rating')     list.sort((a, b) => b.rating - a.rating);
  return list;
};

// Líneas del carrito con datos hidratados (producto, variante, precios)
export const selectCartLines = (state) => {
  return state.cart.items.map((item) => {
    const found = selectVariantById(item.variantId)(state);
    if (!found) return null;
    const { product, variant } = found;
    // Aplicar PRICE_TIERS según cantidad
    const tier = variant.tiers.slice().reverse().find((t) => item.quantity >= t.min_quantity) || variant.tiers[0];
    const unit_price = tier.unit_price;
    const subtotal = unit_price * item.quantity;
    const tierSavings = (variant.base_price - unit_price) * item.quantity;
    return { product, variant, quantity: item.quantity, unit_price, subtotal, tier, tierSavings };
  }).filter(Boolean);
};

// Totales del carrito
export const selectCartTotals = (state) => {
  const lines = selectCartLines(state);
  const subtotal = lines.reduce((acc, l) => acc + l.subtotal, 0);
  const tierSavings = lines.reduce((acc, l) => acc + l.tierSavings, 0);
  let discount = 0;
  let shipping = lines.length === 0 ? 0 : 12_500;
  const coupon = state.cart.coupon;
  if (coupon) {
    if (coupon.discount_type === 'percent')  discount = Math.round(subtotal * (coupon.value / 100));
    if (coupon.discount_type === 'shipping') shipping = 0;
  }
  const total = subtotal - discount + shipping;
  const itemCount = lines.reduce((acc, l) => acc + l.quantity, 0);
  return { subtotal, discount, shipping, total, tierSavings, itemCount };
};
