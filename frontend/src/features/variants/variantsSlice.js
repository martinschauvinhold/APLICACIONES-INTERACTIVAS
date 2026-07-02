import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

export const fetchVariantsByProduct = createAsyncThunk('variants/byProduct', async (productId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/variants/product/${productId}`);
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const createVariant = createAsyncThunk('variants/create', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/variants', body);
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo crear la variante'));
  }
});

export const updateVariant = createAsyncThunk('variants/update', async ({ id, body }, { rejectWithValue }) => {
  try {
    const { data } = await http.put(`/variants/${id}`, body);
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo actualizar la variante'));
  }
});

export const deleteVariant = createAsyncThunk('variants/delete', async (id, { rejectWithValue }) => {
  try {
    await http.delete(`/variants/${id}`);
    return id;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo eliminar la variante'));
  }
});

/** Crea el tier si todavía no existe en el back (tier_id null = fallback sintético), si no lo actualiza. */
export const upsertVariantTier = createAsyncThunk(
  'variants/upsertTier',
  async ({ variantId, tier_id, min_quantity, unit_price }, { rejectWithValue }) => {
    const body = { minQuantity: min_quantity, unitPrice: unit_price };
    try {
      const { data } = tier_id
        ? await http.put(`/variants/${variantId}/tiers/${tier_id}`, body)
        : await http.post(`/variants/${variantId}/tiers`, body);
      return data;
    } catch (err) {
      return rejectWithValue(apiErrorMessage(err, 'No se pudo guardar el tier'));
    }
  },
);

const initialState = { byProduct: {}, loading: false, error: null, mutating: false, mutateError: null };

const variantsSlice = createSlice({
  name: 'variants',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchVariantsByProduct.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchVariantsByProduct.fulfilled, (s, a) => {
        s.loading = false;
        s.byProduct[a.meta.arg] = a.payload;
      })
      .addCase(fetchVariantsByProduct.rejected, (s, a) => { s.loading = false; s.error = a.payload; })
      .addCase(createVariant.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(createVariant.fulfilled, (s) => { s.mutating = false; })
      .addCase(createVariant.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; })
      .addCase(updateVariant.fulfilled, (s) => { s.mutating = false; })
      .addCase(deleteVariant.fulfilled, (s) => { s.mutating = false; })
      .addCase(upsertVariantTier.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(upsertVariantTier.fulfilled, (s) => { s.mutating = false; })
      .addCase(upsertVariantTier.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; });
  },
});

export const selectVariantsByProduct = (productId) => (s) => s.variants.byProduct[productId] || [];
export const selectVariantsMutating = (s) => s.variants.mutating;

export default variantsSlice.reducer;
