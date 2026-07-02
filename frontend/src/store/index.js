import { configureStore } from '@reduxjs/toolkit';
import { setToken } from '../api/client';
import authReducer from '../features/auth/authSlice';
import cartReducer from '../features/cart/cartSlice';
import productsReducer from '../features/products/productsSlice';
import categoriesReducer from '../features/categories/categoriesSlice';
import filtersReducer from '../features/filters/filtersSlice';
import ordersReducer from '../features/orders/ordersSlice';
import usersReducer from '../features/users/usersSlice';
import addressesReducer from '../features/addresses/addressesSlice';
import uiReducer from '../features/ui/uiSlice';
import checkoutReducer from '../features/checkout/checkoutSlice';
import ticketsReducer from '../features/tickets/ticketsSlice';
import variantsReducer from '../features/variants/variantsSlice';
import inventoryReducer from '../features/inventory/inventorySlice';
import warehousesReducer from '../features/warehouses/warehousesSlice';
import discountsReducer from '../features/discounts/discountsSlice';
import deliveriesReducer from '../features/deliveries/deliveriesSlice';
import returnsReducer from '../features/returns/returnsSlice';
import paymentsReducer from '../features/payments/paymentsSlice';
import reviewsReducer from '../features/reviews/reviewsSlice';
import notificationsReducer from '../features/notifications/notificationsSlice';
import refundsReducer from '../features/refunds/refundsSlice';
import shipmentTrackingReducer from '../features/shipmentTracking/shipmentTrackingSlice';
import sellerReducer from '../features/seller/sellerSlice';

const LS_KEY = 'vector_redux';

function loadPersistedState() {
  try {
    const raw = localStorage.getItem(LS_KEY);
    if (!raw) return undefined;
    const { auth, cart, orders } = JSON.parse(raw);
    // Restaurar el token en el cliente HTTP si había sesión
    if (auth?.token) setToken(auth.token);
    return { auth, cart, orders };
  } catch {
    return undefined;
  }
}

const preloadedState = loadPersistedState();

export const store = configureStore({
  reducer: {
    auth: authReducer,
    cart: cartReducer,
    products: productsReducer,
    categories: categoriesReducer,
    filters: filtersReducer,
    orders: ordersReducer,
    users: usersReducer,
    addresses: addressesReducer,
    ui: uiReducer,
    checkout: checkoutReducer,
    tickets: ticketsReducer,
    variants: variantsReducer,
    inventory: inventoryReducer,
    warehouses: warehousesReducer,
    discounts: discountsReducer,
    deliveries: deliveriesReducer,
    returns: returnsReducer,
    payments: paymentsReducer,
    reviews: reviewsReducer,
    notifications: notificationsReducer,
    refunds: refundsReducer,
    shipmentTracking: shipmentTrackingReducer,
    seller: sellerReducer,
  },
  preloadedState,
});

// Persistir auth, cart y orders en cada cambio de estado
store.subscribe(() => {
  const { auth, cart, orders } = store.getState();
  try {
    localStorage.setItem(LS_KEY, JSON.stringify({
      auth: { user: auth.user, isAuthenticated: auth.isAuthenticated },
      cart: { items: cart.items, couponCode: cart.couponCode, coupon: cart.coupon },
      orders: { list: orders.list },
    }));
  } catch {
    // quota exceeded — ignorar
  }
});
