import { useNavigate } from 'react-router-dom';
import Btn from '../components/Btn';
import { t } from '../i18n';

function Devoluciones() {
  const navigate = useNavigate();
  const pasos = t('info.returns.pasos');
  const excluidos = t('info.returns.excluidos');

  return (
    <main className="screen">
      <div className="eyebrow mono">{t('info.returns.eyebrow')}</div>
      <h1 className="screen-title">{t('info.returns.title')} <em>{t('info.returns.titleEm')}</em></h1>

      <div className="info-2col">
        <div>
          <p className="info-lead">{t('info.returns.intro1')}</p>
          <p className="info-lead">{t('info.returns.intro2')}</p>

          <div className="info-col">
            {pasos.map((p) => (
              <div key={p.num} className="step-row">
                <div className="step-row-num">{p.num}</div>
                <div>
                  <div className="step-row-title">{p.titulo}</div>
                  <div className="step-row-desc">{p.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="info-col">
          <div className="ret-box">
            <div className="eyebrow mono">{t('info.returns.excludedTitle')}</div>
            {excluidos.map((item) => (
              <div key={item} className="ret-excluded-row">
                <span className="ret-excluded-x">✕</span>
                {item}
              </div>
            ))}
          </div>

          <div className="ret-box ret-box-accent">
            <div className="eyebrow mono">{t('info.returns.ctaTitle')}</div>
            <p className="info-lead">{t('info.returns.ctaText')}</p>
            <Btn variant="primary" onClick={() => navigate('/soporte')}>{t('info.returns.cta')}</Btn>
          </div>
        </div>
      </div>
    </main>
  );
}

export default Devoluciones;
