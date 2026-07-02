/* Helpers de formato y normalización */

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const resolveImageUrl = (url) => {
  if (!url) return null;
  if (/^https?:\/\//.test(url)) return url;
  return `${API_URL}${url.startsWith('/') ? '' : '/'}${url}`;
};

export const fmtARS = (n) => {
  if (n == null || Number.isNaN(Number(n))) return '—';
  return '$ ' + Number(n).toLocaleString('es-AR', { maximumFractionDigits: 0 });
};

export const slugAttrs = (attrs) =>
  Object.entries(attrs || {})
    .map(([k, v]) => `${k}: ${v}`)
    .join(' · ');
