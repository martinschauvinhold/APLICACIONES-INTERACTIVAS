import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getCatalog } from '../../api/catalog';
import { apiErrorMessage } from '../../api/axios';
import { getProductDetail, createProduct as createProductApi, deleteProduct as deleteProductApi, uploadProductImage as uploadProductImageApi } from './productsApi';

export const fetchCatalog = createAsyncThunk('products/fetchCatalog', async (_, { rejectWithValue }) => {
  try {
    const { products } = await getCatalog();
    return products;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo cargar el catálogo'));
  }
});

export const fetchProductById = createAsyncThunk('products/fetchById', async (id, { rejectWithValue }) => {
  try {
    return await getProductDetail(id);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo cargar el producto'));
  }
});

export const createProduct = createAsyncThunk('products/create', async (payload, { rejectWithValue }) => {
  try {
    return await createProductApi(payload);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo crear el producto'));
  }
});

export const deleteProduct = createAsyncThunk('products/delete', async (id, { rejectWithValue }) => {
  try {
    return await deleteProductApi(id);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo eliminar el producto'));
  }
});

export const uploadProductImage = createAsyncThunk('products/uploadImage', async ({ productId, file }, { rejectWithValue }) => {
  try {
    return await uploadProductImageApi(productId, file);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo subir la imagen'));
  }
});

const initialState = {
  items: [],
  byId: {},
  loading: false,
  error: null,
  detailLoading: false,
  detailError: null,
  mutating: false,
  mutateError: null,
};

const productsSlice = createSlice({
  name: 'products',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchCatalog.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCatalog.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchCatalog.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(fetchProductById.pending, (state) => {
        state.detailLoading = true;
        state.detailError = null;
      })
      .addCase(fetchProductById.fulfilled, (state, action) => {
        state.detailLoading = false;
        state.byId[action.payload.product_id] = action.payload;
      })
      .addCase(fetchProductById.rejected, (state, action) => {
        state.detailLoading = false;
        state.detailError = action.payload;
      })
      .addCase(createProduct.pending, (state) => {
        state.mutating = true;
        state.mutateError = null;
      })
      .addCase(createProduct.fulfilled, (state) => {
        state.mutating = false;
      })
      .addCase(createProduct.rejected, (state, action) => {
        state.mutating = false;
        state.mutateError = action.payload;
      })
      .addCase(deleteProduct.pending, (state) => {
        state.mutating = true;
        state.mutateError = null;
      })
      .addCase(deleteProduct.fulfilled, (state, action) => {
        state.mutating = false;
        state.items = state.items.filter((p) => p.product_id !== action.payload);
      })
      .addCase(deleteProduct.rejected, (state, action) => {
        state.mutating = false;
        state.mutateError = action.payload;
      })
      .addCase(uploadProductImage.pending, (state) => {
        state.mutating = true;
        state.mutateError = null;
      })
      .addCase(uploadProductImage.fulfilled, (state) => {
        state.mutating = false;
      })
      .addCase(uploadProductImage.rejected, (state, action) => {
        state.mutating = false;
        state.mutateError = action.payload;
      });
  },
});

export const selectProducts = (s) => s.products.items;
export const selectProductsLoading = (s) => s.products.loading;
export const selectProductsError = (s) => s.products.error;
export const selectProductDetail = (id) => (s) => s.products.byId[id];
export const selectDetailLoading = (s) => s.products.detailLoading;
export const selectDetailError = (s) => s.products.detailError;
export const selectProductsMutating = (s) => s.products.mutating;
export const selectProductsMutateError = (s) => s.products.mutateError;

export default productsSlice.reducer;
