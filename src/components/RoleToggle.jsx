// Toggle Comprador / Vendedor para el registro.
// El valor seleccionado y el setter llegan por props desde el formulario.
export default function RoleToggle({ value, onChange }) {
  return (
    <div className="role-toggle">
      <button
        type="button"
        className={value === 'COMPRADOR' ? 'active' : ''}
        onClick={() => onChange('COMPRADOR')}
      >
        COMPRADOR
      </button>
      <button
        type="button"
        className={value === 'VENDEDOR' ? 'active' : ''}
        onClick={() => onChange('VENDEDOR')}
      >
        VENDEDOR
      </button>
    </div>
  )
}
