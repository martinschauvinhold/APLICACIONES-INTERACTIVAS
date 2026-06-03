import { createSlice } from '@reduxjs/toolkit';
import { VALID_COUPONS } from '../../data/coupons';

const initialState = {
  items: [],         // [{ variantId, quantity }]
  couponCode: null,
  coupon: null,
};

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    add: (state, action) => {
      const { variantId, quantity } = action.payload;
      const found = state.items.find((i) => i.variantId === variantId);
      if (found) found.quantity += quantity;
      else state.items.push({ variantId, quantity });
    },
    setQuantity: (state, action) => {
      const { variantId, quantity } = action.payload;
      if (quantity <= 0) {
        state.items = state.items.filter((i) => i.variantId !== variantId);
      } else {
        const found = state.items.find((i) => i.variantId === variantId);
        if (found) found.quantity = quantity;
      }
    },
    remove: (state, action) => {
      state.items = state.items.filter((i) => i.variantId !== action.payload.variantId);
    },
    applyCoupon: (state, action) => {
      const code = (action.payload.code || '').toUpperCase().trim();
      state.couponCode = code || null;
      state.coupon = VALID_COUPONS[code] || null;
    },
    removeCoupon: (state) => {
      state.couponCode = null;
      state.coupon = null;
    },
    clear: () => initialState,
  },
});

export const { add, setQuantity, remove, applyCoupon, removeCoupon, clear } = cartSlice.actions;
export default cartSlice.reducer;
