import { http } from '../../api/axios';
import { resolveImageUrl } from '../../utils/format';

function parseAttrs(attributes) {
  if (!attributes) return {};
  try {
    return JSON.parse(attributes);
  } catch {
    return {};
  }
}

// El back devuelve stock y tiers ya embebidos en cada variante (calculados en
// lote del lado del servidor), así que no hace falta pedirlos aparte por variante.
function hydrateVariant(v) {
  const tiers = v.tiers || [];
  return {
    variant_id: v.id,
    sku: v.sku,
    attrs: parseAttrs(v.attributes),
    base_price: Number(v.basePrice),
    stock: v.stock ?? 0,
    tiers: tiers.length > 0
      ? tiers.map((t) => ({ tier_id: t.id, min_quantity: t.minQuantity, unit_price: Number(t.unitPrice) }))
      : [{ tier_id: null, min_quantity: 1, unit_price: Number(v.basePrice) }],
  };
}

export async function getProductDetail(id) {
  const [{ data: p }, { data: variants }, { data: reviews }] = await Promise.all([
    http.get(`/products/${id}`),
    http.get(`/variants/product/${id}`),
    http.get(`/reviews/product/${id}`),
  ]);

  const hydratedVariants = variants.map(hydrateVariant);

  const reviewCount = reviews.length;
  const rating = reviewCount > 0 ? reviews.reduce((acc, r) => acc + r.rating, 0) / reviewCount : 0;

  return {
    product_id: p.id,
    seller_id: p.sellerId ?? null,
    category_id: p.categoryId,
    brand: p.brand,
    name: p.name,
    description: p.description,
    tags: [],
    rating,
    reviewCount,
    image_url: resolveImageUrl(p.imageUrl),
    variants: hydratedVariants,
  };
}

export async function createProduct({ name, description, brand, categoryId }) {
  const { data } = await http.post('/products', { name, description, brand, categoryId });
  return data;
}

export async function deleteProduct(id) {
  await http.delete(`/products/${id}`);
  return id;
}

// Sube un archivo de imagen al producto (multipart). NO seteamos Content-Type a
// mano: axios pone el boundary del multipart/form-data solo.
export async function uploadProductImage(productId, file) {
  const form = new FormData();
  form.append('file', file);
  const { data } = await http.post(`/products/${productId}/images`, form);
  return data;
}
