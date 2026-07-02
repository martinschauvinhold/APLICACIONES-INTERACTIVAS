import { useDispatch, useSelector } from 'react-redux';
import {
  toggleCartDrawer, closeCartDrawer, openQuickView, closeQuickView, trackView,
  selectCartDrawerOpen, selectQuickViewId, selectRecentlyViewed,
} from '../features/ui/uiSlice';

export function useUI() {
  const dispatch = useDispatch();
  const cartDrawerOpen = useSelector(selectCartDrawerOpen);
  const quickViewId = useSelector(selectQuickViewId);
  const recentlyViewed = useSelector(selectRecentlyViewed);

  return {
    cartDrawerOpen,
    quickViewId,
    recentlyViewed,
    toggleCartDrawer: () => dispatch(toggleCartDrawer()),
    closeCartDrawer: () => dispatch(closeCartDrawer()),
    openQuickView: ({ productId }) => dispatch(openQuickView(productId)),
    closeQuickView: () => dispatch(closeQuickView()),
    trackView: ({ productId }) => dispatch(trackView(productId)),
  };
}
