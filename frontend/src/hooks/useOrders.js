import { useDispatch, useSelector } from 'react-redux';
import {
  selectOrders, selectOrdersLoading, selectOrdersPlacing, selectOrdersPlaceError, selectCurrentTracking,
  placeOrder, fetchOrdersByUser, cancelOrder, fetchOrderDetail,
} from '../features/orders/ordersSlice';
import { clearCart } from '../features/cart/cartSlice';
import { resetCheckout } from '../features/checkout/checkoutSlice';
import { selectUser } from '../features/auth/authSlice';
import { selectCartItems, selectCouponCode, selectCoupon } from '../features/cart/cartSlice';
import { selectCheckoutAddressId } from '../features/checkout/checkoutSlice';

export function useOrders() {
  const dispatch = useDispatch();
  const list = useSelector(selectOrders);
  const loading = useSelector(selectOrdersLoading);
  const placing = useSelector(selectOrdersPlacing);
  const placeError = useSelector(selectOrdersPlaceError);
  const currentTracking = useSelector(selectCurrentTracking);
  const user = useSelector(selectUser);
  const cartItems = useSelector(selectCartItems);
  const couponCode = useSelector(selectCouponCode);
  const coupon = useSelector(selectCoupon);
  const addressId = useSelector(selectCheckoutAddressId);

  const handlePlaceOrder = async ({ addressId: addrId, paymentMethod }) => {
    const result = await dispatch(placeOrder({
      userId: user.user_id,
      shippingAddressId: addrId,
      items: cartItems,
      couponCode: couponCode && coupon ? couponCode : null,
      paymentMethod,
    }));
    if (placeOrder.rejected.match(result)) {
      throw new Error(result.payload || 'No se pudo procesar el pedido');
    }
    return result.payload;
  };

  return {
    list,
    loading,
    placing,
    placeError,
    currentTracking,
    placeOrder: handlePlaceOrder,
    fetchByUser: (userId) => dispatch(fetchOrdersByUser(userId)),
    cancelOrder: (orderId) => dispatch(cancelOrder(orderId)),
    fetchDetail: (orderId) => dispatch(fetchOrderDetail(orderId)),
  };
}
