import { es } from './es';

const DICT = es;

function resolve(obj, path) {
  return path.split('.').reduce((acc, key) => (acc == null ? acc : acc[key]), obj);
}

function interpolate(str, vars) {
  return str.replace(/\{(\w+)\}/g, (match, key) =>
    key in vars ? String(vars[key]) : match,
  );
}

export function t(key, vars) {
  const value = resolve(DICT, key);
  if (value == null) return key;
  if (typeof value === 'string') return vars ? interpolate(value, vars) : value;
  return value;
}

export function orderStatus(code) {
  const label = resolve(DICT, `orderStatus.${code}`);
  return typeof label === 'string' ? label : code;
}

export function ticketStatus(code) {
  const label = resolve(DICT, `ticketStatus.${code}`);
  return typeof label === 'string' ? label : code;
}

export { es };
