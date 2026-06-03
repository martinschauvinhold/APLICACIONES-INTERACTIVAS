import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  cartDrawerOpen: false,
  quickViewId: null,
  recentlyViewed: [],
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    toggleCartDrawer: (state) => { state.cartDrawerOpen = !state.cartDrawerOpen; },
    closeCartDrawer: (state) => { state.cartDrawerOpen = false; },
    openQuickView: (state, action) => { state.quickViewId = action.payload.productId; },
    closeQuickView: (state) => { state.quickViewId = null; },
    trackView: (state, action) => {
      const id = action.payload.productId;
      state.recentlyViewed = [id, ...state.recentlyViewed.filter((x) => x !== id)].slice(0, 6);
    },
  },
});

export const {
  toggleCartDrawer, closeCartDrawer,
  openQuickView, closeQuickView,
  trackView,
} = uiSlice.actions;
export default uiSlice.reducer;
