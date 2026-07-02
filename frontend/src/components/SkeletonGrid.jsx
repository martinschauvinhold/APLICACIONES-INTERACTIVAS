/**
 * SkeletonGrid — placeholder animado mientras "carga" el catálogo.
 * Props: count (cantidad de tarjetas fantasma).
 */
function SkeletonGrid({ count = 8 }) {
  return (
    <div className="grid">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="card skel-card" style={{ animationDelay: `${i * 60}ms` }}>
          <div className="skel-img"></div>
          <div className="card-body">
            <div className="skel-line" style={{ width: '30%' }}></div>
            <div className="skel-line" style={{ width: '85%', height: 16 }}></div>
            <div className="skel-line" style={{ width: '50%' }}></div>
            <div className="card-foot">
              <div className="skel-line" style={{ width: 70, height: 18 }}></div>
              <div className="skel-line" style={{ width: 80, height: 28, borderRadius: 4 }}></div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default SkeletonGrid;
