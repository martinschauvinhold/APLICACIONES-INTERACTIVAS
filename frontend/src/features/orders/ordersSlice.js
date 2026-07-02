import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';
import { processPayment } from '../../api/payments';
import { logout } from '../auth/authSlice';

function normalizeOrderAddress(a) {
  if (!a) return null;
  return {
    address_id: a.id,
    street: a.street,
    city: a.city,
    state: a.state,
    zip_code: a.zipCode,
    reference_note: a.referenceNote,
  };
}

function normalizeOrder(o) {
  return {
    order_id: o.id,
    user_id: o.userId ?? null,
    status: String(o.status || '').toLowerCase(),
    total_amount: o.totalAmount != null ? Number(o.totalAmount) : 0,
    currency: o.currency || 'ARS',
    address: normalizeOrderAddress(o.shippingAddress),
    created_at: o.createdAt,
  };
}

function parseAttrs(attributes) {
  if (!attributes) return {};
  try {
    return JSON.parse(attributes);
  } catch {
    return {};
  }
}

function normalizeOrderItem(it) {
  const unitPriceAtTime = Number(it.unitPriceAtTime);
  return {
    product_id: it.productId ?? null,
    variant_id: it.variantId ?? null,
    sku: it.variantSku,
    product_name: it.productName,
    attrs: parseAttrs(it.variantAttributes),
    quantity: it.quantity,
    unit_price_at_time: unitPriceAtTime,
    discount_applied: Number(it.discountApplied || 0),
    subtotal: Number(it.subtotal),
  };
}

function buildTotals(items) {
  const subtotal = items.reduce((acc, it) => acc + it.unit_price_at_time * it.quantity, 0);
  const discount = items.reduce((acc, it) => acc + it.discount_applied * it.quantity, 0);
  return { subtotal, discount, shipping: 0, total: subtotal - discount };
}

export const placeOrder = createAsyncThunk('orders/place', async ({ userId, shippingAddressId, items, couponCode, paymentMethod }, { rejectWithValue }) => {
  try {
    const { data: created } = await http.post('/orders', {
      userId,
      shippingAddressId,
      couponCode: couponCode || null,
      items: items.map(({ variantId, quantity }) => ({ variantId, quantity })),
    });
    const orderId = created.id;
    const payment = await processPayment({ orderId, paymentMethod });
    if (payment.status !== 'paid') throw new Error('El pago fue rechazado. Probá con otro método o con otra tarjeta.');
    const { data: rawItems } = await http.get(`/order-items/order/${orderId}`);
    const normalizedItems = (Array.isArray(rawItems) ? rawItems : []).map(normalizeOrderItem);
    const totals = buildTotals(normalizedItems);
    return {
      ...normalizeOrder(created),
      status: 'paid',
      items: normalizedItems,
      totals,
      payment: { method: paymentMethod, transaction_id: payment.transaction_id },
      tracking_number: 'CA-' + String(orderId).padStart(8, '0'),
      tracking: [
        { checkpoint: 'Pedido recibido', status: 'pending', recorded_at: created.createdAt },
        { checkpoint: 'Pago confirmado', status: 'paid', recorded_at: new Date().toISOString() },
      ],
    };
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo procesar el pedido'));
  }
});

export const fetchOrdersByUser = createAsyncThunk('orders/fetchByUser', async (userId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/orders/user/${userId}`);
    return data.map(normalizeOrder);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudieron cargar los pedidos'));
  }
});

export const fetchAllOrders = createAsyncThunk('orders/fetchAll', async (_, { rejectWithValue }) => {
  try {
    const { data } = await http.get('/orders');
    return data.map(normalizeOrder);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudieron cargar los pedidos'));
  }
});

export const cancelOrder = createAsyncThunk('orders/cancel', async (orderId, { rejectWithValue }) => {
  try {
    const { data } = await http.put(`/orders/${orderId}/cancel`);
    return normalizeOrder(data);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo cancelar el pedido'));
  }
});

export const fetchOrderDetail = createAsyncThunk('orders/fetchDetail', async (orderId, { rejectWithValue }) => {
  try {
    const { data } = await http.get(`/order-items/order/${orderId}`);
    const items = (Array.isArray(data) ? data : []).map(normalizeOrderItem);
    return { order_id: orderId, items, totals: buildTotals(items) };
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudo cargar el detalle del pedido'));
  }
});

const initialState = {
  list: [],
  loading: false,
  error: null,
  placing: false,
  placeError: null,
  currentTracking: null,
  cancelling: false,
  cancelError: null,
  detailById: {},
  detailLoading: false,
  detailError: null,
};

const ordersSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    const onList = (state, action) => {
      state.loading = false;
      state.list = action.payload;
    };
    builder
      .addCase(logout.fulfilled, () => initialState)
      .addCase(placeOrder.pending, (s) => { s.placing = true; s.placeError = null; })
      .addCase(placeOrder.fulfilled, (s, a) => {
        s.placing = false;
        s.list.unshift(a.payload);
        s.currentTracking = a.payload.order_id;
      })
      .addCase(placeOrder.rejected, (s, a) => { s.placing = false; s.placeError = a.payload; })
      .addCase(fetchOrdersByUser.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchOrdersByUser.fulfilled, onList)
      .addCase(fetchOrdersByUser.rejected, (s, a) => { s.loading = false; s.error = a.payload; })
      .addCase(fetchAllOrders.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchAllOrders.fulfilled, onList)
      .addCase(fetchAllOrders.rejected, (s, a) => { s.loading = false; s.error = a.payload; })
      .addCase(cancelOrder.pending, (s) => { s.cancelling = true; s.cancelError = null; })
      .addCase(cancelOrder.fulfilled, (s, a) => {
        s.cancelling = false;
        const i = s.list.findIndex((o) => o.order_id === a.payload.order_id);
        if (i !== -1) s.list[i] = { ...s.list[i], status: a.payload.status };
      })
      .addCase(cancelOrder.rejected, (s, a) => { s.cancelling = false; s.cancelError = a.payload; })
      .addCase(fetchOrderDetail.pending, (s) => { s.detailLoading = true; s.detailError = null; })
      .addCase(fetchOrderDetail.fulfilled, (s, a) => {
        s.detailLoading = false;
        s.detailById[a.payload.order_id] = { items: a.payload.items, totals: a.payload.totals };
      })
      .addCase(fetchOrderDetail.rejected, (s, a) => { s.detailLoading = false; s.detailError = a.payload; });
  },
});

export const selectOrdersPlacing = (s) => s.orders.placing;
export const selectOrdersPlaceError = (s) => s.orders.placeError;
export const selectCurrentTracking = (s) => s.orders.currentTracking;
export const selectOrders = (s) => s.orders.list;
export const selectOrdersLoading = (s) => s.orders.loading;
export const selectOrdersError = (s) => s.orders.error;
export const selectCancelling = (s) => s.orders.cancelling;
export const selectCancelError = (s) => s.orders.cancelError;
export const selectOrderById = (id) => (s) => s.orders.list.find((o) => o.order_id === id);
export const selectOrderDetail = (id) => (s) => s.orders.detailById[id];
export const selectOrderDetailLoading = (s) => s.orders.detailLoading;

export default ordersSlice.reducer;
