/**
 * Cliente HTTP para conectar con el backend Spring Boot.
 * Mapea los endpoints del repo APLICACIONES-INTERACTIVAS + JWT.
 */

const LS_TOKEN   = 'vector_jwt';
const LS_URL     = 'vector_api_url';
const LS_ENABLED = 'vector_api_enabled';

const config = {
  getBaseUrl()  { return localStorage.getItem(LS_URL) || 'http://localhost:8080'; },
  setBaseUrl(u) { localStorage.setItem(LS_URL, u); },
  isEnabled()   { return localStorage.getItem(LS_ENABLED) !== 'false'; },
  setEnabled(v) { localStorage.setItem(LS_ENABLED, v ? 'true' : 'false'); },
  getToken()    { return localStorage.getItem(LS_TOKEN); },
  setToken(t)   { if (t) localStorage.setItem(LS_TOKEN, t); else localStorage.removeItem(LS_TOKEN); },
};

const statusListeners = new Set();
let currentStatus = config.isEnabled() ? 'idle' : 'disabled';
let currentMessage = '';

function setStatus(s, msg = '') {
  currentStatus = s;
  currentMessage = msg;
  statusListeners.forEach((fn) => fn(s, msg));
}

export function subscribeStatus(fn) {
  statusListeners.add(fn);
  fn(currentStatus, currentMessage);
  return () => statusListeners.delete(fn);
}
export const getStatus = () => ({ status: currentStatus, message: currentMessage });

/**
 * Decodifica el payload de un JWT (sin verificar firma — solo para leer claims
 * en el cliente). El backend del repo pone el email en el subject; si en el
 * futuro agrega un claim `role`, esta función ya lo expone.
 */
export function decodeJwt(token) {
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json);
  } catch {
    return null;
  }
}

/**
 * Intenta resolver el rol del usuario logueado:
 *  1. claim `role` dentro del JWT (si el backend lo agrega)
 *  2. endpoint /users/me (si existe)
 *  3. null → el frontend cae al modo demo / rol elegido manualmente
 */
export async function resolveRole(token) {
  const claims = decodeJwt(token);
  if (claims && claims.role) return claims.role;
  try {
    const me = await request('GET', '/users/me');
    return me?.role || null;
  } catch {
    return null;
  }
}

async function request(method, path, body) {
  if (!config.isEnabled()) throw new Error('API_DISABLED');
  const url = config.getBaseUrl().replace(/\/$/, '') + path;
  const headers = { 'Content-Type': 'application/json' };
  const token = config.getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  try {
    const res = await fetch(url, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      const err = new Error(`HTTP ${res.status}: ${text || res.statusText}`);
      err.status = res.status;
      setStatus('error', `${res.status} en ${path}`);
      throw err;
    }
    setStatus('ok');
    if (res.status === 204) return null;
    const ct = res.headers.get('content-type') || '';
    return ct.includes('application/json') ? res.json() : res.text();
  } catch (e) {
    if (e.message === 'API_DISABLED') throw e;
    if (!e.status) setStatus('error', 'No se puede conectar al backend');
    throw e;
  }
}

export const api = {
  config,

  auth: {
    // El backend devuelve { token }. El subject del JWT es el email.
    login:    (email, password) => request('POST', '/auth/login', { email, password }),
    // El backend (RegisterRequest) espera 'password' y SIEMPRE crea role=buyer.
    register: (data)            => request('POST', '/auth/register', data),
    logout:   ()                => { config.setToken(null); return Promise.resolve(); },
  },

  users: {
    list:   ()     => request('GET',  '/users'),
    get:    (id)   => request('GET',  `/users/${id}`),
    create: (data) => request('POST', '/users', data),
  },

  categories: {
    list:   ()     => request('GET',  '/categories'),
    create: (data) => request('POST', '/categories', data),
  },

  addresses: {
    forUser: (userId) => request('GET',  `/addresses/user/${userId}`),
    create:  (data)   => request('POST', '/addresses', data),
  },

  products: {
    list:   ()     => request('GET',  '/products'),
    get:    (id)   => request('GET',  `/products/${id}`),
    create: (data) => request('POST', '/products', data),
  },

  variants: {
    forProduct: (productId) => request('GET',  `/variants/product/${productId}`),
    create:     (data)      => request('POST', '/variants', data),
  },

  orders: {
    list:    ()       => request('GET',  '/orders'),
    forUser: (userId) => request('GET',  `/orders/user/${userId}`),
    get:     (id)     => request('GET',  `/orders/${id}`),
    create:  (data)   => request('POST', '/orders', data),
    update:  (id, d)  => request('PUT',  `/orders/${id}`, d),
  },

  orderItems: {
    forOrder: (orderId) => request('GET',  `/order-items/order/${orderId}`),
    add:      (data)    => request('POST', '/order-items', data),
  },

  payments: {
    forOrder: (orderId) => request('GET',  `/payments/order/${orderId}`),
    create:   (data)    => request('POST', '/payments', data),
  },

  deliveries: {
    forOrder: (orderId) => request('GET',  `/deliveries/order/${orderId}`),
    create:   (data)    => request('POST', '/deliveries', data),
    update:   (id, d)   => request('PUT',  `/deliveries/${id}`, d),
  },

  /**
   * Flujo completo de checkout siguiendo el Insomnia:
   *  1. POST /orders (pending)
   *  2. POST /order-items por cada línea
   *  3. POST /payments (approved)
   *  4. PUT /orders/{id} (paid)
   *  5. POST /deliveries (dispatched)
   */
  async checkout({ userId, shippingAddressId, lines, paymentMethod = 'credit_card' }) {
    const orderRes = await this.orders.create({ userId, shippingAddressId, status: 'pending' });
    const orderId = orderRes.orderId || orderRes.id || orderRes.order_id;
    if (!orderId) throw new Error('Backend no devolvió orderId');

    for (const line of lines) {
      await this.orderItems.add({ orderId, variantId: line.variantId, quantity: line.quantity });
    }

    const transactionId = 'TXN-' + Date.now();
    await this.payments.create({ orderId, paymentMethod, transactionId, paymentStatus: 'approved' });
    await this.orders.update(orderId, { userId, shippingAddressId, status: 'paid' });

    const trackingNumber = 'CA-' + Date.now().toString().slice(-8);
    await this.deliveries.create({
      orderId, shippingMethod: 'correo_argentino', trackingNumber, deliveryStatus: 'dispatched',
    });

    return { orderId, trackingNumber, transactionId };
  },
};
