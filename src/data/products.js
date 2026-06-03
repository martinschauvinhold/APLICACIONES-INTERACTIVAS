// Catálogo semilla — usado en modo demo y mientras el backend carga.

// Helper para generar precios escalonados (descuento mayorista)
const tiers = (base) => [
  { min_quantity: 1,  unit_price: base },
  { min_quantity: 5,  unit_price: Math.round(base * 0.9) },
  { min_quantity: 10, unit_price: Math.round(base * 0.82) },
];

const IMG = {
  1: 'https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=900&q=80',
  2: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=900&q=80',
  3: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=900&q=80',
  4: 'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=900&q=80',
  5: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=900&q=80',
  6: 'https://images.unsplash.com/photo-1547119957-637f8679db1e?w=900&q=80',
  7: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=900&q=80',
  8: 'https://images.unsplash.com/photo-1561112078-7d24e04c3407?w=900&q=80',
  9: 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=900&q=80',
  10: 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=900&q=80',
  11: 'https://images.unsplash.com/photo-1591488320449-011701bb6704?w=900&q=80',
  12: 'https://images.unsplash.com/photo-1597858520171-563a8e8b9925?w=900&q=80',
};

export const PRODUCTS = [
  {
    product_id: 1, category_id: 1, brand: 'Lenovo',
    name: 'ThinkPad X1 Carbon Gen 11',
    description: 'Ultrabook empresarial 14” con Intel Core i7-1365U, panel IPS 2.8K y chasis en fibra de carbono. Pensada para flotas corporativas.',
    tags: ['empresarial', 'ultraliviana', 'intel'],
    rating: 4.7, reviewCount: 128, image_url: IMG[1],
    variants: [
      { variant_id: 101, sku: 'TPX1-16-512', attrs: { ram: '16GB', almacenamiento: '512GB' }, base_price: 2_390_000, stock: 14, tiers: tiers(2_390_000) },
      { variant_id: 102, sku: 'TPX1-32-1TB', attrs: { ram: '32GB', almacenamiento: '1TB' }, base_price: 2_980_000, stock: 6, tiers: tiers(2_980_000) },
    ],
  },
  {
    product_id: 2, category_id: 1, brand: 'Apple',
    name: 'MacBook Air M3 13”',
    description: 'Chip Apple M3 de 8 núcleos, pantalla Liquid Retina y hasta 18 horas de batería. El silencio absoluto: sin ventilador.',
    tags: ['mac', 'silencioso', 'creativo'],
    rating: 4.9, reviewCount: 342, image_url: IMG[2],
    variants: [
      { variant_id: 201, sku: 'MBA-M3-8-256', attrs: { ram: '8GB', almacenamiento: '256GB', color: 'Medianoche' }, base_price: 1_899_000, stock: 22, tiers: tiers(1_899_000) },
      { variant_id: 202, sku: 'MBA-M3-16-512', attrs: { ram: '16GB', almacenamiento: '512GB', color: 'Estelar' }, base_price: 2_450_000, stock: 9, tiers: tiers(2_450_000) },
    ],
  },
  {
    product_id: 3, category_id: 2, brand: 'Sony',
    name: 'WH-1000XM5',
    description: 'Auriculares inalámbricos con cancelación activa de ruido de referencia. 30 horas de batería, codec LDAC.',
    tags: ['bluetooth', 'anc', 'estudio'],
    rating: 4.8, reviewCount: 891, image_url: IMG[3],
    variants: [
      { variant_id: 301, sku: 'WHXM5-BLK', attrs: { color: 'Negro' }, base_price: 519_000, stock: 41, tiers: tiers(519_000) },
      { variant_id: 302, sku: 'WHXM5-SLV', attrs: { color: 'Plateado' }, base_price: 519_000, stock: 18, tiers: tiers(519_000) },
    ],
  },
  {
    product_id: 4, category_id: 2, brand: 'Audio-Technica',
    name: 'ATH-M50x Pro',
    description: 'Monitores cerrados de referencia para estudio. Drivers de 45mm y respuesta plana — el caballo de batalla del rubro.',
    tags: ['estudio', 'monitor', 'cable'],
    rating: 4.6, reviewCount: 412, image_url: IMG[4],
    variants: [
      { variant_id: 401, sku: 'M50X-STD', attrs: { color: 'Negro' }, base_price: 289_000, stock: 26, tiers: tiers(289_000) },
    ],
  },
  {
    product_id: 5, category_id: 3, brand: 'Dell',
    name: 'UltraSharp U2723QE 27” 4K',
    description: 'Monitor IPS Black 4K con cobertura sRGB 100% y hub Thunderbolt 4. Para edición de color y desarrollo.',
    tags: ['4k', 'hub', 'ips-black'],
    rating: 4.7, reviewCount: 219, image_url: IMG[5],
    variants: [
      { variant_id: 501, sku: 'U2723QE-STD', attrs: { tamaño: '27”' }, base_price: 1_120_000, stock: 11, tiers: tiers(1_120_000) },
    ],
  },
  {
    product_id: 6, category_id: 3, brand: 'LG',
    name: 'LG 34WN780-B UltraWide',
    description: 'Ultrawide 34” QHD con ergonomic stand y soporte HDR10. Cobertura sRGB 99%.',
    tags: ['ultrawide', 'qhd', 'hdr'],
    rating: 4.5, reviewCount: 134, image_url: IMG[6],
    variants: [
      { variant_id: 601, sku: '34WN780B', attrs: { tamaño: '34”' }, base_price: 890_000, stock: 5, tiers: tiers(890_000) },
    ],
  },
  {
    product_id: 7, category_id: 4, brand: 'Keychron',
    name: 'Keychron Q1 Pro',
    description: 'Teclado mecánico inalámbrico 75%, switches hot-swap, chasis de aluminio CNC. Programable con QMK/VIA.',
    tags: ['mecánico', 'qmk', '75%'],
    rating: 4.8, reviewCount: 567, image_url: IMG[7],
    variants: [
      { variant_id: 701, sku: 'Q1PRO-BRN', attrs: { switch: 'Brown', layout: 'ANSI' }, base_price: 389_000, stock: 19, tiers: tiers(389_000) },
      { variant_id: 702, sku: 'Q1PRO-RED', attrs: { switch: 'Red', layout: 'ANSI' }, base_price: 389_000, stock: 12, tiers: tiers(389_000) },
      { variant_id: 703, sku: 'Q1PRO-BLUE', attrs: { switch: 'Blue', layout: 'ISO' }, base_price: 389_000, stock: 0, tiers: tiers(389_000) },
    ],
  },
  {
    product_id: 8, category_id: 4, brand: 'Logitech',
    name: 'MX Keys S',
    description: 'Teclado de productividad con teclas perfiladas, retroiluminación inteligente y multi-device. Para escritorio.',
    tags: ['productividad', 'wireless', 'low-profile'],
    rating: 4.6, reviewCount: 289, image_url: IMG[8],
    variants: [
      { variant_id: 801, sku: 'MXKEYS-GRF', attrs: { color: 'Grafito' }, base_price: 189_000, stock: 33, tiers: tiers(189_000) },
    ],
  },
  {
    product_id: 9, category_id: 5, brand: 'Samsung',
    name: 'Galaxy S24 Ultra',
    description: 'Smartphone flagship con S-Pen integrada, cámara de 200MP y procesador Snapdragon 8 Gen 3 for Galaxy.',
    tags: ['flagship', 's-pen', '5g'],
    rating: 4.7, reviewCount: 754, image_url: IMG[9],
    variants: [
      { variant_id: 901, sku: 'S24U-256-TIT', attrs: { almacenamiento: '256GB', color: 'Titanium Black' }, base_price: 1_750_000, stock: 8, tiers: tiers(1_750_000) },
      { variant_id: 902, sku: 'S24U-512-VIO', attrs: { almacenamiento: '512GB', color: 'Titanium Violet' }, base_price: 1_990_000, stock: 4, tiers: tiers(1_990_000) },
    ],
  },
  {
    product_id: 10, category_id: 5, brand: 'Google',
    name: 'Pixel 8 Pro',
    description: 'Tensor G3, cámara computacional referente y 7 años de actualizaciones de OS. Android puro.',
    tags: ['pixel', 'android', 'fotografía'],
    rating: 4.6, reviewCount: 421, image_url: IMG[10],
    variants: [
      { variant_id: 1001, sku: 'P8P-128-OBS', attrs: { almacenamiento: '128GB', color: 'Obsidian' }, base_price: 1_390_000, stock: 15, tiers: tiers(1_390_000) },
    ],
  },
  {
    product_id: 11, category_id: 6, brand: 'NVIDIA',
    name: 'GeForce RTX 4070 Super',
    description: 'GPU con 12GB GDDR6X, DLSS 3.5 y ray tracing de tercera generación. Sweet spot para 1440p.',
    tags: ['gpu', 'ray-tracing', 'dlss'],
    rating: 4.7, reviewCount: 198, image_url: IMG[11],
    variants: [
      { variant_id: 1101, sku: 'RTX4070S-FE', attrs: { fabricante: 'Founders Edition' }, base_price: 780_000, stock: 7, tiers: tiers(780_000) },
    ],
  },
  {
    product_id: 12, category_id: 6, brand: 'Samsung',
    name: 'SSD 990 PRO NVMe 2TB',
    description: 'SSD M.2 PCIe 4.0 con velocidades de hasta 7450 MB/s. Para workstations y gaming de alto rendimiento.',
    tags: ['nvme', 'pcie4', '2tb'],
    rating: 4.9, reviewCount: 612, image_url: IMG[12],
    variants: [
      { variant_id: 1201, sku: '990PRO-2TB', attrs: { capacidad: '2TB' }, base_price: 295_000, stock: 28, tiers: tiers(295_000) },
    ],
  },
];

export const SEED_USER = {
  user_id: 1,
  username: 'martin123',
  email: 'martin@mail.com',
  first_name: 'Martín',
  last_name: 'García',
  role: 'buyer',
  phone: '1155667788',
};

export const SEED_ADDRESSES = [
  { address_id: 1, user_id: 1, street: 'Av. Corrientes 1234', city: 'Buenos Aires', state: 'CABA', zip_code: '1043', reference_note: 'Piso 3 depto B' },
  { address_id: 2, user_id: 1, street: 'Cabildo 2890',        city: 'Buenos Aires', state: 'CABA', zip_code: '1428', reference_note: 'Casa al frente' },
];
