import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

function normalizeInventory(i) {
  return {
    inventory_id: i.id,
    variant_id: i.variantId ?? null,
    variant_sku: i.variantSku ?? null,
    warehouse_id: i.warehouseId ?? null,
    warehouse_name: i.warehouseName ?? null,
    stock_quantity: i.stockQuantity,
    last_updated: i.lastUpdated,
  };
}

export const fetchInventoryByVariant = createAsyncThunk('inventory/byVariant', async (variantId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/inventory/variant/${variantId}`);
    return { variantId, data: (Array.isArray(data) ? data : []).map(normalizeInventory) };
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const createInventory = createAsyncThunk('inventory/create', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/inventory', body);
    return normalizeInventory(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo crear el inventario'));
  }
});

export const updateInventory = createAsyncThunk('inventory/update', async ({ id, body }, { rejectWithValue }) => {
  try {
    const { data } = await http.put(`/inventory/${id}`, body);
    return normalizeInventory(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo actualizar el stock'));
  }
});

const initialState = { byVariant: {}, mutating: false, mutateError: null };

const inventorySlice = createSlice({
  name: 'inventory',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchInventoryByVariant.fulfilled, (s, a) => { s.byVariant[a.payload.variantId] = a.payload.data; })
      .addCase(createInventory.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(createInventory.fulfilled, (s) => { s.mutating = false; })
      .addCase(createInventory.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; })
      .addCase(updateInventory.fulfilled, (s) => { s.mutating = false; })
      .addCase(updateInventory.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; });
  },
});

export const selectInventoryByVariant = (variantId) => (s) => s.inventory.byVariant[variantId];

export default inventorySlice.reducer;
