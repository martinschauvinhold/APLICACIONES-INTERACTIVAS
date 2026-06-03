/* Helpers de formato y normalización */

export const fmtARS = (n) => {
  if (n == null || isNaN(n)) return '—';
  return '$ ' + n.toLocaleString('es-AR', { maximumFractionDigits: 0 });
};

export const slugAttrs = (attrs) =>
  Object.entries(attrs || {})
    .map(([k, v]) => `${k}: ${v}`)
    .join(' · ');
