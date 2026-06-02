// Renderiza estrellas a partir de un puntaje (0 a 5) recibido por props.
export default function Stars({ rating = 0 }) {
  const full = Math.round(rating)
  const stars = '★★★★★☆☆☆☆☆'.slice(5 - full, 10 - full)
  return <span className="stars">{stars}</span>
}
