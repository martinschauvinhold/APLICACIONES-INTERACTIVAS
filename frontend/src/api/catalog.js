import { http } from './axios';
import { resolveImageUrl } from '../utils/format';

// GET /products devuelve un Page ({ content, totalElements, ... }). Pedimos un
// tamaño amplio para traer todo el catálogo de una (la paginación real del
// listado llega con la migración a Redux). Tolera también un array plano.
async function getProducts() {
  const { data: page } = await http.get('/products?size=1000');
  return Array.isArray(page) ? page : (page?.content ?? []);
}

async function getCategories() {
  const { data } = await http.get('/categories');
  return data;
}

// El back devuelve stock y tiers ya embebidos en cada variante (calculados en
// lote del lado del servidor), así que no hace falta pedirlos aparte por
// variante.
async function getVariants() {
  const { data } = await http.get('/variants');
  return data;
}

async function getReviews() {
  const { data } = await http.get('/reviews');
  return data;
}

function parseAttrs(attributes) {
  if (!attributes) return {};
  try {
    return JSON.parse(attributes);
  } catch {
    return {};
  }
}

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

/**
 * getCatalog — trae products/categories/variants/reviews reales y los combina
 * en la misma forma que usaban los datos mockeados (product con variants
 * anidadas, rating/reviewCount calculados de las reviews reales), para no
 * tener que tocar selectors.js ni los componentes de catálogo.
 */
export async function getCatalog() {
  const [products, categories, variants, reviews] = await Promise.all([
    getProducts(), getCategories(), getVariants(), getReviews(),
  ]);

  const variantsByProduct = new Map();
  for (const v of variants) {
    const pid = v.productId ?? v.product?.id;
    const list = variantsByProduct.get(pid) || [];
    list.push(v);
    variantsByProduct.set(pid, list);
  }

  const reviewsByProduct = new Map();
  for (const r of reviews) {
    const list = reviewsByProduct.get(r.productId) || [];
    list.push(r);
    reviewsByProduct.set(r.productId, list);
  }

  const hydratedProducts = products.map((p) => {
    const productVariants = variantsByProduct.get(p.id) || [];
    const productReviews = reviewsByProduct.get(p.id) || [];
    const reviewCount = productReviews.length;
    const rating = reviewCount > 0
      ? productReviews.reduce((acc, r) => acc + r.rating, 0) / reviewCount
      : 0;

    return {
      product_id: p.id,
      seller_id: p.sellerId ?? p.seller?.id ?? null,
      category_id: p.categoryId ?? p.category?.id,
      brand: p.brand,
      name: p.name,
      description: p.description,
      tags: [],
      rating,
      reviewCount,
      image_url: resolveImageUrl(p.imageUrl),
      variants: productVariants.map(hydrateVariant),
    };
  });

  // Un producto sin variantes no es vendible (no tiene precio ni stock):
  // el resto del front asume que siempre hay al menos una. Se filtra acá en
  // vez de tocar cada componente.
  const withoutVariants = hydratedProducts.filter((p) => p.variants.length === 0).length;
  if (withoutVariants > 0) {
    console.warn(`${withoutVariants} producto(s) sin variantes en el back, no se muestran en el catálogo`);
  }

  return {
    products: hydratedProducts.filter((p) => p.variants.length > 0),
    categories: categories.map((c) => ({ id: c.id, name: c.description, slug: c.slug })),
  };
}
