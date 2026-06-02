// Formatea un número como precio en pesos: 2299000 -> "$ 2.299.000"
export function formatARS(value) {
  return '$ ' + Math.round(value).toLocaleString('es-AR')
}
