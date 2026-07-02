import { createSlice } from '@reduxjs/toolkit';

const uiSlice = createSlice({
  name: 'ui',
  initialState: {
    cartDrawerOpen: false,
    quickViewId: null,
    recentlyViewed: [],
  },
  reducers: {
    toggleCartDrawer(state) {
      state.cartDrawerOpen = !state.cartDrawerOpen;
    },
    closeCartDrawer(state) {
      state.cartDrawerOpen = false;
    },
    openQuickView(state, action) {
      state.quickViewId = action.payload;
    },
    closeQuickView(state) {
      state.quickViewId = null;
    },
    trackView(state, action) {
      const id = action.payload;
      state.recentlyViewed = [id, ...state.recentlyViewed.filter((x) => x !== id)].slice(0, 6);
    },
  },
});

export const { toggleCartDrawer, closeCartDrawer, openQuickView, closeQuickView, trackView } = uiSlice.actions;

export const selectCartDrawerOpen = (s) => s.ui.cartDrawerOpen;
export const selectQuickViewId = (s) => s.ui.quickViewId;
export const selectRecentlyViewed = (s) => s.ui.recentlyViewed;

export default uiSlice.reducer;
