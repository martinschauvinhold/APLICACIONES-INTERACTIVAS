import { createCrudSlice } from '../createCrudSlice';

function normalizeWarehouse(w) {
  return {
    warehouse_id: w.id,
    name: w.name,
    location: w.location,
    contact_phone: w.contactPhone,
  };
}

const { reducer, thunks } = createCrudSlice({
  name: 'warehouses',
  basePath: '/warehouses',
  idKey: 'warehouse_id',
  normalize: normalizeWarehouse,
});

export const {
  fetchAll: fetchWarehouses,
  fetchById: fetchWarehouse,
  createItem: createWarehouse,
  updateItem: updateWarehouse,
  removeItem: removeWarehouse,
} = thunks;

export const selectWarehouses = (s) => s.warehouses.items;
export const selectWarehousesLoading = (s) => s.warehouses.loading;

export default reducer;
