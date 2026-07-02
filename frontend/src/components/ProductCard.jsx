import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { useUI } from '../hooks/useUI';
import { useCatalog } from '../hooks/useCatalog';
import ProductImage from './ProductImage';
import Stars from './Stars';
import Btn from './Btn';
import { fmtARS } from '../utils/format';
import { t } from '../i18n';

function ProductCard({ product }) {
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { toggleCartDrawer, openQuickView } = useUI();
  const { sellerById } = useCatalog();
  const { role, isAuthenticated } = useAuth();
  const canBuy = isAuthenticated && role === 'buyer';
  const seller = sellerById(product.seller_id);
  const v0 = product.variants[0];
  const inStock = product.variants.some((v) => v.stock > 0);
  const lowStock = inStock && product.variants.some((v) => v.stock <= 5 && v.stock > 0);
  const minPrice = Math.min(...product.variants.map((v) => v.base_price));
  const hasMultipleVariants = product.variants.length > 1;

  const handleClick = () => navigate(`/producto/${product.product_id}`);

  const handleAdd = (e) => {
    e.stopPropagation();
    if (!isAuthenticated) { navigate('/login'); return; }
    addToCart({ variantId: v0.variant_id, quantity: 1 });
    toggleCartDrawer();
  };

  const handleQuickView = (e) => {
    e.stopPropagation();
    openQuickView({ productId: product.product_id });
  };

  return (
    <article className="card" onClick={handleClick}>
      <div className="card-img">
        <ProductImage src={product.image_url} alt={product.name} ratio="4/3" />
        {!inStock && <div className="card-badge">{t('product.outOfStock')}</div>}
        {lowStock && <div className="card-badge card-badge-warn">{t('product.lowStock')}</div>}
        <button className="card-quickview" onClick={handleQuickView}>{t('product.quickView')}</button>
      </div>
      <div className="card-body">
        <div className="card-brand">{product.brand}</div>
        <h3 className="card-name">{product.name}</h3>
        <div className="card-meta">
          <Stars rating={product.rating} count={product.reviewCount} />
        </div>
        {seller && <div className="card-seller mono">{t('product.soldBy', { seller: seller.first_name })}</div>}
        <div className="card-foot">
          <div>
            {hasMultipleVariants && <span className="card-price-from">{t('product.from')}</span>}
            <span className="card-price-val">{fmtARS(minPrice)}</span>
          </div>
          {(canBuy || !isAuthenticated) && (
            <Btn size="sm" variant="primary" disabled={canBuy && !inStock} onClick={handleAdd}>
              {t('product.add')}
            </Btn>
          )}
        </div>
      </div>
    </article>
  );
}

export default ProductCard;
