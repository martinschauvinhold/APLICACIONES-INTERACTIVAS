import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import ProductImage from './ProductImage';
import Stars from './Stars';
import Btn from './Btn';
import { fmtARS } from '../utils/format';
import { add } from '../store/slices/cartSlice';
import { toggleCartDrawer, openQuickView } from '../store/slices/uiSlice';

/**
 * ProductCard — tarjeta de producto en la grilla del catálogo.
 * Props: product (objeto del catálogo)
 */
function ProductCard({ product }) {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const v0 = product.variants[0];
  const inStock = product.variants.some((v) => v.stock > 0);
  const lowStock = inStock && product.variants.some((v) => v.stock <= 5 && v.stock > 0);
  const minPrice = Math.min(...product.variants.map((v) => v.base_price));
  const hasMultipleVariants = product.variants.length > 1;

  const handleClick = () => navigate(`/producto/${product.product_id}`);

  const handleAdd = (e) => {
    e.stopPropagation();
    dispatch(add({ variantId: v0.variant_id, quantity: 1 }));
    dispatch(toggleCartDrawer());
  };

  const handleQuickView = (e) => {
    e.stopPropagation();
    dispatch(openQuickView({ productId: product.product_id }));
  };

  return (
    <article className="card" onClick={handleClick}>
      <div className="card-img">
        <ProductImage src={product.image_url} alt={product.name} ratio="4/3" />
        {!inStock && <div className="card-badge">sin stock</div>}
        {lowStock && <div className="card-badge card-badge-warn">stock bajo</div>}
        <button className="card-quickview" onClick={handleQuickView}>QUICK VIEW</button>
      </div>
      <div className="card-body">
        <div className="card-brand">{product.brand}</div>
        <h3 className="card-name">{product.name}</h3>
        <div className="card-meta">
          <Stars rating={product.rating} count={product.reviewCount} />
        </div>
        <div className="card-foot">
          <div>
            {hasMultipleVariants && <span className="card-price-from">desde</span>}
            <span className="card-price-val">{fmtARS(minPrice)}</span>
          </div>
          <Btn size="sm" variant="primary" disabled={!inStock} onClick={handleAdd}>
            Agregar
          </Btn>
        </div>
      </div>
    </article>
  );
}

export default ProductCard;
