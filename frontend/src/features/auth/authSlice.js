import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';
import { setToken } from '../../api/client';

function normalizeUser(u) {
  return {
    user_id: u.id,
    username: u.username,
    email: u.email,
    first_name: u.firstName,
    last_name: u.lastName,
    role: u.role,
    phone: u.phone,
  };
}

export const login = createAsyncThunk('auth/login', async ({ email, password }, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/auth/login', { email, password });
    setToken(data.token);
    const me = await http.get('/users/me');
    return normalizeUser(me.data);
  } catch (err) {
    setToken(null);
    return rejectWithValue(apiErrorMessage(err, 'No se pudo iniciar sesión'));
  }
});

export const register = createAsyncThunk('auth/register', async (form, { rejectWithValue }) => {
  try {
    const { username, email, password, first_name, last_name, phone } = form;
    const { data } = await http.post('/auth/register', {
      username, email, password, firstName: first_name, lastName: last_name, phone,
    });
    setToken(data.token);
    const me = await http.get('/users/me');
    return normalizeUser(me.data);
  } catch (err) {
    setToken(null);
    return rejectWithValue(apiErrorMessage(err, 'No se pudo registrar'));
  }
});

export const fetchMe = createAsyncThunk('auth/fetchMe', async (_, { rejectWithValue }) => {
  try {
    const me = await http.get('/users/me');
    return normalizeUser(me.data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'Sesión expirada'));
  }
});

export const logout = createAsyncThunk('auth/logout', async () => {
  try {
    await http.post('/auth/logout');
  } catch {
    // best-effort: aunque falle la red, limpiamos la sesión local igual
  } finally {
    setToken(null);
  }
});

const initialState = { user: null, isAuthenticated: false, loading: false, error: null };

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    const pending = (state) => {
      state.loading = true;
      state.error = null;
    };
    const fulfilled = (state, action) => {
      state.loading = false;
      state.user = action.payload;
      state.isAuthenticated = true;
    };
    const rejected = (state, action) => {
      state.loading = false;
      state.error = action.payload;
      state.user = null;
      state.isAuthenticated = false;
    };
    builder
      .addCase(login.pending, pending)
      .addCase(login.fulfilled, fulfilled)
      .addCase(login.rejected, rejected)
      .addCase(register.pending, pending)
      .addCase(register.fulfilled, fulfilled)
      .addCase(register.rejected, rejected)
      .addCase(fetchMe.fulfilled, (state, action) => {
        state.user = action.payload;
        state.isAuthenticated = true;
      })
      .addCase(fetchMe.rejected, (state) => {
        state.user = null;
        state.isAuthenticated = false;
      })
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.isAuthenticated = false;
        state.error = null;
      });
  },
});

export const { clearError } = authSlice.actions;

export const selectUser = (s) => s.auth.user;
export const selectIsAuthenticated = (s) => s.auth.isAuthenticated;
export const selectAuthLoading = (s) => s.auth.loading;
export const selectAuthError = (s) => s.auth.error;

export default authSlice.reducer;
