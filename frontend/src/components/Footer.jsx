import { Link, useNavigate } from 'react-router-dom';
import { useCatalog } from '../hooks/useCatalog';
import { t } from '../i18n';

function Footer() {
  const navigate = useNavigate();
  const { categories, setFilter } = useCatalog();

  const goToCategory = (categoryId) => {
    setFilter({ categoryId, search: '' });
    navigate('/catalogo');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <footer className="foot">
      <div className="foot-inner">
        <div className="foot-top">
          <div>
            <div className="foot-display">
              {t('footer.taglinePre')} <em>{t('footer.taglineEm1')}</em>,<br />
              {t('footer.taglineMid')} <em>{t('footer.taglineEm2')}</em>.
            </div>
            <p className="foot-sub"></p>
          </div>

          <div>
            <div className="foot-col-h">{t('footer.colBuy')}</div>
            {categories.map((c) => (
              <button key={c.id} className="foot-col-link" onClick={() => goToCategory(c.id)}>
                {c.name}
              </button>
            ))}
          </div>

          <div>
            <div className="foot-col-h">{t('footer.colAccount')}</div>
            <Link className="foot-col-link" to="/pedidos">{t('footer.orders')}</Link>
            <Link className="foot-col-link" to="/direcciones">{t('footer.addresses')}</Link>
            <Link className="foot-col-link" to="/devoluciones">{t('footer.returns')}</Link>
            <Link className="foot-col-link" to="/soporte">{t('footer.support')}</Link>
          </div>

          <div>
            <div className="foot-col-h">{t('footer.colBrand')}</div>
            <Link className="foot-col-link" to="/sobre-nosotros">{t('footer.about')}</Link>
            <Link className="foot-col-link" to="/register">{t('footer.sell')}</Link>
            <Link className="foot-col-link" to="/terminos">{t('footer.terms')}</Link>
            <Link className="foot-col-link" to="/privacidad">{t('footer.privacy')}</Link>
          </div>
        </div>

        <div className="foot-bottom">
          <div className="foot-mark">
            <span className="foot-mark-glyph"></span>
            <span>{t('footer.rights')}</span>
          </div>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
