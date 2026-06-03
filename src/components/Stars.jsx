/**
 * Stars — muestra rating con 5 estrellas + valor numérico.
 * Props: rating (0-5), count (opcional)
 */
function Stars({ rating, count }) {
  const full = Math.round(rating);
  return (
    <span className="stars">
      <span className="stars-glyphs" aria-hidden="true">
        {[0, 1, 2, 3, 4].map((i) => (
          <span key={i} className={i < full ? 'star on' : 'star'}>★</span>
        ))}
      </span>
      <span className="stars-val">{rating.toFixed(1)}</span>
      {count != null && <span className="stars-count">({count})</span>}
    </span>
  );
}

export default Stars;
