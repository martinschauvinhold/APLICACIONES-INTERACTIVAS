import { createSlice } from '@reduxjs/toolkit';
import { CATEGORIES } from '../../data/categories';
import { PRODUCTS } from '../../data/products';

const initialState = {
  products: PRODUCTS,
  categories: CATEGORIES,
  filters: { categoryId: null, search: '', sort: 'relevance', inStockOnly: false },
};

const catalogSlice = createSlice({
  name: 'catalog',
  initialState,
  reducers: {
    setFilter: (state, action) => {
      Object.assign(state.filters, action.payload);
    },
    clearFilters: (state) => {
      state.filters = initialState.filters;
    },
    hydrate: (state, action) => {
      // Reemplaza productos por los del backend
      state.products = action.payload.products;
    },
    // ── ADMIN ──────────────────────────────────────────────
    updateTier: (state, action) => {
      // Edita el precio de un tramo (price_tier) de una variante
      const { variantId, tierIndex, unit_price } = action.payload;
      for (const p of state.products) {
        const v = p.variants.find((v) => v.variant_id === variantId);
        if (v && v.tiers[tierIndex]) { v.tiers[tierIndex].unit_price = unit_price; break; }
      }
    },
    updateStock: (state, action) => {
      const { variantId, stock } = action.payload;
      for (const p of state.products) {
        const v = p.variants.find((v) => v.variant_id === variantId);
        if (v) { v.stock = stock; break; }
      }
    },
    updateBasePrice: (state, action) => {
      const { variantId, base_price } = action.payload;
      for (const p of state.products) {
        const v = p.variants.find((v) => v.variant_id === variantId);
        if (v) { v.base_price = base_price; break; }
      }
    },
    toggleProductActive: (state, action) => {
      const p = state.products.find((p) => p.product_id === action.payload.productId);
      if (p) p.is_active = p.is_active === false ? true : false;
    },
  },
});

export const {
  setFilter, clearFilters, hydrate,
  updateTier, updateStock, updateBasePrice, toggleProductActive,
} = catalogSlice.actions;
export default catalogSlice.reducer;
