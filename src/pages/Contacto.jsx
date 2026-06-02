import { useState } from 'react'

// Vista de contacto: formulario con useState que muestra un mensaje de éxito al enviar.
export default function Contacto() {
  const [form, setForm] = useState({ nombre: '', email: '', mensaje: '' })
  const [sent, setSent] = useState(false)

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    setSent(true)
  }

  return (
    <div className="contact-wrap">
      <h1 className="page-title">Contacto</h1>

      {sent && (
        <div className="alert-success">
          ✓ ¡Gracias {form.nombre || ''}! Tu mensaje fue enviado. Te responderemos a la
          brevedad.
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label>NOMBRE</label>
          <input
            placeholder="Tu nombre"
            value={form.nombre}
            onChange={(e) => update('nombre', e.target.value)}
            required
          />
        </div>
        <div className="field">
          <label>EMAIL</label>
          <input
            type="email"
            placeholder="tu@email.com"
            value={form.email}
            onChange={(e) => update('email', e.target.value)}
            required
          />
        </div>
        <div className="field">
          <label>MENSAJE</label>
          <textarea
            placeholder="¿En qué podemos ayudarte?"
            value={form.mensaje}
            onChange={(e) => update('mensaje', e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn btn-dark">
          Enviar mensaje →
        </button>
      </form>
    </div>
  )
}
