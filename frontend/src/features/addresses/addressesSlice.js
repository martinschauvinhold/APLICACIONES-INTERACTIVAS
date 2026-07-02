import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';
import { normalizeAddress } from '../../api/addresses';
import { logout } from '../auth/authSlice';

export const fetchAddressesByUser = createAsyncThunk('addresses/byUser', async (userId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/addresses/user/${userId}`);
    return data.map(normalizeAddress);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudieron cargar las direcciones'));
  }
});

export const createAddress = createAsyncThunk('addresses/create', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/addresses', body);
    return normalizeAddress(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo crear la dirección'));
  }
});

export const updateAddress = createAsyncThunk('addresses/update', async ({ id, body }, { rejectWithValue }) => {
  try {
    const { data } = await http.put(`/addresses/${id}`, body);
    return normalizeAddress(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo actualizar la dirección'));
  }
});

export const deleteAddress = createAsyncThunk('addresses/delete', async (id, { rejectWithValue }) => {
  try {
    await http.delete(`/addresses/${id}`);
    return id;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo eliminar la dirección'));
  }
});

const initialState = { items: [], loading: false, error: null, mutating: false, mutateError: null };

const addressesSlice = createSlice({
  name: 'addresses',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(logout.fulfilled, (s) => { s.items = []; })
      .addCase(fetchAddressesByUser.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchAddressesByUser.fulfilled, (s, a) => { s.loading = false; s.items = a.payload; })
      .addCase(fetchAddressesByUser.rejected, (s, a) => { s.loading = false; s.error = a.payload; })
      .addCase(createAddress.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(createAddress.fulfilled, (s, a) => { s.mutating = false; s.items.push(a.payload); })
      .addCase(createAddress.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; })
      .addCase(updateAddress.fulfilled, (s, a) => {
        s.mutating = false;
        const i = s.items.findIndex((x) => x.address_id === a.payload.address_id);
        if (i !== -1) s.items[i] = a.payload;
      })
      .addCase(deleteAddress.fulfilled, (s, a) => {
        s.items = s.items.filter((x) => x.address_id !== a.payload);
      });
  },
});

export const selectAddresses = (s) => s.addresses.items;
export const selectAddressesLoading = (s) => s.addresses.loading;

export default addressesSlice.reducer;
