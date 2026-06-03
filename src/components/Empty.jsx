/**
 * Empty — estado vacío reutilizable.
 * Props: title, hint (opcional), action (opcional, ej. botón)
 */
function Empty({ title, hint, action }) {
  return (
    <div className="empty">
      <div className="empty-mark">{'{ }'}</div>
      <div className="empty-title">{title}</div>
      {hint && <div className="empty-hint">{hint}</div>}
      {action}
    </div>
  );
}

export default Empty;
