import { useDispatch, useSelector } from 'react-redux';
import {
  addToCart, setQuantity, removeFromCart, removeCoupon, clearCart,
  selectCartItems, selectCoupon, selectCouponCode, selectCouponError,
} from '../features/cart/cartSlice';
import { applyCoupon } from '../features/cart/cartSlice';
import { selectCartLines, selectCartTotals } from '../store/selectors';

export function useCart() {
  const dispatch = useDispatch();
  const items = useSelector(selectCartItems);
  const couponCode = useSelector(selectCouponCode);
  const coupon = useSelector(selectCoupon);
  const couponError = useSelector(selectCouponError);
  const lines = useSelector(selectCartLines);
  const totals = useSelector(selectCartTotals);

  return {
    items,
    couponCode,
    coupon,
    couponError,
    lines,
    totals,
    count: totals.itemCount,
    addToCart: (payload) => dispatch(addToCart(payload)),
    setQuantity: (payload) => dispatch(setQuantity(payload)),
    removeFromCart: (payload) => dispatch(removeFromCart(payload)),
    applyCoupon: ({ code }) => dispatch(applyCoupon(code)),
    removeCoupon: () => dispatch(removeCoupon()),
    clearCart: () => dispatch(clearCart()),
  };
}
