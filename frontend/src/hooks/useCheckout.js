import { useDispatch, useSelector } from 'react-redux';
import {
  selectCheckout,
  setStep, setAddress, setPayment, resetCheckout,
} from '../features/checkout/checkoutSlice';

export function useCheckout() {
  const dispatch = useDispatch();
  const checkout = useSelector(selectCheckout);

  return {
    ...checkout,
    setStep: ({ step }) => dispatch(setStep(step)),
    setAddress: ({ addressId }) => dispatch(setAddress(addressId)),
    setPayment: (partial) => dispatch(setPayment(partial)),
    resetCheckout: () => dispatch(resetCheckout()),
  };
}
