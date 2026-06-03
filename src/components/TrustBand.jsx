/**
 * TrustBand — banda con 4 promesas debajo del catálogo.
 */
function TrustBand() {
  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: 0,
        margin: 'var(--pad-lg) calc(var(--pad-lg) * -1) 0',
        padding: 'var(--pad-lg)',
        borderTop: '1px solid var(--border)',
        borderBottom: '1px solid var(--border)',
        background: 'var(--bg-2)',
      }}
    >
      {[
        { num: '24h', label: 'Despacho garantizado',  sub: 'Para CABA y GBA' },
        { num: '30d', label: 'Devolución sin cargo',  sub: 'Returns + Refunds' },
        { num: '12m', label: 'Garantía oficial',      sub: 'Vía vendedor verificado' },
        { num: 'B2B', label: 'Precios mayoristas',    sub: 'Descuento por volumen' },
      ].map((cell, i) => (
        <div
          key={i}
          style={{
            padding: '0 var(--pad)',
            borderRight: i < 3 ? '1px solid var(--border)' : 'none',
          }}
        >
          <div
            style={{
              fontFamily: 'var(--font-display)',
              fontSize: 36,
              lineHeight: 1,
              letterSpacing: '-0.02em',
              marginBottom: 6,
            }}
          >
            <em style={{ fontStyle: 'italic', color: 'var(--accent)' }}>{cell.num}</em>
          </div>
          <div style={{ fontWeight: 600, fontSize: 13 }}>{cell.label}</div>
          <div className="mono" style={{ color: 'var(--fg-muted)', fontSize: 11, marginTop: 4 }}>{cell.sub}</div>
        </div>
      ))}
    </div>
  );
}

export default TrustBand;
