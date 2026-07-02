import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

export const fetchTrackingByDelivery = createAsyncThunk('tracking/byDelivery', async (deliveryId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/tracking/delivery/${deliveryId}`);
    return { deliveryId, data };
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const createTracking = createAsyncThunk('tracking/create', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/tracking', body);
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const updateTrackingStatus = createAsyncThunk('tracking/updateStatus', async ({ id, status }, { rejectWithValue }) => {
  try {
    const { data } = await http.put(`/tracking/${id}/status`, { status });
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

const initialState = { byDelivery: {}, mutating: false, mutateError: null };

const shipmentTrackingSlice = createSlice({
  name: 'shipmentTracking',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchTrackingByDelivery.fulfilled, (s, a) => { s.byDelivery[a.payload.deliveryId] = a.payload.data; })
      .addCase(createTracking.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(createTracking.fulfilled, (s) => { s.mutating = false; })
      .addCase(createTracking.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; })
      .addCase(updateTrackingStatus.fulfilled, (s) => { s.mutating = false; });
  },
});

export const selectTrackingByDelivery = (deliveryId) => (s) => s.shipmentTracking.byDelivery[deliveryId] || [];

export default shipmentTrackingSlice.reducer;
