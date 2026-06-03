/**
 * Field — campo de formulario con label + hint/error.
 * Props: label, hint, error, children
 */
function Field({ label, hint, error, children }) {
  return (
    <label className="field">
      <span className="field-label">{label}</span>
      {children}
      {hint && !error && <span className="field-hint">{hint}</span>}
      {error && <span className="field-error">⚠ {error}</span>}
    </label>
  );
}

export default Field;
