import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

export const fetchNotifications = createAsyncThunk('notifications/fetchAll', async (_, { rejectWithValue }) => {
  try {
    const { data } = await http.get('/notifications');
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const fetchUnreadNotifications = createAsyncThunk('notifications/unread', async (_, { rejectWithValue }) => {
  try {
    const { data } = await http.get('/notifications/unread');
    return data;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

export const markNotificationRead = createAsyncThunk('notifications/markRead', async (id, { rejectWithValue }) => {
  try {
    await http.put(`/notifications/${id}/read`);
    return id;
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err));
  }
});

const initialState = { items: [], unread: [], loading: false, error: null };

const notificationsSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotifications.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchNotifications.fulfilled, (s, a) => { s.loading = false; s.items = a.payload; })
      .addCase(fetchNotifications.rejected, (s, a) => { s.loading = false; s.error = a.payload; })
      .addCase(fetchUnreadNotifications.fulfilled, (s, a) => { s.unread = a.payload; })
      .addCase(markNotificationRead.fulfilled, (s, a) => {
        s.unread = s.unread.filter((n) => n.id !== a.payload);
      });
  },
});

export const selectNotifications = (s) => s.notifications.items;
export const selectUnreadNotifications = (s) => s.notifications.unread;

export default notificationsSlice.reducer;
