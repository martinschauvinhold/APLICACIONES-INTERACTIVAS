/**
 * client — helpers de sesión compartidos por axios (api/axios.js) y por el
 * resto de los módulos de api/.
 */

const TOKEN_KEY = 'vector_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

// Se ejecuta cuando una request con token recibe 401 (sesión inválida/expirada),
// para no repetir esa lógica en cada servicio. main.jsx la registra una sola
// vez para limpiar la sesión de Redux.
let onUnauthorized = null;
export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn;
}

export function notifyUnauthorized() {
  onUnauthorized?.();
}
