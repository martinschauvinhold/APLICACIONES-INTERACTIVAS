import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  list: [],
  currentTracking: null,
};

const ordersSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    place: (state, action) => {
      const o = action.payload;
      state.list.unshift(o);
      state.currentTracking = o.order_id;
    },
    advanceTracking: (state, action) => {
      const { orderId, checkpoint, status } = action.payload;
      const order = state.list.find((o) => o.order_id === orderId);
      if (!order) return;
      order.tracking.push(checkpoint);
      if (status) order.status = status;
    },
    hydrate: (state, action) => {
      const list = (action.payload.list || []).map((o) => ({
        order_id: o.orderId || o.order_id || o.id,
        user_id: o.userId || o.user_id,
        shipping_address_id: o.shippingAddressId || o.shipping_address_id,
        status: o.status || 'pending',
        items: o.items || [],
        totals: o.totals || { subtotal: o.totalAmount || 0, discount: 0, shipping: 0, total: o.totalAmount || 0, tierSavings: 0, itemCount: (o.items || []).length },
        payment: o.payment || { method: 'credit_card', card_last4: '****' },
        tracking_number: o.trackingNumber || o.tracking_number || '—',
        tracking: o.tracking || [{ checkpoint: 'Pedido recibido', status: o.status || 'pending', recorded_at: o.createdAt || new Date().toISOString() }],
        address: o.address || { street: '—', city: '—', state: '—', zip_code: '—' },
        created_at: o.createdAt || o.created_at || new Date().toISOString(),
      }));
      state.list = list;
    },
  },
});

export const { place, advanceTracking, hydrate } = ordersSlice.actions;
export default ordersSlice.reducer;
