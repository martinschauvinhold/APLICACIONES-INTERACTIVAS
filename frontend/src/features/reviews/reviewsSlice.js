import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

export const fetchReviewsByProduct = createAsyncThunk('reviews/byProduct', async (productId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/reviews/product/${productId}`);
    return { productId, data };
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const createReview = createAsyncThunk('reviews/create', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/reviews', body);
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo publicar la reseña'));
  }
});

export const deleteReview = createAsyncThunk('reviews/delete', async (id, { rejectWithValue }) => {
  try {
    await http.delete(`/reviews/${id}`);
    return id;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

const initialState = { byProduct: {}, mutating: false, mutateError: null };

const reviewsSlice = createSlice({
  name: 'reviews',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchReviewsByProduct.fulfilled, (s, a) => { s.byProduct[a.payload.productId] = a.payload.data; })
      .addCase(createReview.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(createReview.fulfilled, (s) => { s.mutating = false; })
      .addCase(createReview.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; })
      .addCase(deleteReview.fulfilled, (s) => { s.mutating = false; });
  },
});

export const selectReviewsByProduct = (productId) => (s) => s.reviews.byProduct[productId] || [];

export default reviewsSlice.reducer;
