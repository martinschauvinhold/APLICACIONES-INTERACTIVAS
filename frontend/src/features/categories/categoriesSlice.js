import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

export const fetchCategories = createAsyncThunk('categories/fetchAll', async (_, { rejectWithValue }) => {
  try {
    const { data } = await http.get('/categories');
    return data.map((c) => ({ id: c.id, name: c.description, slug: c.slug }));
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudieron cargar las categorías'));
  }
});

const initialState = { items: [], loading: false, error: null };

const categoriesSlice = createSlice({
  name: 'categories',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchCategories.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCategories.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchCategories.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const selectCategories = (s) => s.categories.items;
export const selectCategoriesLoading = (s) => s.categories.loading;
export const selectCategoriesError = (s) => s.categories.error;

export default categoriesSlice.reducer;
