import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  step: 0, // 0 dirección, 1 pago, 2 revisión
  addressId: 1,
  paymentMethod: 'credit_card',
  cardLast4: '4242',
  cardHolder: 'MARTIN GARCIA',
  cardExpiry: '08/29',
};

const checkoutSlice = createSlice({
  name: 'checkout',
  initialState,
  reducers: {
    setStep: (state, action) => { state.step = action.payload.step; },
    setAddress: (state, action) => { state.addressId = action.payload.addressId; },
    setPayment: (state, action) => { Object.assign(state, action.payload); },
    reset: () => initialState,
  },
});

export const { setStep, setAddress, setPayment, reset } = checkoutSlice.actions;
export default checkoutSlice.reducer;
