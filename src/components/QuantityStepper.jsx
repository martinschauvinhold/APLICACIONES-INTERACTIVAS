// Selector de cantidad reutilizable (− valor +).
// Recibe el valor y los callbacks por props; el estado vive en el padre.
export default function QuantityStepper({ value, onChange, min = 1 }) {
  return (
    <div className="stepper">
      <button onClick={() => onChange(Math.max(min, value - 1))}>−</button>
      <span className="val">{value}</span>
      <button onClick={() => onChange(value + 1)}>+</button>
    </div>
  )
}
