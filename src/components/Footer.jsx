/**
 * Footer — pie de página editorial.
 */
function Footer() {
  return (
    <footer className="foot">
      <div className="foot-inner">
        <div className="foot-top">
          <div>
            <div className="foot-display">
              Tecnología <em>seria</em>,<br/>al alcance de un <em>click</em>.
            </div>
            <p className="foot-sub">
              Enterate primero de drops, restocks y precios mayoristas. Sin spam, prometido.
            </p>
          </div>
          <div>
            <div className="foot-col-h">Comprar</div>
            <a className="foot-col-link" href="#">Laptops</a>
            <a className="foot-col-link" href="#">Audio</a>
            <a className="foot-col-link" href="#">Monitores</a>
            <a className="foot-col-link" href="#">Teclados</a>
            <a className="foot-col-link" href="#">Smartphones</a>
          </div>
          <div>
            <div className="foot-col-h">Cuenta</div>
            <a className="foot-col-link" href="#">Mis pedidos</a>
            <a className="foot-col-link" href="#">Direcciones</a>
            <a className="foot-col-link" href="#">Devoluciones</a>
            <a className="foot-col-link" href="#">Soporte</a>
          </div>
          <div>
            <div className="foot-col-h">Vector</div>
            <a className="foot-col-link" href="#">Sobre nosotros</a>
            <a className="foot-col-link" href="#">Vender en Vector</a>
            <a className="foot-col-link" href="#">Términos</a>
            <a className="foot-col-link" href="#">Privacidad</a>
          </div>
        </div>
        <div className="foot-bottom">
          <div className="foot-mark">
            <span className="foot-mark-glyph"></span>
            <span>VECTOR.TECH · 2026 · TODOS LOS DERECHOS RESERVADOS</span>
          </div>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
