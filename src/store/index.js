import { configureStore } from '@reduxjs/toolkit';
import catalog from './slices/catalogSlice';
import cart from './slices/cartSlice';
import checkout from './slices/checkoutSlice';
import session from './slices/sessionSlice';
import orders from './slices/ordersSlice';
import ui from './slices/uiSlice';

export const store = configureStore({
  reducer: { catalog, cart, checkout, session, orders, ui },
});
