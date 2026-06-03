/**
 * ProductImage — imagen del producto con fallback.
 * Props: src, alt, ratio
 */
function ProductImage({ src, alt, ratio = '4/3' }) {
  return (
    <div className="ph-frame" style={{ aspectRatio: ratio, width: '100%', height: '100%' }}>
      {src ? (
        <img
          src={src}
          alt={alt}
          loading="lazy"
          style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
        />
      ) : (
        <div style={{ background: 'var(--bg-3)', width: '100%', height: '100%' }} />
      )}
    </div>
  );
}

export default ProductImage;
