/**
 * ProductImage — imagen del producto con fallback.
 * Props: src, alt, ratio (relación de aspecto, ej. '4/3' o '1/1').
 * Los estilos viven en index.css (.ph-frame / .ph-img / .ph-empty).
 */
function ProductImage({ src, alt, ratio = '4/3' }) {
  return (
    <div className="ph-frame" style={{ aspectRatio: ratio }}>
      {src ? (
        <img className="ph-img" src={src} alt={alt} loading="lazy" />
      ) : (
        <div className="ph-empty" />
      )}
    </div>
  );
}

export default ProductImage;
