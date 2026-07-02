import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

export const fetchRefundsByReturn = createAsyncThunk('refunds/byReturn', async (returnId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/refunds/return/${returnId}`);
    return { returnId, data };
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const createRefund = createAsyncThunk('refunds/create', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/refunds', body);
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo crear el reembolso'));
  }
});

export const updateRefundStatus = createAsyncThunk('refunds/updateStatus', async ({ id, status }, { rejectWithValue }) => {
  try {
    const { data } = await http.put(`/refunds/${id}/status`, { status });
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

const initialState = { byReturn: {}, mutating: false, mutateError: null };

const refundsSlice = createSlice({
  name: 'refunds',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchRefundsByReturn.fulfilled, (s, a) => { s.byReturn[a.payload.returnId] = a.payload.data; })
      .addCase(createRefund.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(createRefund.fulfilled, (s) => { s.mutating = false; })
      .addCase(createRefund.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; })
      .addCase(updateRefundStatus.fulfilled, (s) => { s.mutating = false; });
  },
});

export const selectRefundsByReturn = (returnId) => (s) => s.refunds.byReturn[returnId] || [];

export default refundsSlice.reducer;
