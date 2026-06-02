// Barra superior negra "LIVE" con mensajes en vivo.
// Recibe los mensajes por props como [{ Icon, text }] (comunicación entre componentes).
export default function LiveTicker({ messages = [] }) {
  return (
    <div className="ticker">
      <span className="live">LIVE</span>
      {messages.map(({ Icon, text }, i) => (
        <span key={i} className="ticker-msg">
          {i > 0 && <span className="sep">·</span>}
          {Icon && <Icon size={14} />}
          {text}
        </span>
      ))}
    </div>
  )
}
