import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { http, apiErrorMessage } from '../../api/axios';
import { logout } from '../auth/authSlice';

export const applyCoupon = createAsyncThunk('cart/applyCoupon', async (code, { rejectWithValue }) => {
  const normalized = (code || '').toUpperCase().trim();
  if (!normalized) return { code: null, coupon: null };
  try {
    const { data } = await http.get(`/coupons/validate/${normalized}`);
    return { code: normalized, coupon: data };
  } catch (err) {
    return rejectWithValue({ code: normalized, message: apiErrorMessage(err, 'Cupón inválido o vencido') });
  }
});

const initialState = {
  items: [],
  couponCode: null,
  coupon: null,
  couponError: null,
};

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    addToCart(state, action) {
      const { variantId, quantity } = action.payload;
      const found = state.items.find((i) => i.variantId === variantId);
      if (found) found.quantity += quantity;
      else state.items.push({ variantId, quantity });
    },
    setQuantity(state, action) {
      const { variantId, quantity } = action.payload;
      if (quantity <= 0) {
        state.items = state.items.filter((i) => i.variantId !== variantId);
        return;
      }
      const found = state.items.find((i) => i.variantId === variantId);
      if (found) found.quantity = quantity;
    },
    removeFromCart(state, action) {
      state.items = state.items.filter((i) => i.variantId !== action.payload.variantId);
    },
    removeCoupon(state) {
      state.couponCode = null;
      state.coupon = null;
      state.couponError = null;
    },
    clearCart() {
      return initialState;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(logout.fulfilled, () => initialState)
      .addCase(applyCoupon.pending, (state) => {
        state.couponError = null;
      })
      .addCase(applyCoupon.fulfilled, (state, action) => {
        state.couponCode = action.payload.code;
        state.coupon = action.payload.coupon;
        state.couponError = null;
      })
      .addCase(applyCoupon.rejected, (state, action) => {
        state.couponCode = action.payload?.code ?? null;
        state.coupon = null;
        state.couponError = action.payload?.message ?? 'Cupón inválido';
      });
  },
});

export const { addToCart, setQuantity, removeFromCart, removeCoupon, clearCart } = cartSlice.actions;

export const selectCartItems = (s) => s.cart.items;
export const selectCartCount = (s) => s.cart.items.reduce((n, i) => n + i.quantity, 0);
export const selectCoupon = (s) => s.cart.coupon;
export const selectCouponCode = (s) => s.cart.couponCode;
export const selectCouponError = (s) => s.cart.couponError;

export default cartSlice.reducer;
