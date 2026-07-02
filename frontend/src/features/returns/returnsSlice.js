import { createCrudSlice } from '../createCrudSlice';

function normalizeReturn(r) {
  return {
    return_id: r.id,
    order_id: r.orderId ?? null,
    reason: r.reason,
    status: String(r.status || '').toLowerCase(),
    requested_at: r.requestedAt,
  };
}

const { reducer, thunks } = createCrudSlice({
  name: 'returns',
  basePath: '/returns',
  idKey: 'return_id',
  normalize: normalizeReturn,
});

export const {
  fetchAll: fetchReturns,
  fetchById: fetchReturn,
  createItem: createReturn,
  updateItem: updateReturn,
  removeItem: removeReturn,
} = thunks;

export const selectReturns = (s) => s.returns.items;
export const selectReturnsLoading = (s) => s.returns.loading;

export default reducer;
