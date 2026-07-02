import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

export const fetchPaymentsByOrder = createAsyncThunk('payments/byOrder', async (orderId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/payments/order/${orderId}`);
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const processPayment = createAsyncThunk('payments/process', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/payments', body);
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo procesar el pago'));
  }
});

const initialState = { byOrder: {}, processing: false, error: null };

const paymentsSlice = createSlice({
  name: 'payments',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchPaymentsByOrder.fulfilled, (s, a) => { s.byOrder[a.meta.arg] = a.payload; })
      .addCase(processPayment.pending, (s) => { s.processing = true; s.error = null; })
      .addCase(processPayment.fulfilled, (s) => { s.processing = false; })
      .addCase(processPayment.rejected, (s, a) => { s.processing = false; s.error = a.payload; });
  },
});

export const selectPaymentsByOrder = (orderId) => (s) => s.payments.byOrder[orderId] || [];
export const selectPaymentProcessing = (s) => s.payments.processing;

export default paymentsSlice.reducer;
