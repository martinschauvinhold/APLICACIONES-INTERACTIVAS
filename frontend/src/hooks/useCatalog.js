import { useDispatch, useSelector } from 'react-redux';
import { selectProducts, selectProductsLoading, selectProductsMutating } from '../features/products/productsSlice';
import { uploadProductImage, deleteProduct } from '../features/products/productsSlice';
import { selectCategories } from '../features/categories/categoriesSlice';
import { selectFilters } from '../features/filters/filtersSlice';
import { setFilter, clearFilters } from '../features/filters/filtersSlice';
import { selectFilteredProducts } from '../store/selectors';
import { selectUsers } from '../features/users/usersSlice';

export function useCatalog() {
  const dispatch = useDispatch();
  const products = useSelector(selectProducts);
  const categories = useSelector(selectCategories);
  const filters = useSelector(selectFilters);
  const catalogLoading = useSelector(selectProductsLoading);
  const mutating = useSelector(selectProductsMutating);
  const filtered = useSelector(selectFilteredProducts);
  const users = useSelector(selectUsers);

  return {
    products,
    categories,
    filters,
    catalogLoading,
    mutating,
    filtered,
    productById: (id) => products.find((p) => p.product_id === Number(id)),
    sellerById: (id) => users.find((u) => u.user_id === id) || null,
    productsBySeller: (id) => products.filter((p) => p.seller_id === id),
    setFilter: (partial) => dispatch(setFilter(partial)),
    clearFilters: () => dispatch(clearFilters()),
    deleteProduct: (id) => dispatch(deleteProduct(id)),
    uploadProductImage: (productId, file) => dispatch(uploadProductImage({ productId, file })),
  };
}
