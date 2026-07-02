import { createCrudSlice } from '../createCrudSlice';

function normalizeDelivery(d) {
  return {
    delivery_id: d.id,
    order_id: d.orderId ?? null,
    shipping_method: d.shippingMethod,
    tracking_number: d.trackingNumber,
    status: String(d.status || '').toLowerCase(),
    dispatched_at: d.dispatchedAt,
  };
}

const { reducer, thunks } = createCrudSlice({
  name: 'deliveries',
  basePath: '/deliveries',
  idKey: 'delivery_id',
  normalize: normalizeDelivery,
});

export const {
  fetchAll: fetchDeliveries,
  fetchById: fetchDelivery,
  createItem: createDelivery,
  updateItem: updateDelivery,
  removeItem: removeDelivery,
} = thunks;

export const selectDeliveries = (s) => s.deliveries.items;
export const selectDeliveriesLoading = (s) => s.deliveries.loading;

export default reducer;
