import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';
import { logout } from '../auth/authSlice';

/**
 * fetchSalesBySeller — agrupa los OrderItem de este vendedor por orden, en la
 * forma que espera el panel (Vendedor.jsx): { order, items, subtotal, units }.
 */
export const fetchSalesBySeller = createAsyncThunk('seller/salesBySeller', async (sellerId, { rejectWithValue }) => {
  try {
    const { data: orderItems } = await http.get(`/order-items/seller/${sellerId}`);

    const byOrder = new Map();
    for (const it of orderItems) {
      const orderId = it.orderId;
      const entry = byOrder.get(orderId) || {
        order: {
          order_id: it.orderId,
          status: String(it.orderStatus || '').toLowerCase(),
          created_at: it.orderCreatedAt,
        },
        items: [],
        subtotal: 0,
        units: 0,
      };
      entry.items.push({ product_name: it.productName, quantity: it.quantity });
      entry.subtotal += Number(it.subtotal);
      entry.units += it.quantity;
      byOrder.set(orderId, entry);
    }

    return Array.from(byOrder.values()).sort((a, b) => b.order.order_id - a.order.order_id);
  } catch (err) {
    return rejectWithValue(apiErrorMessage(err, 'No se pudieron cargar las ventas'));
  }
});

const initialState = { sales: [], loading: false, error: null };

const sellerSlice = createSlice({
  name: 'seller',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(logout.fulfilled, () => initialState)
      .addCase(fetchSalesBySeller.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchSalesBySeller.fulfilled, (s, a) => { s.loading = false; s.sales = a.payload; })
      .addCase(fetchSalesBySeller.rejected, (s, a) => { s.loading = false; s.error = a.payload; });
  },
});

export const selectSellerSales = (s) => s.seller.sales;

export default sellerSlice.reducer;
