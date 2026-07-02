import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../api/axios';

export function createCrudSlice({ name, basePath, idKey = 'id', normalize = (x) => x }) {
  const fetchAll = createAsyncThunk(`${name}/fetchAll`, async (_, { rejectWithValue }) => {
    try {
      const { data } = await http.get(basePath);
      return data.map(normalize);
    } catch (err) {
      return rejectWithValue(apiErrorMessage(err));
    }
  });

  const fetchById = createAsyncThunk(`${name}/fetchById`, async (id, { rejectWithValue }) => {
    try {
      const { data } = await http.get(`${basePath}/${id}`);
      return normalize(data);
    } catch (err) {
      return rejectWithValue(apiErrorMessage(err));
    }
  });

  const createItem = createAsyncThunk(`${name}/create`, async (body, { rejectWithValue }) => {
    try {
      const { data } = await http.post(basePath, body);
      return normalize(data);
    } catch (err) {
      return rejectWithValue(apiErrorMessage(err));
    }
  });

  const updateItem = createAsyncThunk(`${name}/update`, async ({ id, body }, { rejectWithValue }) => {
    try {
      const { data } = await http.put(`${basePath}/${id}`, body);
      return normalize(data);
    } catch (err) {
      return rejectWithValue(apiErrorMessage(err));
    }
  });

  const removeItem = createAsyncThunk(`${name}/remove`, async (id, { rejectWithValue }) => {
    try {
      await http.delete(`${basePath}/${id}`);
      return id;
    } catch (err) {
      return rejectWithValue(apiErrorMessage(err));
    }
  });

  const slice = createSlice({
    name,
    initialState: { items: [], current: null, loading: false, error: null, mutating: false, mutateError: null },
    reducers: {},
    extraReducers: (builder) => {
      builder
        .addCase(fetchAll.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(fetchAll.fulfilled, (state, action) => {
          state.loading = false;
          state.items = action.payload;
        })
        .addCase(fetchAll.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload;
        })
        .addCase(fetchById.fulfilled, (state, action) => {
          state.current = action.payload;
        })
        .addCase(createItem.pending, (state) => {
          state.mutating = true;
          state.mutateError = null;
        })
        .addCase(createItem.fulfilled, (state, action) => {
          state.mutating = false;
          state.items.push(action.payload);
        })
        .addCase(createItem.rejected, (state, action) => {
          state.mutating = false;
          state.mutateError = action.payload;
        })
        .addCase(updateItem.fulfilled, (state, action) => {
          state.mutating = false;
          const i = state.items.findIndex((x) => x[idKey] === action.payload[idKey]);
          if (i !== -1) state.items[i] = action.payload;
        })
        .addCase(removeItem.fulfilled, (state, action) => {
          state.items = state.items.filter((x) => x[idKey] !== action.payload);
        });
    },
  });

  return { reducer: slice.reducer, thunks: { fetchAll, fetchById, createItem, updateItem, removeItem } };
}
