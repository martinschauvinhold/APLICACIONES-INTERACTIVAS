import { createSlice } from '@reduxjs/toolkit';

const INITIAL = { categoryId: null, search: '', sort: 'relevance', inStockOnly: false };

const filtersSlice = createSlice({
  name: 'filters',
  initialState: INITIAL,
  reducers: {
    setFilter(state, action) {
      return { ...state, ...action.payload };
    },
    clearFilters() {
      return INITIAL;
    },
  },
});

export const { setFilter, clearFilters } = filtersSlice.actions;

export const selectFilters = (s) => s.filters;

export default filtersSlice.reducer;
