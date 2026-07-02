import { createCrudSlice } from '../createCrudSlice';

function normalizeDiscount(d) {
  return {
    discount_id: d.id,
    name: d.name,
    discount_type: d.discountType,
    value: d.value != null ? Number(d.value) : null,
    applies_to: d.appliesTo,
    product_id: d.productId ?? null,
    product_name: d.productName ?? null,
    category_id: d.categoryId ?? null,
    category_name: d.categoryName ?? null,
    min_price: d.minPrice != null ? Number(d.minPrice) : null,
    starts_at: d.startsAt,
    expires_at: d.expiresAt,
    is_active: d.isActive,
  };
}

const { reducer, thunks } = createCrudSlice({
  name: 'discounts',
  basePath: '/discounts',
  idKey: 'discount_id',
  normalize: normalizeDiscount,
});

export const {
  fetchAll: fetchDiscounts,
  fetchById: fetchDiscount,
  createItem: createDiscount,
  updateItem: updateDiscount,
  removeItem: removeDiscount,
} = thunks;

export const selectDiscounts = (s) => s.discounts.items;
export const selectDiscountsLoading = (s) => s.discounts.loading;

export default reducer;
