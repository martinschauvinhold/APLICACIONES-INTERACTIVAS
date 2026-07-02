import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';

function normalizeTicket(tk) {
  return {
    ticket_id: tk.id,
    user_id: tk.userId ?? tk.user?.id ?? null,
    subject: tk.subject,
    status: String(tk.status || '').toLowerCase(),
    order_id: tk.orderId ?? tk.order?.id ?? null,
    created_at: tk.createdAt,
  };
}

function normalizeMessage(m) {
  return {
    message_id: m.id,
    ticket_id: m.ticketId ?? m.ticket?.id ?? null,
    sender_id: m.senderId ?? m.sender?.id ?? null,
    content: m.content,
    created_at: m.createdAt,
  };
}

function asList(data) {
  return Array.isArray(data) ? data : data?.content || [];
}

export const fetchTickets = createAsyncThunk('tickets/fetchAll', async (_, { rejectWithValue }) => {
  try {
    const { data } = await http.get('/support/tickets');
    return asList(data).map(normalizeTicket);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudieron cargar los tickets'));
  }
});

export const createTicket = createAsyncThunk('tickets/create', async (body, { rejectWithValue }) => {
  try {
    const { data } = await http.post('/support/tickets', body);
    return normalizeTicket(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo crear el ticket'));
  }
});

export const updateTicketStatus = createAsyncThunk('tickets/updateStatus', async ({ id, status }, { rejectWithValue }) => {
  try {
    const { data } = await http.put(`/support/tickets/${id}/status`, { status });
    return normalizeTicket(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo actualizar el ticket'));
  }
});

export const fetchTicketMessages = createAsyncThunk('tickets/fetchMessages', async (id, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/support/tickets/${id}/messages`);
    return { id, messages: asList(data).map(normalizeMessage) };
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudieron cargar los mensajes'));
  }
});

export const addTicketMessage = createAsyncThunk('tickets/addMessage', async ({ id, body }, { rejectWithValue }) => {
  try {
    const { data } = await http.post(`/support/tickets/${id}/messages`, body);
    return { id, message: normalizeMessage(data) };
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo enviar el mensaje'));
  }
});

const initialState = {
  items: [],
  messagesByTicket: {},
  loading: false,
  error: null,
  messagesLoading: false,
  messagesError: null,
  mutating: false,
  mutateError: null,
};

const ticketsSlice = createSlice({
  name: 'tickets',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchTickets.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchTickets.fulfilled, (s, a) => { s.loading = false; s.items = a.payload; })
      .addCase(fetchTickets.rejected, (s, a) => { s.loading = false; s.error = a.payload; })
      .addCase(createTicket.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(createTicket.fulfilled, (s, a) => { s.mutating = false; s.items.unshift(a.payload); })
      .addCase(createTicket.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; })
      .addCase(updateTicketStatus.fulfilled, (s, a) => {
        const i = s.items.findIndex((tk) => tk.ticket_id === a.payload.ticket_id);
        if (i !== -1) s.items[i] = a.payload;
      })
      .addCase(fetchTicketMessages.pending, (s) => { s.messagesLoading = true; s.messagesError = null; })
      .addCase(fetchTicketMessages.fulfilled, (s, a) => {
        s.messagesLoading = false;
        s.messagesByTicket[a.payload.id] = a.payload.messages;
      })
      .addCase(fetchTicketMessages.rejected, (s, a) => { s.messagesLoading = false; s.messagesError = a.payload; })
      .addCase(addTicketMessage.pending, (s) => { s.mutating = true; s.mutateError = null; })
      .addCase(addTicketMessage.fulfilled, (s, a) => {
        s.mutating = false;
        const list = s.messagesByTicket[a.payload.id] || [];
        s.messagesByTicket[a.payload.id] = [...list, a.payload.message];
      })
      .addCase(addTicketMessage.rejected, (s, a) => { s.mutating = false; s.mutateError = a.payload; });
  },
});

export const selectTickets = (s) => s.tickets.items;
export const selectTicketsLoading = (s) => s.tickets.loading;
export const selectTicketsError = (s) => s.tickets.error;
export const selectTicketMessages = (id) => (s) => s.tickets.messagesByTicket[id] || [];
export const selectMessagesLoading = (s) => s.tickets.messagesLoading;
export const selectTicketMutating = (s) => s.tickets.mutating;
export const selectTicketMutateError = (s) => s.tickets.mutateError;

export default ticketsSlice.reducer;
