import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';
import { logout } from '../auth/authSlice';

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

export const fetchUsers = createAsyncThunk('users/fetchAll', async (_, { rejectWithValue }) => {
  try {
    const { data } = await http.get('/users');
    return data.map(normalizeUser);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudieron cargar los usuarios'));
  }
});

export const createUser = createAsyncThunk('users/create', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/users', body);
    return normalizeUser(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo crear el usuario'));
  }
});

export const updateUser = createAsyncThunk('users/update', async ({ id, body }, { rejectWithValue }) => {
  try {
    const { data } = await http.put(`/users/${id}`, body);
    return normalizeUser(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo actualizar el usuario'));
  }
});

export const deleteUser = createAsyncThunk('users/delete', async (id, { rejectWithValue }) => {
  try {
    await http.delete(`/users/${id}`);
    return id;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo eliminar el usuario'));
  }
});

const initialState = { items: [], loading: false, error: null, mutating: false, mutateError: null };

const usersSlice = createSlice({
  name: 'users',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(logout.fulfilled, () => initialState)
      .addCase(fetchUsers.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchUsers.fulfilled, (s, a) => { s.loading = false; s.items = a.payload; })
      .addCase(fetchUsers.rejected, (s, a) => { s.loading = false; s.error = a.payload; })
      .addCase(createUser.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(createUser.fulfilled, (s, a) => { s.mutating = false; s.items.push(a.payload); })
      .addCase(createUser.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; })
      .addCase(updateUser.fulfilled, (s, a) => {
        s.mutating = false;
        const i = s.items.findIndex((u) => u.user_id === a.payload.user_id);
        if (i !== -1) s.items[i] = a.payload;
      })
      .addCase(deleteUser.fulfilled, (s, a) => {
        s.items = s.items.filter((u) => u.user_id !== a.payload);
      });
  },
});

export const selectUsers = (s) => s.users.items;
export const selectUsersLoading = (s) => s.users.loading;
export const selectUsersError = (s) => s.users.error;
export const selectUsersMutating = (s) => s.users.mutating;

export default usersSlice.reducer;
