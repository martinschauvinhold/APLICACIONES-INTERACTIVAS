import { createSlice } from '@reduxjs/toolkit';

const INITIAL = {
  step: 0,
  addressId: 1,
  paymentMethod: 'credit_card',
  cardLast4: '4242',
  cardHolder: 'MARTIN GARCIA',
  cardExpiry: '08/29',
};

const checkoutSlice = createSlice({
  name: 'checkout',
  initialState: INITIAL,
  reducers: {
    setStep(state, action) {
      state.step = action.payload;
    },
    setAddress(state, action) {
      state.addressId = action.payload;
    },
    setPayment(state, action) {
      return { ...state, ...action.payload };
    },
    resetCheckout() {
      return INITIAL;
    },
  },
});

export const { setStep, setAddress, setPayment, resetCheckout } = checkoutSlice.actions;

export const selectCheckout = (s) => s.checkout;
export const selectCheckoutStep = (s) => s.checkout.step;
export const selectCheckoutAddressId = (s) => s.checkout.addressId;

export default checkoutSlice.reducer;
