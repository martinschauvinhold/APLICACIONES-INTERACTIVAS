/**
 * Stepper — indicador horizontal de pasos para el checkout.
 * Props: steps (array de strings), current (índice del paso activo)
 */
function Stepper({ steps, current }) {
  return (
    <ol className="stepper">
      {steps.map((label, i) => (
        <li
          key={label}
          className={`stepper-item ${i === current ? 'is-current' : ''} ${i < current ? 'is-done' : ''}`}
        >
          <span className="stepper-num">{String(i + 1).padStart(2, '0')}</span>
          <span>{label}</span>
        </li>
      ))}
    </ol>
  );
}

export default Stepper;
